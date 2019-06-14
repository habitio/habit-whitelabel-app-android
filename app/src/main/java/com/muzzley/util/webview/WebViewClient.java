package com.muzzley.util.webview;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.io.IOException;
import java.io.InputStream;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 20/10/14.
 */
public class WebViewClient extends android.webkit.WebViewClient {

    private final Context context;
    private ProgressBar progress;

    public WebViewClient(Context context, ProgressBar progress) {
        this.context = context;
        this.progress = progress;
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        Timber.d("Load resource " + url);
        super.onLoadResource(view, url);
    }

    @Override public void onPageStarted(WebView view, String url, Bitmap favicon) {
        Timber.d("Start page '" + url + "' load");
        super.onPageStarted(view, url, favicon);
        if(progress != null && progress.getVisibility() != View.VISIBLE) {
            progress.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if ("about:blank".equals(url)) return;

        view.requestFocus();
        if(progress != null) {
            progress.setVisibility(View.GONE);
        }

        Timber.d("Page finished, inject Javascript bridge");
        final String bridge = new String(getResourceFromAssets("webviewjavascriptbridge.js"));
        view.loadUrl("javascript:" + bridge);
        Timber.d("Javascript bridge injected");
        view.requestFocus();
    }

    private byte[] getResourceFromAssets(String location) {
        try {
            // get input stream for text
            InputStream stream = context.getAssets().open(location);
            int size = stream.available();
            byte[] buffer = new byte[size];
            stream.read(buffer);
            stream.close();

            return buffer;
        } catch (IOException ex) {
            return new byte[]{';'};
        }
    }

//    @Override
//    public boolean shouldOverrideUrlLoading(WebView view, String url) {
//        // TODO: reduce url loading to muzzley
//        return super.shouldOverrideUrlLoading(view, url);
//    }
}
