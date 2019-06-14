package com.muzzley.app

import android.app.Activity
import android.content.Context
//import com.crashlytics.android.Crashlytics
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.login.UnauthorizedActivity
import com.muzzley.app.notifications.AzureNotificationHub
//import com.muzzley.app.scheduling.ScheduleInteractor
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.services.LocationInteractor
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.rx.LogObserver
import com.muzzley.util.startActivity
import com.muzzley.util.ui.ShowcaseBuilder
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Created by ruigoncalo on 22/12/14.
 */
@Singleton
class SignUserController
    @Inject constructor(
            var context: Context,
            var preferencesRepository: PreferencesRepository,
            var locationInteractor: LocationInteractor,
            var modelsStore: ModelsStore,
            var realtime: Realtime,
            var analyticsTracker: AnalyticsTracker
    ) {

    fun onSignIn(activity: Activity) {
        val user = preferencesRepository.user ?: return

        Timber.d("User ${user.name} [${user.id}] ${user.email}")
        //        realtime.connect(user.getClientId(), user.getAuthToken()).subscribe(new LogObserver<Boolean>("mqtt connect"));


        realtime.connect(context!!.getString(R.string.app_client_id),
                preferencesRepository.authorization!!.accessToken!!,
                preferencesRepository.authorization!!.endpoints!!.mqtt!!)
//                .flatMap(object : Function<Boolean, ObservableSource<Boolean>> {
//                    @Throws(Exception::class)
//                    override fun apply(@NonNull aBoolean: Boolean?): ObservableSource<Boolean> {
//                        return realtime!!.subscribe("/v3/users/" + preferencesRepository!!.user!!.id + "/channels/#")
//                    }
//                })
//                .flatMap(object : Function<Boolean, ObservableSource<Boolean>> {
//                    @Throws(Exception::class)
//                    override fun apply(@NonNull aBoolean: Boolean?): ObservableSource<Boolean> {
//                        return realtime!!.subscribe("/v3/users/" + preferencesRepository!!.user!!.id + "/grants/#")
//                    }
//                })
//                .flatMap(object : Function<Boolean, ObservableSource<Boolean>> {
//                    @Throws(Exception::class)
//                    override fun apply(@NonNull aBoolean: Boolean?): ObservableSource<Boolean> {
//                        return realtime!!.subscribe("/v3/applications/" + context!!.getString(R.string.app_client_id) + "/grants/#")
//                    }
//                })
//                .retry { throwable ->
//                    Timber.e(throwable, "Testing for recoverable error")
//                    //                        if (throwable instanceof ProtocolException && throwable.getMessage().contains("Command from server contained an invalid message id")) {
//                    Thread.sleep(5000)
//                    true
//                    //                        }
//                    //                        return false ;
//                }
//                .retry { throwable ->
//                    Timber.e(throwable, "Testing for recoverable error")
//                    //                        if (throwable instanceof ProtocolException && throwable.getMessage().contains("Command from server contained an invalid message id")) {
//                    true
//                    //                        }
//                    //                        return false ;
//                }
//                .retryWhen { Observable.timer(5, TimeUnit.SECONDS) }
                .subscribe(
                        { Timber.i("mqtt connect $it") },
                        {
                            Timber.e(it, "mqtt connect Fatal error")
                            analyticsTracker.trackThrowable(it)
//                            Crashlytics.logException(it)
                            if (it is Realtime.Exception) { // we might have different ones in the future
                                activity.startActivity<UnauthorizedActivity>()
                            }
                        }
                )
        //                .subscribe(new LogObserver<Boolean>("mqtt connect"));
        startLocationService()
//        ShortcutWidgetProvider.sendUpdateWidgetsBroadcast(context)
        //        connectMuzzley(user);
        //FIXME: use this to connect to mqtt ?
        //        pushNotificationsService.start(user);
    }

    fun onSignOut() {
//        ShortcutWidgetProvider.sendUpdateWidgetsBroadcast(context)
        analyticsTracker.trackSignOut(preferencesRepository.user?.email)

        realtime.disconnect().subscribe(LogObserver("mqtt disconnect"))

        Observable.fromCallable {
            try {
//                AzureNotificationHub(context).unregister()
                AzureNotificationHub(context.getString(R.string.azure_notification_hub_path),
                        preferencesRepository.azureEndpoint, context).unregister()
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
        .subscribeOn(Schedulers.io())
        .subscribe(
                { Timber.d("Unregistered from Azure: $it") },
                { Timber.d(it, "Could not unregister from Azure") }
        )

//        ScheduleInteractor.cancellAll(context)

        ShowcaseBuilder.clearShown(context).subscribe()
        preferencesRepository.apply {
            user = null
            authorization = null
            push = null
            userChannelId = null
            customerUserId = null
            firebaseToken = null
            azureId = null
            tags = null
            preferences = null
            lastKnownLocation = null
//            muzzCapabilities = null
//            muzzDevicePermissions = null
            expirationdate = null
            calls = null
            interfacesStore = null

        }
        stopLocationService()
        modelsStore.clear()

//        try {
//            Analytics.logout { Timber.d("Error logging out SDK: $it") }
//        } catch (e: Exception) {
//            Timber.e(e, "Error logging out SDK")
//        }
    }

    private fun startLocationService() {
//        locationInteractor.requestLocationUpdates()
        locationInteractor.registerAllGeofences()
    }

    private fun stopLocationService() {
//        locationInteractor.removeLocationUpdates()
        locationInteractor.unregisterAllGeofences()
    }


}
