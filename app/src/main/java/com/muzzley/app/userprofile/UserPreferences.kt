package com.muzzley.app.userprofile

import android.content.Context
import android.text.format.DateFormat
import com.muzzley.model.Preferences
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.retrofit.UserService
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

import javax.inject.Inject
import java.text.NumberFormat
import java.util.*

class UserPreferences

    @Inject constructor(
            val preferencesRepository: PreferencesRepository,
            val userService: UserService,
            val context: Context
    ){


    //updates already existing preferences, on regular query
    fun update(preferences: Preferences){
        preferences.also {
            if (it != preferencesRepository.preferences) {
                setLocalDefaults(it)
                preferencesRepository.preferences = it

                userService.updatePreferences(it)
                        .subscribeOn(Schedulers.io())
                        .subscribe(
                        {
                            Timber.d("Sent new prefs async "+it)
                        },
                        { Timber.d(it,"Error sending new prefs async")}
                )

            }
        }

    }


    fun updatePreferences() {
        if (preferencesRepository.preferences == null) {
            val preferences = Preferences()
            setLocalDefaults(preferences)
            preferencesRepository.preferences = preferences
        }

    }

    fun setLocalDefaults(preferences: Preferences) {
        preferences.apply{
            setMetric(! listOf("US", "LR", "MM").contains(Locale.getDefault().getCountry()));
            is24hours = DateFormat.is24HourFormat(context)
            timezone = TimeZone.getDefault().getID()
            Locale.getDefault().let {
                locale = it.toString()
                language = it.language
                currency = NumberFormat.getCurrencyInstance(it).currency.currencyCode
            }
        }
    }

}