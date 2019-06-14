package com.muzzley.services

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.muzzley.model.channels.Address
import com.muzzley.model.grants.GrantMessage
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.util.iff
import io.reactivex.*
import io.reactivex.disposables.Disposable
import io.reactivex.processors.PublishProcessor
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.*
import timber.log.Timber
import javax.inject.Inject

class Mqtt



    @Inject constructor() : Realtime {

//        Timber.e("MQTT initializing")
//
//        mqtt = new MQTT()
//        mqtt.setHost(context.getString(R.string.mqtt))
//        mqtt.setUserName(preferencesRepository.getUser().clientId)
//        mqtt.setPassword(preferencesRepository.getUser().authToken)
////        mqtt.setConnectAttemptsMax(1)
////        mqtt.setReconnectAttemptsMax(1)
//
//        Timber.d("mqtt clientId: ${preferencesRepository.getUser().clientId}, authToken: ${preferencesRepository.getUser().authToken}")
//        connection = mqtt.callbackConnection()
//
//        connect()
//        .subscribe(new LogObserver<Boolean>("mqtt connection"))


    private var mqtt: MQTT? = null
    var connected: Boolean = false
    var connection: CallbackConnection? = null
    val published: PublishProcessor<List<Any>>  = PublishProcessor.create()
    val pPublished: Observable<List<Any>>  = published.onBackpressureLatest().toObservable().share()
    val gson = Gson()
    val activeSubscriptions  = HashSet<String>()

    override
    fun disconnect(): Observable<Boolean> =
        Flowable.create(FlowableOnSubscribe<Boolean> {
            try {
                connection?.disconnect(object: Callback<Void?> {
                    override fun onSuccess(value: Void?) {
                        Timber.d("onSuccess disconnect")
                        // The result of the subscribe request.
                        it.onNext(true)
                        it.onComplete()
                        connection = null
                        mqtt = null
                        synchronized (activeSubscriptions) {
                            activeSubscriptions.clear()
                        }
                    }

                    override fun onFailure(throwable: Throwable) {
                        Timber.e(throwable, "onFailure disconnect")
                        it.onError(throwable)
                    }

                })
            } catch (throwable: Throwable) {
                Timber.e(throwable, "onFailure disconnect")
                it.onError(throwable)
            }
        }, BackpressureStrategy.LATEST).toObservable()


    override fun connect(user: String , pass: String , h: String ): Observable<Boolean> =

        Flowable.create(FlowableOnSubscribe<Boolean> { emitter ->
                try {
                    Timber.e("MQTT initializing")
                    val host = if( h.startsWith("mqtts"))  {
                         h.replaceFirst("mqtts","tls");
                    } else {
                        h.replaceFirst("mqtt","tcp");
                    }
                    mqtt = MQTT().apply {
                        setHost(host)
                        setUserName(user)
                        setPassword(pass)
                    }
//                    mqtt.setConnectAttemptsMax(1)
//                    mqtt.setReconnectAttemptsMax(1)

                    Timber.d("mqtt user: $user, authToken: $pass, host: $host")
                    connection = mqtt?.callbackConnection()

                    emitter.setDisposable(object: Disposable {
                        var d = false

                        override fun dispose(){
                            connection?.disconnect(null)
                            connected = false
                            mqtt = null
                            d = true
                        }

                        override
                        fun isDisposed(): Boolean  {
                            return d
                        }
                    })

                    connection?.listener(object: Listener {
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
                            published.onNext(arrayListOf(topic, body.ascii().toString().toByteArray())) // FIXME: this will probably fail for binary...

                            ack.run()
                        }

                        // this only gets called if connection was previously established
                        // and autoreconnect gives up
                        override
                        fun onFailure(throwable: Throwable) {
                            Timber.e(throwable, "onFailure listener")
//                            connected = false
//                            connection.disconnect(null) // FIXME: is this necessary ?
                            emitter.onError(throwable) //FIXME: what is the impact of this ?
                            //FIXME: should we propagate to published (or all listeners) ?
                        }
                    })


                    connection?.connect(object: Callback<Void?> {
                        override
                        fun onSuccess(value: Void? ) {
                            try {
                                Timber.i("onSuccess connect")
                                //we get also get notification in the connection listener
                                //check if we need to resubscribe
                                var topics: List<Topic>? = null
                                synchronized (activeSubscriptions) {
                                    topics = activeSubscriptions.map { Topic(it, QoS.AT_MOST_ONCE)}
                                }
                                if (topics.orEmpty().isNotEmpty()) {

                                    Timber.d("resubscribing to ${topics?.map { it.name().toString()}}")

                                        connection?.subscribe(topics?.toTypedArray(), object: Callback<ByteArray> {
                                            override fun onSuccess(qoses: ByteArray) {
        //                                        Timber.d("onSuccess resubscribe $qoses")
                                                // The result of the subscribe request.
                                            }

                                            override fun onFailure(throwable: Throwable ) {
                                                Timber.e(throwable, "onFailure resubscribe")
                                                emitter.onError(throwable)
                                            }
                                        })

                                }
                            } catch (e: Exception) {
                                Timber.e(e,"Error resubscribing topics")
                                //FIXME: I think we shoudln't emit an error in this case
                            }
                        }

                        //org.fusesource.mqtt.client.MQTTException: Could not connect: CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD
                        //java.io.IOException: java.nio.channels.UnresolvedAddressException
                        override
                        fun onFailure(throwable: Throwable ) {
                            Timber.e(throwable, "onFailure connect")
                            connected = false
//                            connection.disconnect(null) // FIXME: is this necessary ?
                            emitter.onError(throwable) //FIXME: what is the impact of this ?
                            //FIXME: should we propagate to published ?
                        }
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
    fun subscribe(topic: String ): Observable<Boolean>  =

            Flowable.create(FlowableOnSubscribe<Boolean> { emitter ->
                    try {
                        var isNewSubscription= false
                        synchronized (activeSubscriptions){
                            isNewSubscription = activeSubscriptions.add(topic)
                        }
                        if (!isNewSubscription) {
                            emitter.onNext(true)
                            emitter.onComplete()
//                            return
                        }
                        val topics: Array<Topic> = arrayOf(Topic(topic, QoS.AT_MOST_ONCE));
                        connection?.subscribe(topics, object: Callback<ByteArray> {
                            override fun onSuccess(qoses: ByteArray) {
                                Timber.d("onSuccess subscribe $topic, qoses: $qoses")
                                // The result of the subscribe request.
                                emitter.onNext(true)
                                emitter.onComplete()
                            }

                            override fun onFailure(throwable: Throwable ) {
                                Timber.e(throwable, "onFailure subscribe $topic")
                                emitter.onError(throwable)
                            }
                        });
                    } catch (throwable: Throwable ) {
                        Timber.e(throwable, "onFailure subscribe $topic")
                        emitter.onError(throwable)
                    }
            },BackpressureStrategy.LATEST).toObservable()

    override fun unsubscribe(topic: String ): Observable<Boolean> =
        Flowable.create(FlowableOnSubscribe<Boolean> { emitter ->
                try {
                    connection?.unsubscribe(arrayOf(UTF8Buffer.utf8(topic)), object: Callback<Void?> {
                        override fun onSuccess(value: Void? ) {
                            Timber.d("onSuccess unsubscribe")
                            // The result of the subscribe request.
                            emitter.onNext(true)
                            emitter.onComplete()
                        }

                        override fun onFailure(throwable: Throwable ) {
                            Timber.e(throwable, "onFailure unsubscribe")
                            emitter.onError(throwable)
                        }
                    });
                } catch (throwable: Throwable ) {
                    Timber.e(throwable, "onFailure unsubscribe")
                    emitter.onError(throwable)
                }
        },BackpressureStrategy.LATEST).toObservable()

    fun toBytes(obj: Any?): ByteArray =
        when (obj) {
            is JsonElement, String -> obj.toString().toByteArray()
            else -> gson.toJsonTree(obj).toString().toByteArray()
        }

    private fun publish(topic: String? , msg: Any?): Observable<Boolean>  =
        Flowable.create(object: FlowableOnSubscribe<Boolean> {
            fun onError(throwable: Throwable ,emitter: FlowableEmitter<Boolean>){
                Timber.e(throwable,"failed publish topic:$topic, msg:$msg")
                emitter.onError(throwable)
            }
            override
            fun subscribe(emitter: FlowableEmitter<Boolean> ) {
                try {
                    connection?.publish(topic, toBytes(msg), QoS.AT_MOST_ONCE, false, object: Callback<Void?> {
                        override
                        fun onSuccess(value: Void? ) {
                            Timber.d("published topic:$topic, msg:$msg")
                            emitter.onNext(true)
                            emitter.onComplete()
                        }

                        override
                        fun onFailure(throwable: Throwable ) {
                            onError(throwable,emitter)
                        }
                    })
                } catch (throwable: Throwable ) {
                    onError(throwable,emitter)
                }
            }
        },BackpressureStrategy.LATEST).toObservable()

    private fun listen(): Observable<List<Any>> =  pPublished

    override fun send(msg: RealtimeMessage ): Observable<Boolean> =
        publish(msg.address?.toTopic(),msg.payload)

    override fun send(msg: GrantMessage ): Observable<Boolean> =
        publish(msg.topic,msg.payload)

    override fun listenToRTM(): Observable<RealtimeMessage> =
        listen()
        .filter { !(it[0].toString()).contains("/grant") }
        .map {
            RealtimeMessage(
                address = Address.fromTopic(it[0].toString()),
                payload = gson.fromJson(String(it[1] as ByteArray),RealtimeMessage.Payload::class.java)
            )
        }.filter {
            it?.payload?.io?.name?.startsWith('i') ?: false
        }


    override fun listenToGrants(): Observable<GrantMessage> =
        listen()
        .filter { (it[0].toString()).contains("/grant") }
        .map {
            GrantMessage(
                topic = (it[0].toString()),
                payload =  gson.fromJson(String(it[1] as ByteArray),Map::class.java)

            )
        }.filter {
            (it?.payload?.get("io").toString() )?.startsWith("i")
        }

    override fun isConnected(): Boolean = connected


}