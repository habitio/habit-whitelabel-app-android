package com.muzzley.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;

import com.muzzley.BuildConfig;
import com.muzzley.app.LauncherActivity;
import com.muzzley.model.VersionSupportResponse;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.retrofit.ChannelService;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by kyryloryabin on 08/02/16.
 */
public class VersionUtil {

    @Inject ChannelService channelService;
    @Inject Context context;
    @Inject PreferencesRepository preferencesRepository;

    public enum VersionState { CONTINUE, GRACE, EXPIRED }

    @Inject
    public VersionUtil(){
    }

    public VersionState getState(){
        long days = TimeUnit.DAYS.convert(preferencesRepository.getExpirationdate().getTime() - new Date().getTime(), TimeUnit.MILLISECONDS);
        return days < 0 ?
                VersionState.EXPIRED :
                days < 2 ? VersionState.GRACE : VersionState.CONTINUE;
    }

    public void validateCurrentVersionSupport() {
        switch (getState()) {
            case CONTINUE: Timber.d("App can continue because version is still valid"); break;
            case GRACE: Timber.d("App should request date in background"); requestExpirationDate(); break;
            case EXPIRED:
                Timber.d("App is blocked because version of app expired");
                context.startActivity(new Intent(context, LauncherActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
                break;
        }
    }

    @SuppressLint("CheckResult")
    public void requestExpirationDate() {
        checkVersionSupport()
                .subscribe(new Consumer<VersionSupportResponse>() {
                    @Override
                    public void accept(VersionSupportResponse response) {
                        preferencesRepository.setExpirationdate(response.getGoodUntil());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.e(throwable,"Error checking version");
                    }
                });
    }

    public Observable<VersionSupportResponse> checkVersionSupport() {
        return channelService.checkVersionSupport(BuildConfig.VERSION_NAME.replaceAll("[^\\d\\.]", ""))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
