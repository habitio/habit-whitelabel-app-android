package com.muzzley.app.profiles

import android.app.Activity
import android.content.Intent
import com.muzzley.Constants
import com.muzzley.app.HomeActivity
import com.muzzley.app.auth.AuthActivity

class BundleNavigator {
    companion object {
        @JvmStatic
        fun navigateTo(activity: Activity, state: BundleFlow.BundleState ) {
            when (state) {
                BundleFlow.BundleState.auth -> activity.startActivityForResult(Intent(activity, AuthActivity::class.java), 46)
                BundleFlow.BundleState.tilesrefresh -> //copied from Navigator. DRY this
                    activity.startActivity(Intent(activity, HomeActivity::class.java)
                            .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                            .putExtra(Constants.EXTRA_UPDATE_CHANNELS, true)
                            .putExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS, Constants.Frag.channels))

                BundleFlow.BundleState.tutorial -> activity.startActivity(Intent(activity,TutorialActivity::class.java))
                BundleFlow.BundleState.profiles -> activity.startActivity(Intent(activity,BundleCardsActivity::class.java))
                BundleFlow.BundleState.summary ->  activity.startActivity(Intent(activity,SummaryActivity::class.java))

            }

        }
    }
}