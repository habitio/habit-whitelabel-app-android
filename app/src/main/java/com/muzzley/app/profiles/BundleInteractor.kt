package com.muzzley.app.profiles

//import com.crashlytics.android.Crashlytics
import com.muzzley.model.Subscription
import com.muzzley.model.grants.GrantMessage
import com.muzzley.model.profiles.UrlRequest
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxComposers
import io.reactivex.Observable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class BundleInteractor
    @Inject constructor(
            val channelService:ChannelService,
            val profilesInteractor:ProfilesInteractor,
            val realtime:Realtime,
            val preferencesRepository:PreferencesRepository
    ) {

    fun postAllSubscriptions(subscriptions: List<Subscription>): Observable<List<Subscription>> =
        Observable.fromIterable(subscriptions)
                .compose(RxComposers.applyIo())
                .concatMap { subscription: Subscription  ->
                        Timber.d("Thread: ${Thread.currentThread().name}, tstamp:${System.currentTimeMillis()}")
                        profilesInteractor.postSubscription(subscription)
                }
                .toList().toObservable()
//                .doOnError(Crashlytics::logException)
                .delay(10,TimeUnit.SECONDS)

    fun getServiceBundleAuthorizationUrlRequest(bundleId: String ): Observable<UrlRequest> =
        channelService.getServiceBundleAuthorization3(bundleId)
                .onErrorReturn(profilesInteractor::ignoreRedirectError)
                .map(profilesInteractor::parseResponseHeaders)

    fun askApplicationGrant(deviceId: String , manufacturerName: String , manufacturerId: String ) =
        realtime.send(GrantMessage.request(preferencesRepository,"application",deviceId, mapOf("content" to manufacturerName, "id" to manufacturerId)))


    fun askUserGrant(deviceId: String ,manufacturerName: String , manufacturerId: String) =
        askGrant("user",deviceId,manufacturerName,manufacturerId)

    fun askGrant(role: String , deviceId: String ,manufacturerName: String , manufacturerId: String ) =
        realtime.send(GrantMessage.request(preferencesRepository,role,deviceId,mapOf("content" to  manufacturerName, "id" to manufacturerId)))
                .flatMap { realtime.listenToGrants()}
                .filter { it.topic.contains(role)}
                .map { it.payload }
                .take(1)
                .timeout(60,TimeUnit.SECONDS)

}