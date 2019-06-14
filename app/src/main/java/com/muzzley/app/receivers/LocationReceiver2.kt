package com.muzzley.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import com.muzzley.App
import com.muzzley.services.LocationInteractor

import javax.inject.Inject

class LocationReceiver2 : BroadcastReceiver() {

    @Inject lateinit var locationInteractor: LocationInteractor

    override fun onReceive(context: Context, intent: Intent) {
        App.appComponent.inject(this)
        locationInteractor.handleIntent(intent)
    }
}
