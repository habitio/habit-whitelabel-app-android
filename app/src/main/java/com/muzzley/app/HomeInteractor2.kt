package com.muzzley.app

import android.annotation.SuppressLint
import android.content.Context
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rxerr
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by caan on 28-02-2018.
 */
class HomeInteractor2

    @Inject constructor(
        val userService : UserService,
        val preferencesRepository: PreferencesRepository,
        val context : Context
    ){

    @SuppressLint("CheckResult")
    fun initChannelUserIdIfNeeded(){
        Timber.d("OOO kotlin home interactor !")
        if (preferencesRepository.userChannelId == null) {
            Timber.d("getting userChannelId")
            getUserChannelId()
                    .subscribe(
                            {
                                Timber.d("setting userChannelId: ${it.id}")
                                preferencesRepository.userChannelId = it.id
                            },
                            rxerr("Error updating userChannelId")
                    )
        } else {
            Timber.d("userChannelId already set: ${preferencesRepository.userChannelId}")
        }

    }

    fun getUserChannelId() =
            userService.getChannelData()
                    .map {
                        it.channels.find {
                            val type = it.components.find { it.classes.contains("com.muzzley.components.user.geo") }?.type
                            type != null && it._properties.any {
                                it.components.contains(type) && it.classes.contains( "com.muzzley.properties.location" )
                            }
                        } ?: throw Exception("Could not find Context channel")
                    }



}