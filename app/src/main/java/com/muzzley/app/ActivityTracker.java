package com.muzzley.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.muzzley.App;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.services.PreferencesRepository;
//import com.muzzley.util.AdvertisingIdClient;
//import com.muzzley.util.VersionUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by caan on 19-02-2016.
 */
public class ActivityTracker implements Application.ActivityLifecycleCallbacks {

    private final long MAX_ACTIVITY_TRANSITION_TIME_MS = 2000;
    public volatile boolean wasInBackground = true;

    public @Inject AnalyticsTracker analyticsTracker;
//    public @Inject PreferencesRepository preferencesRepository;
//    public @Inject VersionUtil versionUtil;
    private App app;
    private Timer activityTransitionTimer;
    private TimerTask activityTransitionTimerTask;
    private int counter;

    public ActivityTracker(App app) {
        this.app = app;
//        app.inject(this);
        app.appComponent.inject(this);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityCreated");
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (counter == 0) {
            Timber.d("Experimental: coming from the background");
        } else {
            Timber.d("Experimental: activities already in the stack: "+counter);
        }
        counter++;
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityStarted");
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityResumed");
        if(wasInBackground){
            analyticsTracker.trackAppStart();
        }
        stopActivityTransitionTimer();
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityPaused");
        startActivityTransitionTimer();
    }

    @Override
    public void onActivityStopped(Activity activity) {
        counter--;
        if (counter == 0) {
            Timber.d("Experimental: going to the background");
        } else {
            Timber.d("Experimental: activities left in the stack: "+counter);
        }

        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityStopped");
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivitySaveInstanceState");
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Timber.d("Activity " + activity.getClass().getSimpleName() + " onActivityDestroyed");
    }

    public void startActivityTransitionTimer(){
        activityTransitionTimer = new Timer();
        activityTransitionTimerTask = new TimerTask() {
            @Override public void run() {
                wasInBackground = true;
                analyticsTracker.trackAppExit();
            }
        };

        activityTransitionTimer.schedule(activityTransitionTimerTask, MAX_ACTIVITY_TRANSITION_TIME_MS);
    }

    public void stopActivityTransitionTimer(){
        if(activityTransitionTimerTask != null){
            activityTransitionTimerTask.cancel();
        }

        if(activityTransitionTimer != null){
            activityTransitionTimer.cancel();
        }

        wasInBackground = false;
    }

}
