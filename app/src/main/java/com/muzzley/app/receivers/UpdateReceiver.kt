package com.muzzley.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.muzzley.App
import com.muzzley.R
import com.muzzley.app.notifications.NotificationsInteractor
import com.muzzley.services.PreferencesRepository
import timber.log.Timber
import javax.inject.Inject

class UpdateReceiver : BroadcastReceiver() {

    @Inject lateinit var preferencesRepository: PreferencesRepository


    override fun onReceive(context: Context, intent: Intent?) {

        Timber.d("onReceive update")

        App.appComponent.inject(this)

        if (preferencesRepository.azureEndpoint == null) {
            preferencesRepository.azureEndpoint = context.getString(R.string.azure_connection_string_previous)
        }


    }
}
