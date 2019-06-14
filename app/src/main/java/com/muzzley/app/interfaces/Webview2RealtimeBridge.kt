package com.muzzley.app.interfaces

import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.model.channels.Address
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.model.tiles.TileWidgetOptions
import com.muzzley.model.webview.WebviewMessage
import com.muzzley.services.Realtime
import com.muzzley.util.parseJson
import com.muzzley.util.rx.LogObserver
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.toJsonString
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @see <a href="https://bitbucket.org/muzzley/muzzley-wiki/wiki/js-bridge">https://bitbucket.org/muzzley/muzzley-wiki/wiki/js-bridge</a>
 */

class Webview2RealtimeBridge (
        private val webView: WebView,
        private val realtime: Realtime,
        private val modelsStore: ModelsStore,
        private val options: String
    ){
    
    private val topicSubscriptions = mutableSetOf<String>()
    private val handler : Handler= Handler(Looper.getMainLooper())
    private val ready : AtomicBoolean= AtomicBoolean(false)
    private val lock : Any = Object()
    private val subscription: Disposable


    init {
        subscription = realtime.listenToRTM()
                .filter{ it.isInfo() && topicIsSubscribed(it.address.toTopic()) }
                .map {
                    val msg = mapOf("data" to  mapOf("a" to  "publish",
                            "h" to mapOf("ch" to  it.address.channel ),
                            "d" to mapOf("io" to it.payload.io?.name, "data" to  it.payload.data) + addressToMap(it.address)))

                    msg.toJsonString()
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        Timber.d("sending response $it")
                        if (!ready.get()) {
                            Timber.e("Not yet ready !")
                        }
                        synchronized (lock) {
                            webView.loadUrl("javascript:bridge._handle('$it')")
                        }
                    },
                    { Timber.e(it, "mqtt Error while listening to messages") },
                    { Timber.d("mqtt stream ended") }
                )

    }

    private fun topicIsSubscribed(topic: String) =
            synchronized (topicSubscriptions) {
                topicSubscriptions.any {
                    val i = it.indexOf('#')
                    i > 0 && topic.startsWith(it.substring(0 until i)) || topic == it
                }
            }


    private fun addressToMap(address: Address): Map<String,String> =
        mapOf(
                "channel" to address.channel,
                "component" to address.component,
                "property" to address.property
        )

    private fun buildSubscribeTopic(webviewMessage: WebviewMessage): String {
        val sbTopic = StringBuilder("/v3")

        val payload = webviewMessage.data?.d
        payload?.apply {
            if (!channel.isNullOrEmpty()) {
                sbTopic.append("/channels/$channel")
            }
            if (!component.isNullOrEmpty()) {
                sbTopic.append("/components/$component")
            }
            if (!property.isNullOrEmpty()) {
                sbTopic.append("/properties/$property")
            }
        }

        sbTopic.append("/#")
        return sbTopic.toString()
    }

    @JavascriptInterface
    fun call(message: String) {
        Timber.d("call called from webview: ${Thread.currentThread().name} " + message)
        handler.postDelayed( {
            Timber.d("call called from webview: ${Thread.currentThread().name} " + message)
            val webviewMessage = message.parseJson<WebviewMessage>()
            val action = webviewMessage?.data?.a
            Timber.d("Got action: $action")

            when (action) {
                "subscribe" -> {
                    val topic = buildSubscribeTopic (webviewMessage) //if we were really subscribing this should include the user
                    Timber.d("adding topic $topic")
                    synchronized(topicSubscriptions) {
                        topicSubscriptions.add(topic)
                    }
                    val webviewResponse = mapOf("rcid" to webviewMessage.cid, "data" to mapOf("s" to true, "m" to "Subscribed",
                    "d" to mapOf("channel" to mapOf("id" to webviewMessage.data.d?.channel))))
                    send2Webview(webviewResponse)
                }
                "publish" ->
                    webviewMessage.data.d?.apply {
                        if (channel != null && component != null && property != null ) {
                            val addr = Address(channel,component,property)
                            val rtmsg =
                                    when (io) {
                                        "r" -> RealtimeMessage.read(addr,data)
                                        "w" -> RealtimeMessage.write(addr,data)
                                        else -> null
                                    }
                            if (rtmsg != null) {
                                realtime.send(rtmsg)
                                        .compose(RxComposers.applyIo())
                                        .subscribe(LogObserver<Boolean>("published"))
                            }
                        }
                    }
                "getUnitsSpec" -> send2Webview(mapOf(
                        "rcid" to webviewMessage.cid, 
                        "data" to mapOf(
                                "s" to true, 
                                "d" to modelsStore.models?.unitsTable
                        )))

                "getAllComponents" -> {
                    val tileOptions = options.parseJson<TileWidgetOptions>()
                    val ch = tileOptions?.channels?.find { it.id == tileOptions.tiles[0].channel }
                    val nativeComponents = ch?._interface?.native
                    send2Webview(mapOf(
                            "rcid" to webviewMessage.cid,
                            "data" to mapOf(
                                    "s" to true,
                                    "d" to mapOf("nativeComponents" to nativeComponents))))
                }
                "openUrl" -> {
                    val data =
                    try {
                        //FIXME: should we delegate back to activity ?
                        webView.context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(webviewMessage.data.d?.value?.asString)))
                        mapOf("s" to  true, "m" to "Success")
                    } catch (throwable: Throwable) {
                        mapOf("s" to false, "m" to  throwable.message)
                    }
                    if (webviewMessage.cid != null) {
                        Timber.d("got callback")
                        send2Webview(mapOf("rcid" to webviewMessage.cid, "data" to  data))
                    }
                }
                "getWhiteLabelConfig" -> getSharedInterface(webviewMessage)
                else -> Timber.e("webview action $action not implemented")
            }
        }, 0)
    }

    private fun getSharedInterface(webviewMessage: WebviewMessage) =
            send2Webview(
                    mapOf(
                            "rcid" to  webviewMessage.cid,
                            "data" to  mapOf(
                                    "s" to true,
                                    "d" to try {
                                        webView.context.assets.open("shared_interface.json")
                                                .bufferedReader()
                                                .use { it.readText() }
                                                .parseJson<Map<String,String>>()
                                    } catch (e: Exception) {
                                        Timber.e(e,"Error parsing shared_interface.json")
                                        emptyMap<String,String>()
                                    }

                            )
                    )
            )


    private fun send2Webview(obj: Any) {
        val response = obj.toJsonString()
        Timber.d("sending to webview $response")

        fun load() {
            if (!ready.get()) {
                Timber.e("Not yet ready !")
            }
            synchronized (lock) {
                webView.loadUrl("javascript:bridge._handle('$response')")
            }
        }

        if (Looper.myLooper() != Looper.getMainLooper()) {
            Timber.e("was not on main thread !?") // because 'JavaBridge' Thread ...
            webView.post { load() }
        } else {
            load()
        }

    }
    @JavascriptInterface
    fun ready() {
        Timber.d("ready called from webview")
        ready.set(true)

    }
    @JavascriptInterface
    fun options(): String {
        Timber.d("returning options to webview: $options")
        return options
    }

    fun destroy() {
        subscription.dispose()
    }
}