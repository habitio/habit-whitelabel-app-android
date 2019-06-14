package com.muzzley.app.notifications

/**
 * Created by caan on 13-09-2016.
 */

import com.google.firebase.messaging.FirebaseMessagingService
import com.muzzley.App

import javax.inject.Inject

import timber.log.Timber

class MyFirebaseMessagingService : FirebaseMessagingService() {


    /**
     * Called when the system determines that the tokens need to be refreshed. The application should call getToken() and send the tokens to all application servers.
     *
     * This will not be called very frequently, it is needed for key rotation and to handle Instance ID changes due to:
     *
     * App deletes Instance ID
     * App is restored on a new device
     * User uninstalls/reinstall the app
     * User clears app data
     */

    @Inject
    lateinit var notificationsInteractor: NotificationsInteractor

    override fun onNewToken(s: String?) {
        super.onNewToken(s)
        Timber.d("Refreshing Firebase Token")

        App.appComponent.inject(this)

        notificationsInteractor.register()
    }

}