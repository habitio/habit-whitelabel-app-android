package com.muzzley.app;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.muzzley.App;
import com.muzzley.BuildConfig;
import com.muzzley.R;

import butterknife.ButterKnife;
import butterknife.BindView;
import timber.log.Timber;

public class WebViewActivity extends AppCompatActivity {

    public static final String EXTRA_URL = BuildConfig.APPLICATION_ID + ".extra.URL";

    @BindView(R.id.web_view)
    WebView mWebView;

    @BindView(R.id.loader)
    ProgressBar mLoader;

    private String mUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_web_view);

        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mUrl = bundle.getString(EXTRA_URL, "");
        }


        mWebView.getSettings().setJavaScriptEnabled(true);
//        mWebView.setWebChromeClient(new InternalWebChromeClient());
        mWebView.setWebViewClient(new InternalWebViewClient());
        mWebView.loadUrl(mUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.menu_webview, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem itemBrowser = menu.findItem(R.id.action_open_in_browser);
//        if (itemBrowser != null)
//            itemBrowser.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                handleBackIntents();
                return true;
            case R.id.action_open_in_browser:
                handleBrowserIntents();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if(mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            finish();
        }
    }

//    private void handleBackIntents() {
//        if (mViewSwitcher.getDisplayedChild() == 1) {
//            mWebview.loadUrl("about:blank");
//            mViewSwitcher.showPrevious();
//            mViewSwitcher.setInAnimation(slide_in_right);
//            mViewSwitcher.setOutAnimation(null);
//            if (itemBrowser != null) itemBrowser.setVisible(false);
//        } else {
//            finish();
//        }
//    }

    private void handleBrowserIntents() {
        if (mUrl != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(mUrl));
            browserIntent.addCategory(Intent.CATEGORY_DEFAULT);
            browserIntent.addCategory(Intent.CATEGORY_BROWSABLE);
            try {
                startActivity(browserIntent);
            } catch (Exception e) {
                Timber.e(e,"No default activity found");
            }
        }
    }

    private class InternalWebChromeClient extends WebChromeClient {

        @Override
        public void onProgressChanged(WebView view, int progress) {
            mLoader.setProgress(progress);
            if (progress == 100) {
                mLoader.setVisibility(View.GONE);
            } else {
                mLoader.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public boolean onConsoleMessage(ConsoleMessage cm) {
            Timber.d(cm.sourceId() + " [" + cm.lineNumber() + "] " + cm.message());
            return true;
        }

    }

    private class InternalWebViewClient extends WebViewClient {
//        @Override
//        public boolean shouldOverrideUrlLoading(WebView view, String url) {
//            view.loadUrl(url);
//
//            return false;
//        }

        @Override
        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {
            Timber.d("SslError: "+error.getUrl()+", "+error.getPrimaryError()+", "+error.toString());
            if (System.currentTimeMillis() > 0) {
                handler.proceed();
            } else {
                handler.cancel();
            }
        }
    }


//    @dagger.Module(injects = WebViewActivity.class, complete = false)
//    public static final class Module {
//
//    }
}
