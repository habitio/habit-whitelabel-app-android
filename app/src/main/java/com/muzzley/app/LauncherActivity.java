package com.muzzley.app;

import android.content.Intent;
import android.os.Bundle;

import com.muzzley.App;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.login.GetStartedActivity;
import com.muzzley.app.userprofile.UserPreferences;
import com.muzzley.model.user.Authorization;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.retrofit.AuthService;
import com.muzzley.util.rx.RxComposers;

import java.util.Date;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import dagger.Lazy;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class LauncherActivity extends AppCompatActivity {

    @Inject PreferencesRepository preferencesRepository;
    @Inject AuthService authService;

    @Inject Lazy<UserPreferences> lazyUserPreferences;
//    @Inject VersionUtil versionUtil;
//    @Inject @Named(PreferencesModule.KEY_GOOD_UNTIL_PREF) DatePreference goodUntil;
    @Inject AnalyticsTracker analyticsTracker;
    @Inject Lazy<SignUserController> lazySignUserController;
    AlertDialog mVersionExpiredAlert;
    boolean doFinish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
//        setContentView(R.layout.activity_launcher);
        startApplication(getIntent());
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        startApplication(intent);
    }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startApplication();
//
//        switch (versionUtil.getState()) {
//            case GRACE:
//                Timber.d("App should request date in background");
//                requestExpirationDate(false);
//            case CONTINUE:
//                Timber.d("App can continue because version is still valid");
//                startApplication();
//                break;
//            case EXPIRED:
//                Timber.d("App is blocked because version of app expired");
//                requestExpirationDate(true);
//        }
//
//    }

    @Override
    protected void onResume() {
        super.onResume();
        if (doFinish) {
            finish();
        }
        doFinish = true;
    }

//    private void requestExpirationDate(final boolean isBlocked) {
//        Timber.d("Start request for validate application");
//        versionUtil.checkVersionSupport()
//                .subscribe(new Consumer<VersionSupportResponse>() {
//                    @Override
//                    public void accept(VersionSupportResponse response) {
//                        goodUntil.set(response.getGoodUntil());
//
//                        if (isBlocked) {
//                            if (response.getGoodUntil().before(new Date()) && (mVersionExpiredAlert == null || !mVersionExpiredAlert.isShowing())) {
//                                showExpiredMessage();
//                            } else {
//                                startApplication();
//                            }
//                        }
//                    }
//                }, new Consumer<Throwable>() {
//                    @Override
//                    public void accept(Throwable throwable) {
//                        boolean expired = false;
//                        Timber.d(throwable, "LA: got error in expiration date");
//                        if (throwable instanceof HttpException) {
//                            HttpException error = (HttpException) throwable;
//                            if (error.code() == 403) {
//                                showExpiredMessage();
//                                expired = true;
//                            }
//                        }
//                        if (!expired) {
//                            startApplication();
//                        }
//                    }
//                });
//    }
//
//    private void showExpiredMessage() {
//        analyticsTracker.trackSimpleEvent(AnalyticsEvents.FORCE_TO_UPDATE_SHOW_EVENT);
//
//        if (mVersionExpiredAlert == null) {
//            mVersionExpiredAlert = new AlertDialog.Builder(this)
//                    .setMessage(R.string.mobile_update_app_warning)
//                    .setPositiveButton(R.string.mobile_update, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            try {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.muzzley")));
//                            } catch (ActivityNotFoundException e) {
//                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.muzzley")));//BuildConfig.APPLICATION_ID
//                            }
//
//                            analyticsTracker.trackSimpleEvent(AnalyticsEvents.FORCE_TO_UPDATE_REDIRECT_TO_STORE_EVENT);
//                        }
//                    })
//                    .setCancelable(false)
//                    .create();
//            mVersionExpiredAlert.setCanceledOnTouchOutside(false);
//        }
//
//        mVersionExpiredAlert.show();
//    }

    private void startApplication(Intent intent) {
        if (intent.getBooleanExtra("logout",false)) {
            Timber.e("is Logout");
            lazySignUserController.get().onSignOut();
        } else {
            Timber.e("is NOT Logout");
        }

//        if (!userPreference.exists()) {

        Authorization authorization = preferencesRepository.getAuthorization();
        Timber.e("hasUser: "+(preferencesRepository.getUser() != null)+", hasAuth: "+(authorization != null));
        if (preferencesRepository.getUser() == null || authorization == null) {
            startActivity(new Intent(this, GetStartedActivity.class));
        } else if (authorization.getExpires().after(new Date())) {
            lazyUserPreferences.get().updatePreferences();
            startActivity(new Intent(this, HomeActivity.class));
        } else {
            Timber.d("EEE Expired");
            setContentView(R.layout.activity_launcher);
            authService.exchange(authorization.getClientId(),authorization.getRefreshToken())
                    .compose(RxComposers.<Authorization>applyIo())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Authorization>() {
                        @Override
                        public void accept(@NonNull Authorization authorization) throws Exception {
                            Timber.d("EEE refreshed");
                            preferencesRepository.setAuthorization(authorization);
//                            try {
//                                Analytics.INSTANCE.logout(habitStatusCodes -> {
//                                    Timber.d("Error logging out SDK: "+habitStatusCodes);
//                                    return Unit.INSTANCE;
//                                });
//                            } catch (Exception e) {
//                                Timber.e(e,"Error logging out SDK");
//                            }
//                            try {
//                                Analytics.INSTANCE.setUser(UtilsKt.toJsonString(authorization),habitStatusCodes -> {
//                                    Timber.d("Error logging in SDK: "+habitStatusCodes);
//                                    return Unit.INSTANCE;
//                                });
//                            } catch (Exception e) {
//                                Timber.e(e,"Error logging in SDK");
//                            }
                            lazyUserPreferences.get().updatePreferences();
                            startActivity(new Intent(LauncherActivity.this, HomeActivity.class));
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(@NonNull Throwable throwable) throws Exception {
                            Timber.e(throwable,"EEE error refreshing");
                            startActivity(new Intent(LauncherActivity.this, GetStartedActivity.class));
                        }
                    });
        }


        doFinish = false;
//        finish(); //FIXME: we have "noHistory". Do we need to finish() ?
    }


//    private boolean userIsLoggedIn() {
//        return userPreference.exists() ;
//    }
}