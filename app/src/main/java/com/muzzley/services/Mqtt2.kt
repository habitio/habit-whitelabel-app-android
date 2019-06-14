package com.muzzley.services

//import com.crashlytics.android.Crashlytics
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.model.channels.Address
import com.muzzley.model.grants.GrantMessage
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.util.filterIsInstance
import com.muzzley.util.parseJson
import com.muzzley.util.rx.LogCompletableObserver
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.*
import org.fusesource.mqtt.codec.CONNACK
import timber.log.Timber
import java.util.concurrent.ThreadFactory

class Mqtt2(val analyticsTracker: AnalyticsTracker) : Realtime {


//    private var mqtt: MQTT? = null
    private var connected: Boolean = false
//    var connection: CallbackConnection? = null
    private val published: PublishProcessor<Any>  = PublishProcessor.create()
    private val pPublished: Observable<Any> = published.onBackpressureLatest().toObservable().share()
    private val gson = Gson()
//    private val activeSubscriptions  = HashSet<String>()
    private val activeSubscriptions  = mutableSetOf<String>()

    override
    fun disconnect(): Observable<Boolean> =
            rxConnection.disconnect()
                    .doOnComplete {
                        synchronized (activeSubscriptions) {
                            activeSubscriptions.clear()
                        }
                    }
                    .andThen (Observable.just(true))


    private lateinit var rxConnection: RxConnection

    override fun connect(user: String, pass: String, h: String ): Observable<Boolean> =

            Flowable.create(FlowableOnSubscribe<Boolean> { emitter ->
                try {
                    Timber.e("MQTT initializing")
                    val host = if( h.startsWith("mqtts"))  {
                        h.replaceFirst("mqtts","tls");
                    } else {
                        h.replaceFirst("mqtt","tcp");
                    }
                    Timber.d("mqtt user: $user, authToken: $pass, host: $host")

                    val mqtt = MQTT().apply {
                        setHost(host)
                        setUserName(user)
                        setPassword(pass)
//                        setPassword("wrongcredential")

//                        val tg = ThreadGroup("Mqtt ThreadGroup").apply { isDaemon = true }
                        val STACK_SIZE = System.getProperty("mqtt.thread.stack_size", "${1024 * 512}").toLong()
                        val factory = ThreadFactory {
                            Thread(null, it, "MQTT Task New", STACK_SIZE).apply {
                                isDaemon = true
                                setUncaughtExceptionHandler { _, throwable ->
                                    analyticsTracker.trackThrowable(throwable)
//                                    Crashlytics.logException(throwable)
                                    val msg = "Disconnecting on uncaught Exception"
                                    Timber.e(throwable,msg)
                                    //FIXME: should we try to reconnect ?!
                                    //if so, rxconnection would need to be reassigned...
//                                    rxConnection.disconnect().subscribe(LogCompletableObserver(msg))
                                }
                            }
                        }
                        val threadPoolExecutor = MQTT.getBlockingThreadPool()
                        threadPoolExecutor.threadFactory = factory
                        blockingExecutor = threadPoolExecutor

//                        setConnectAttemptsMax(1)
//                        setReconnectAttemptsMax(1)
                    }

                    val connection = mqtt.callbackConnection()
                    rxConnection = RxConnection(connection)

                    emitter.setDisposable(object: Disposable {
                        var d = false

                        override fun dispose(){
                            connection?.disconnect(null)
                            connected = false
                            d = true
                        }

                        override
                        fun isDisposed(): Boolean = d
                    })

                    connection.listener(object: Listener {
                        override
                        fun onConnected() {
                            Timber.i("onConnected")
                            connected = true
                            emitter.onNext(true)
                        }

                        // if connection ist interruped, we still get this before onFailure
                        override
                        fun onDisconnected() {
                            Timber.e("onDisconnected")
                            connected = false
                            emitter.onNext(false)
                        }

                        override
                        fun onPublish(topic: UTF8Buffer, body: Buffer , ack: Runnable ) {
                            Timber.d("onPublish topic: $topic body: $body")
                            //FIXME: should we keep a list of subscriptions and send each message just to the ones that match ?

                            try {
                                if (topic.toString().contains("/grant")) {
                                    val payload = body.ascii().toString().parseJson<Map<*,*>>()
                                    if (payload?.get("io").toString().startsWith("i")) {
                                        published.onNext(GrantMessage(topic = topic.toString(), payload =  payload!!))
                                    }
                                } else {
                                    val payload = body.ascii().toString().parseJson<RealtimeMessage.Payload>()
                                    if (payload?.io?.name?.startsWith("i") == true) {
                                            published.onNext(RealtimeMessage(address = Address.fromTopic(topic.toString()), payload = payload)
                                        )
                                    }
                                }
                            } catch (e: Exception) {
                                Timber.e(e,"Error parsing received message")
                            }

                            ack.run()
                        }

                        // this only gets called if connection was previously established
                        // and autoreconnect gives up
                        override
                        fun onFailure(throwable: Throwable) {
                            Timber.e(throwable, "onFailure listener")
//                            connected = false
                            emitter.onError(throwable) //FIXME: what is the impact of this ?
                            //FIXME: should we propagate to published (or all listeners) ?
                        }
                    })

                    rxConnection.connect().andThen {
                        try {
                            Timber.i("onSuccess connect")
                            //we get also get notification in the connection listener
                            //check if we need to resubscribe
                            val topics = synchronized (activeSubscriptions) {
                                activeSubscriptions.toTypedArray()
                            }
                            rxConnection.subscribe(*topics).onErrorComplete()
                        } catch (e: Exception) {
                            Timber.e(e,"Error resubscribing topics")
                            //FIXME: I think we shouldn't emit an error in this case
                            Completable.complete()
                        }
                    }.subscribe({},{
                        //org.fusesource.mqtt.client.MQTTException: Could not connect: CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD
                        //java.io.IOException: java.nio.channels.UnresolvedAddressException
                        Timber.e(it, "onFailure connect")
                        connected = false
//                            connection.disconnect(null) // FIXME: is this necessary ?
                        val t = if(it is MQTTException && it.message?.contains(CONNACK.Code.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD.name) == true)
                            Realtime.Exception("Bad username or password", it)
                        else
                            it

                        emitter.onError(t) //FIXME: what is the impact of this ?
                        //FIXME: should we propagate to published ?
                    })

                } catch (throwable: Throwable ) {
                    Timber.e(throwable,"Error connecting")
                    emitter.onError(throwable)
                }

            },BackpressureStrategy.LATEST).toObservable()

