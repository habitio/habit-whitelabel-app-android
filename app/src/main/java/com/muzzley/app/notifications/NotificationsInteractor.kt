package com.muzzley.app.notifications

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.iid.FirebaseInstanceId
import com.muzzley.R
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.toObservable
import dagger.Lazy
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class NotificationsInteractor
    @Inject constructor(
            val lazyUserService:Lazy<UserService>,
            val preferencesRepository :PreferencesRepository,
            val context:Context
    ) {



    @SuppressLint("CheckResult")
    fun register() {

        if (preferencesRepository.user == null) {
            Timber.e("No user! Bailing out of cloud notification registration")
            return
        }
        //FIXME: We need register _everytime_ because of azure's expiration (at most 90 days),
        //FIXME: and because we have nothing implemented (i.e. a push) that triggers token renewal
        //FIXME: so, we're not using previously saved tokens and tags

        Observable.zip(firebaseToken(),getTags(),
                BiFunction<String,String,List<String>> { token: String , tags: String  ->
                    listOf(token, tags)
                }
        )
        .subscribeOn(Schedulers.io())
        .observeOn(Schedulers.io())
        .flatMap {
            registerAzure(it[0],it[1])
        }
        .retryWhen {
                it.doOnNext {
                    Timber.e(it,"Cloud messaging registration error. Retrying in 30s")
                }.flatMap {
                    Observable.timer(30, TimeUnit.SECONDS);
                }
        }
        .subscribe(
                { Timber.i("Cloud messaging registered successfully")},
                { Timber.e(it,"Cloud messaging registration error. Gave up")}
        )
    }
    fun firebaseToken()  =
            FirebaseInstanceId.getInstance().instanceId.toObservable()
                    .map { it.token }
                    .doOnNext {
                        Timber.d("Firebase token: $it")
                        preferencesRepository.firebaseToken = it
                    }

    fun getTags(): Observable<String> =
        lazyUserService.get().tags
                .map { it.tags?.joinToString(",") ?: ""}
                .doOnNext {
                    Timber.d("tags: $it")
                    preferencesRepository.tags = it
                }


    fun registerAzure(firebaseToken: String, tags: String ): Observable<String> =
        Observable.fromCallable {
//            try {
//                Timber.d("gcm senderId: " +context.getString(R.string.gcm_defaultSenderId) );
//            } catch (e: Exception ) {
//                Timber.d(e, "Oh noes");
//            }
//            NotificationsManager.handleNotifications(context, context.getString(R.string.gcm_defaultSenderId), AzureNotificationsHandler::class.java)
            try {

                val endpoint = preferencesRepository.azureEndpoint ?: context.getString(R.string.azure_connection_string_previous)
                if (endpoint != context.getString(R.string.azure_connection_string)) {
                    Timber.d("going to unregister azure previous")

                    AzureNotificationHub(context.getString(R.string.azure_notification_hub_path), endpoint, context).unregister()
                    Timber.d("unregistered azure")
                    preferencesRepository.azureEndpoint = context.getString(R.string.azure_connection_string)
                }

            } catch (e: Exception) {
                Timber.w(e, "Error unregistering azure previous");
            }
            try {

                Timber.d("going to unregister azure");
                AzureNotificationHub(context.getString(R.string.azure_notification_hub_path),
                        context.getString(R.string.azure_connection_string), context).unregister()
                Timber.d("unregistered azure")
            } catch (e: Exception) {
                Timber.w(e, "Error unregistering azure");
            }

            AzureNotificationHub(context.getString(R.string.azure_notification_hub_path),
                    context.getString(R.string.azure_connection_string), context).register(firebaseToken, tags).getRegistrationId();
        }
        .doOnNext {
            Timber.d("Azure registered successfully id: $it" );
            preferencesRepository.azureId = it
        }
}