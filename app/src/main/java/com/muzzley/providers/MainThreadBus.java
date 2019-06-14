package com.muzzley.providers;

import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 11/06/14.
 */
public class MainThreadBus extends Bus {
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override public void post(final Object event) {
        if(event != null) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                super.post(event);
            } else {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        MainThreadBus.super.post(event);
                    }
                });
            }
        } else {
            Timber.d("Event is null");
        }
    }
}