    //FIXME: should we propagate connection errors ?
    //FIXME: should we get an Observable<Message> instead ?

    //If client is attempting a reconnect, it waits until is reconnected to get either callback
    // topico v1/iot/#

    override
    fun subscribe(topic: String): Observable<Boolean> =
            if (!synchronized(activeSubscriptions) { activeSubscriptions.add(topic) }) {
                Observable.just(true)
            } else {
                rxConnection.subscribe(topic).toSingleDefault(true).toObservable()
            }

    override fun unsubscribe(topic: String ): Observable<Boolean> =
            rxConnection.unsubscribe(topic).andThen(Observable.just(true))

    fun toBytes(obj: Any?): ByteArray =
            when (obj) {
                is JsonElement, String -> obj.toString().toByteArray()
                else -> gson.toJsonTree(obj).toString().toByteArray()
            }

    private fun publish(topic: String? , msg: Any?): Observable<Boolean>  =
            rxConnection.publish(topic!!,toBytes(msg))
                    .doOnSubscribe { Timber.d("publishing topic:$topic, msg:$msg") }
                    .doOnError { Timber.e(it,"failed publish topic:$topic, msg:$msg") }
                    .andThen(Observable.just(true))

    private fun listen(): Observable<Any> =  pPublished

    override fun send(msg: RealtimeMessage ): Observable<Boolean> =
            publish(msg.address?.toTopic(),msg.payload)

    override fun send(msg: GrantMessage ): Observable<Boolean> =
            publish(msg.topic,msg.payload)

    override fun listenToRTM(): Observable<RealtimeMessage> =
            listen().filterIsInstance()

    override fun listenToGrants(): Observable<GrantMessage> =
            listen().filterIsInstance()

    override fun isConnected(): Boolean = connected

    class RxConnection(private val connection: CallbackConnection) {

        inline fun <reified T> callback(emitter: CompletableEmitter): Callback<T> =
                object : Callback<T> {
                    override fun onSuccess(value: T?) = emitter.onComplete()
                    override fun onFailure(value: Throwable) = emitter.onError(value)
                }

        fun connect() = Completable.create { connection.connect(callback(it)) }
        fun disconnect() = Completable.create { connection.disconnect(callback(it)) }
        fun subscribe(vararg topics: String) =
                Completable.create{
                    val t = topics.map { Topic(it, QoS.AT_MOST_ONCE)}.toTypedArray()
                    if (t.isEmpty())
                        it.onComplete()
                    else
                        connection.subscribe(t,callback(it))
                }
        fun unsubscribe(topic: String) = Completable.create{ connection.unsubscribe(arrayOf(UTF8Buffer.utf8(topic)),callback(it))}
        fun publish(topic: String, ba: ByteArray) = Completable.create{ connection.publish(topic,ba,QoS.AT_MOST_ONCE, false,callback(it))}
    }

}