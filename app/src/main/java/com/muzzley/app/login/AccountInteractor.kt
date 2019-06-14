package com.muzzley.app.login

import android.content.Context
import android.provider.Settings
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.model.Me
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.retrofit.AuthService
import com.muzzley.util.retrofit.UserService
import dagger.Lazy
import io.reactivex.Completable
import io.reactivex.Observable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AccountInteractor

    @Inject constructor(
            val authService: AuthService ,
            val analyticsTracker:AnalyticsTracker ,
            val context: Context,
            val preferencesRepository: PreferencesRepository,
            val userServiceLazy: Lazy<UserService>
    )
    {

    companion object {
        const val TYPE_FACEBOOK = "facebook";
        const val TYPE_GOOGLE = "google";
        const val TYPE_EMAIL = "email";
        const val TYPE_ACCOUNT = "account"; //used for account creation (sign up) with email
    }

    //legacy signUp
    fun signUp(me: Me): Completable  {
//        val track = analyticsTracker.&trackSignUpFinish.curry(me.getEmail(), me.getName(), normalizeAuthType(me.getAuthType()))

        val track = { status: EventStatus , detail: String?  ->
            analyticsTracker.trackSignUpFinish(me.email, me.name, normalizeAuthType(me.authType),status,detail)
        }
        me.device = "android"
        me.appId = preferencesRepository.applicationId
        return authService.signup(me)
                .ignoreElements() // because this is legacy stuff and we're calling getUser afterwards anyway
                .andThen(auth(me.email,me.password))
                .doOnError { track(EventStatus.Error, it.message) }
                .doOnComplete { track(EventStatus.Success, "Success") }
    }

    fun auth(username: String?, password: String?): Completable =
        authService.authorize(context.getString(R.string.app_client_id), username, password)
//                .doOnNext(preferencesRepository::setAuthorization)
                .doOnNext {preferencesRepository.authorization = it}
//                .doOnNext{
//                    try {
//                        Analytics.setUser(it.toJsonString()) { Timber.d("setAuthorization in SDK: $it") }
//                    } catch (e: Exception) {
//                        Timber.e(e, "Error logging in SDK")
//                    }
//                }
                .delay(2,TimeUnit.SECONDS) //FIXME: hack because of backend sync
                .flatMap { userServiceLazy.get().getUser() }
//                .doOnNext(preferencesRepository::setUser)
                .doOnNext {preferencesRepository.user = it }
                .flatMap{ userServiceLazy.get().getPreferences() }
//                .doOnNext(preferencesRepository::setPreferences)
                .doOnNext {preferencesRepository.preferences = it }
                .ignoreElements()

    fun normalizeAuthType(authType: String?): String =
        when(authType) {
            TYPE_GOOGLE ->Constants.SIGN_IN_TYPE_GOOGLE
            TYPE_FACEBOOK ->Constants.SIGN_IN_TYPE_FACEBOOK
            else ->Constants.SIGN_IN_TYPE_EMAIL
        }

    fun validate(email: String, password: String): String? =
        when {
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> context.getString(R.string.mobile_email_invalid)
            password.length < 6 -> context.getString(R.string.mobile_pass_weak)
            else -> null
        }

    fun newCustomerUserId() =
        Observable.fromCallable {
            try {
                AdvertisingIdClient.getAdvertisingIdInfo(context).id
                        ?.also { Timber.i("Customer ID set to Advertising Id") }
            } catch (e: Exception) {
                Timber.e(e,"Error getting Advertising Id")
                null
            }
            ?: Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                    ?.also { Timber.i("Customer ID set to Android Id") }
            ?: UUID.randomUUID().toString()
                    .also { Timber.i("Customer ID set to randomUUID") }
        }
}