package com.muzzley.app.interfaces

import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface
import com.muzzley.model.webview.WebviewMessage
import com.muzzley.util.parseJson
import com.muzzley.util.ui.ijk.VideoFrame;
import timber.log.Timber

class Webview2VideoBridge(val videoFrame: VideoFrame) {

    val handler : Handler = Handler(Looper.getMainLooper());


    @JavascriptInterface
    fun on(message: String) {
        Timber.e("not handling 'on' called from webview.videoBridge: $message");
    }

    @JavascriptInterface
    fun send(message: String) {
        Timber.d("send called from webview $message")
        handler.post { // because this is called from "JavaBridge" thread
//            val webviewMessage = gson.fromJson(message,WebviewMessage::class.java)
            val webviewMessage = message.parseJson<WebviewMessage>()
            val action = webviewMessage?.data?.a

            when (action) {
                "videoSendAction" ->
                    when (webviewMessage.data.d?.type) {
                        "audio" -> videoFrame.setEnabled(webviewMessage.data.d.value?.asBoolean ?: false,VideoFrame.TrackType.audio);
                            
//                    "background_audio":
//                        if (audioUrl != null) {
//                            targetUrl = webviewMessage.data.d.value.asBoolean ? audioUrl : videoUrl;
//                            videoFrame.setVideoPath(targetUrl);
//                        }
//                        break
                        "play_video" ->
//                            videoFrame.setVideoPath(webviewMessage.data.d.value.asString)
                            webviewMessage.data.d.value?.asString?.let {
                                videoFrame.setAndPlay(VideoFrame.UrlType.recorded,it)
                            } ?: Timber.e("No url found")

                            //FIXME: answer callback ?
                            
                        "microphone" ->
                            videoFrame.setMicrophone(webviewMessage.data.d.value?.asBoolean ?: false)
                            

//                        "LOW_BATT_NOTIFICATION":
//                        "RECORDING":
//                        "PLAY_LIVE_STREAM":
//                        "PLAY_VID_LIBRARY":
//                        "BACKGROUND_AUDIO_STATUS":
                        else ->
                            Timber.e("type ${webviewMessage.data.d?.type} not implemented ")
                    }
                else ->
                    Timber.e("action $action not implemented")
            }

        }

    }

}