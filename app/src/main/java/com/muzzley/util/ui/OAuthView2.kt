package com.muzzley.util.ui

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.AttributeSet
import android.webkit.CookieManager
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.RelativeLayout
import com.muzzley.model.profiles.UrlRequest
import com.muzzley.util.isNotNullOrEmpty
import io.reactivex.Observable
import io.reactivex.functions.Predicate
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.oauth.view.*
import timber.log.Timber

class OAuthView2
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = android.R.attr.textViewStyle
    ): RelativeLayout(context,attrs,defStyleAttr) {

//    @InjectView(R.id.webview) lateinit var webView: WebView
//    @InjectView(R.id.progress_bar) lateinit var progress_bar: View

    var urlObservable: PublishSubject<String>? = null
    var predicate: Predicate<String>? = null

//    OAuthView2(Context context) {
//        super(context)
//    }
//
//    OAuthView2(Context context, AttributeSet attrs) {
//        super(context, attrs)
//    }
//
//    OAuthView2(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr)
//    }
//
//    OAuthView2(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
//        super(context, attrs, defStyleAttr, defStyleRes)
//    }

    override
    fun onFinishInflate() {
        super.onFinishInflate()

        val webViewClient = object: WebViewClient() {

            override
            fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon);
                Timber.d("OnPageStarted=$url");
                progress_bar.show()
            }

            override
            fun onPageFinished(view: WebView, url: String) {
                Timber.d("OnPageFinished=$url");
                progress_bar.hide()
            }

            override
            fun onReceivedError(view: WebView, errorCode: Int, description: String?, failingUrl: String?) {
                Timber.d("WebView Error: $description, errorCode: $errorCode, failingUrl: $failingUrl, scheme: ${Uri.parse(failingUrl).getScheme()}");
            }

            override
            fun onReceivedLoginRequest(view: WebView, realm: String?, account: String?, args: String?) {
                Timber.d("WebView Login: $realm \n$account\n$args");
            }

            override
            fun  shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                Timber.d("ShouldOverrideUrlLoading=$url");
                val  handled = predicate?.test(url) ?: false
                if ( handled ) {
                    urlObservable?.onNext(url)
                }
                return handled
            }
        };

        val cookieManager = CookieManager.getInstance();
        if (Build.VERSION.SDK_INT >= 21) { //Build.VERSION_CODES.LOLLIPOP
            cookieManager.removeAllCookies{
                    Timber.d("Remove all cookies: $it");
            }
        } else {
            cookieManager.removeAllCookie();
        }

//        WebSettings webSettings = webView.getSettings();
//        webSettings.setJavaScriptEnabled(true);
//        webSettings.setSupportZoom(false);
//        webSettings.setBuiltInZoomControls(false);
//        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
//

        webview.settings.apply {
            javaScriptEnabled = true;
            setSupportZoom(false);
            builtInZoomControls = false;
            cacheMode = WebSettings.LOAD_NO_CACHE;
            domStorageEnabled = true
        }

        webview.setWebViewClient(webViewClient);


    }

    fun loadUrlRequest(urlRequest: UrlRequest, predicate: Predicate<String> = Predicate{ false }): Observable<String> {
        this.predicate = predicate
        Timber.d("Loading urlRequest.url: ${urlRequest.url}")
        return PublishSubject.create<String>().also {
            urlObservable = it
            if (urlRequest.headers.isNotNullOrEmpty()) {
                webview.loadUrl(urlRequest.url, urlRequest.headers)
            } else {
                webview.loadUrl(urlRequest.url)
            }
        }

    }


}