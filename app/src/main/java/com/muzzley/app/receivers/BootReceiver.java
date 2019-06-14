package com.muzzley.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.muzzley.App;
import com.muzzley.services.LocationInteractor;
import com.muzzley.services.PreferencesRepository;

import javax.inject.Inject;

import dagger.Lazy;
import timber.log.Timber;

/**
 * Created by ruigoncalo on 19/12/14.
 */
public class BootReceiver extends BroadcastReceiver {

    @Inject public Lazy<LocationInteractor> locationInteractor;
    @Inject public PreferencesRepository preferencesRepository;

    @Override
    public void onReceive(Context context, Intent intent) {
        App.appComponent.inject(this);
        Timber.d("On Boot Receiver ");
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())
                && preferencesRepository.getAuthorization() != null && preferencesRepository.getUser() != null) {
            try {
//                    locationInteractor.get().requestLocationUpdates();
                locationInteractor.get().registerAllGeofences();
            } catch (Throwable throwable) {
                Timber.e(throwable,"Error requesting location updates");
            }

        }
    }
}
