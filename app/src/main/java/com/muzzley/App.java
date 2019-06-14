package com.muzzley;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StrictMode;
import android.webkit.WebView;

import com.google.android.libraries.places.api.Places;
import com.muzzley.app.ActivityTracker;
import com.muzzley.app.Log;
import com.muzzley.app.login.AccountInteractor;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.CapabilityPermissionMap;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.PermissionChecker;
import dagger.Lazy;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import static android.os.StrictMode.ThreadPolicy;
import static android.os.StrictMode.VmPolicy;

public class App extends androidx.multidex.MultiDexApplication {

    @Inject PreferencesRepository preferencesRepository;
    @Inject
    Lazy<AccountInteractor> lazyAccountInteractor;

    public static AppComponent appComponent;

    public static App obtain(Context context) {
        return (App) context.getApplicationContext();
    }

//    private Crashlytics crashlytics;

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);

//        crashlytics = new Crashlytics.Builder().core(new CrashlyticsCore.Builder().disabled(!getResources().getBoolean(R.bool.crashlytics_active)).build()).build();
//        Fabric.with(this, crashlytics);


        System.setProperty("http.agent", String.format("%s/%s (Android %s ; %s %s)", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, Build.VERSION.RELEASE, Build.MANUFACTURER, Build.MODEL));

        RxJavaPlugins.setErrorHandler(throwable -> {
            Timber.e(throwable, "RxErrorHandler");
//            Crashlytics.logException(throwable);
        });

        Places.initialize(getApplicationContext(), getString(R.string.maps_key));
//        PlacesClient placesClient = Places.createClient(this);

        buildDebugOptions();
        buildObjectGraph();
        Timber.d("Creating app");

        //TODO: replace this with read from file assets
//        SdkInit.Analytics.Events events = null;
//        if (!TextUtils.isEmpty(getString(R.string.neura_client_id)) && !TextUtils.isEmpty(getString(R.string.neura_client_secret))) {
//            events = new SdkInit.Analytics.Events(
//                    getString(R.string.neura_client_id),
//                    getString(R.string.neura_client_secret)
//            );
//        }
//        String sdkInit = UtilsExtensionsKt.toJsonString(
//                new SdkInit(
//                        new SdkInit.Analytics(
//                                new SdkInit.Analytics.Ux(
//                                        getString(R.string.analytics_application_name),
//                                        getString(R.string.app_mixpanel_token)
//                                ),
//                                events
//                        )
//                )
//        );
//        Analytics.INSTANCE.init(this, sdkInit, habitStatusCodes -> {
//            Timber.d("Sdk status: %s", habitStatusCodes.name());
//            if (habitStatusCodes.equals(HabitStatusCodes.HABIT_SDK_SET_AUTHENTICATION)
//                    && preferencesRepository.getAuthorization() != null) {
//                Analytics.INSTANCE.setUser(UtilsExtensionsKt.toJsonString(preferencesRepository.getAuthorization()), status -> {
//                    Timber.d("setting SDK user after status code");
//                    return Unit.INSTANCE;
//                });
//            }
//            return Unit.INSTANCE;
//        });


        registerActivityLifecycleCallbacks(new ActivityTracker(this));

        prepareMuzzCapabilitiesPreferences();
        updateCurrentPermissions();

        if (preferencesRepository.getCustomerUserId() == null) {
            lazyAccountInteractor.get().newCustomerUserId()
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            x -> preferencesRepository.setCustomerUserId(x),
                            t -> Timber.e(t,"Error creating new customeUserId")
                    );
        }

    }

    //FIXME: should probably be called on the StartApp event. Seems reasonable
    public void updateCurrentPermissions() {
        Timber.d("Updating Current Permissions");
        Set<String> currentDevicePermissions = new HashSet();
        for (List<String> permissions : CapabilityPermissionMap.getMap().values()) {
            for (String permission : permissions) {
                if (!currentDevicePermissions.contains(permission) && checkPermission(permission)) {
                    currentDevicePermissions.add(permission);
                }
            }
        }
        preferencesRepository.setMuzzDevicePermissions(currentDevicePermissions);
    }


    private boolean checkPermission(String permission) {
        return (PermissionChecker.checkSelfPermission(this,
                permission) == PackageManager.PERMISSION_GRANTED);
    }

    private void prepareMuzzCapabilitiesPreferences() {

        Set<String> newMuzzCapabilities = new HashSet();

//        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
//            newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_CALL_INCOMING);
//            //newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_CALL_INCOMING_NO_NUMBER);
//            newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_CALL_MISSED);
//            //newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_CALL_MISSED_NO_NUMBER);
//            newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_SMS_INCOMING);
//            //newMuzzCapabilities.add(Constants.MUZ_CAP_TRIGGER_SMS_INCOMING_NO_NUMBER);
//        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)) {
            newMuzzCapabilities.add(Constants.MUZ_CAP_BLUETOOTH);
        }

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            newMuzzCapabilities.add(Constants.MUZ_CAP_LOCATION_GPS);
        }

        newMuzzCapabilities.add(Constants.MUZ_CAP_PUSH_NOTIFICATIONS);
        newMuzzCapabilities.add(Constants.MUZ_CAP_BG_AUDIO);
        newMuzzCapabilities.add(Constants.MUZ_CAP_TALK_FOSCAM);

        preferencesRepository.setMuzzCapabilities(newMuzzCapabilities);
    }

//    public void setCrashlyticsUserData(String userId, String userEmail, String appVersion) {
//        if (crashlytics != null) {
//            Crashlytics.setUserIdentifier(userId);
//            Crashlytics.setString("AppVersion", appVersion);
//        }
//    }

    private void buildDebugOptions() {
        if (BuildConfig.DEBUG) {
            //buildStrictOptions();
//            Console.setup(Level.ALL);
            Timber.plant(new Timber.DebugTree());
            Log.INSTANCE.init();
            if (Build.VERSION.SDK_INT >= 19) { //Build.VERSION_CODES.KITKAT
                WebView.setWebContentsDebuggingEnabled(true);
            }
        }
    }

    private void buildObjectGraph() {
//        AppComponent appComponent;
        appComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();
        appComponent.inject(this);
    }

    @SuppressLint("NewApi")
    private void buildStrictOptions() {
        ThreadPolicy.Builder thread = new ThreadPolicy.Builder()
                .detectAll()
                .detectDiskReads()
                .detectDiskWrites()
                .penaltyLog();

        VmPolicy.Builder vm = new VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .penaltyDeath()
                .penaltyLog();

        vm.detectLeakedClosableObjects();
        StrictMode.setThreadPolicy(thread.build());
        StrictMode.setVmPolicy(vm.build());
    }

}
