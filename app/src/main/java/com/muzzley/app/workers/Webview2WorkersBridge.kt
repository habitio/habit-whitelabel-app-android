package com.muzzley.app.workers

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.model.webview.WebviewMessage
import com.muzzley.util.parseJson
import com.muzzley.util.toJsonString
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.atomic.AtomicBoolean

class Webview2WorkersBridge (

        val webView: WebView,
        val options: String,
        val modelsStore: ModelsStore,
        val workable: Workable
    ){

    val handler : Handler = Handler(Looper.getMainLooper());
    val ready : AtomicBoolean = AtomicBoolean(false)
    val lock : Object = Object();

    @JavascriptInterface
    fun call(message: String) {
        Timber.d("call called from webview: ${Thread.currentThread().name} " + message);
        handler.post{
            Timber.d("call called from webview: ${Thread.currentThread().name} " + message);
            message.parseJson<WebviewMessage>()?.let { webviewMessage ->
                val action = webviewMessage.data?.a
                Timber.d("Got action: $action")

                when (action) {
                    "getUnitsSpec" ->
                        //FIXME: refactor this into proper place (should not be modelStates)
                        send2Webview(mapOf("rcid" to  webviewMessage.cid, "data" to  mapOf("s" to true, "d" to  modelsStore.modelsStates?.unitsTable)))
                    "saveRule" ->
//                    workable.onRuleSaved(webviewMessage.data.d.toString()) //FIXME: check this path
                        workable.onRuleSaved(webviewMessage.data.d.toJsonString()) //FIXME: check this path
                    "getContacts" -> getContacts(webviewMessage)
                    "getWhiteLabelConfig" -> getSharedInterface(webviewMessage)
                    else -> Timber.e("webview action $action not implemented in workers bridge");
                }
            } ?: Timber.e("Could not parse WebviewMessage")
        }
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

    fun getContacts(webviewMessage: WebviewMessage) {
        workable.onGetContacts()
                .subscribe(
                {
                    send2Webview(mapOf("rcid" to  webviewMessage.cid, "data" to mapOf("s" to true, "d" to  it)))
                },
                {
                    Timber.e(it,"Error getting contacts")
                    send2Webview(mapOf("rcid" to  webviewMessage.cid, "data" to  mapOf("s" to  false, "m" to  "{code: 403, description: no permission}")))
                }
        )

    }

    @JavascriptInterface
    fun ready() {
        Timber.d("ready called from webview")
        ready.set(true)

    }
    @JavascriptInterface
    fun options(): String{
        Timber.d("returning options to webview: $options");
        return options
    }

//    fun destroy() {
//        webView = null
//    }
//
//    //throws Throwable
//    protected
//    fun finalize() {
////        Timber.d("finalized called")
//        destroy()
////        super.finalize()
//    }

    private fun send2Webview(obj: Any) {
        val response = obj.toJsonString()
        Timber.d("sending to webview $response");

        if (Looper.myLooper() != Looper.getMainLooper()) {
            Timber.e("was not on main thread !?")
            webView.post { // because 'JavaBridge' Thread ...
                if (!ready.get()) {
                    Timber.e("Not yet ready !")
                }
                synchronized (lock) {
                    webView.loadUrl("javascript:bridge._handle('$response')");
                }
            }
        } else {
            if (!ready.get()) {
                Timber.e("Not yet ready !")
            }
            synchronized (lock) {
                webView.loadUrl("javascript:bridge._handle('$response')");
            }
        }

    }

    interface Workable {
        fun onRuleSaved(rule: String);
        fun onGetContacts(): Observable<Any>
    }


}