package com.muzzley.util.webview;

import android.webkit.ConsoleMessage;
import android.webkit.GeolocationPermissions;
import android.webkit.WebChromeClient;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 30/06/15.
 */
public class MuzzleyWebViewChromeClient extends WebChromeClient {

    @Override
    public boolean onConsoleMessage(ConsoleMessage cm) {
        Timber.d(cm.sourceId() + " [" + cm.lineNumber() + "] " + cm.message());
        return true;
    }

    @Override
    public void onGeolocationPermissionsShowPrompt(final String origin, final GeolocationPermissions.Callback callback) {
        callback.invoke(origin, true, false);
    }
}
