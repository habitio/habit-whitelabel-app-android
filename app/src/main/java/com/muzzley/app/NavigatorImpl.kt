package com.muzzley.app

import android.content.Context
import android.content.Intent
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.app.login.GetStartedActivity

class NavigatorImpl(private val context: Context): Navigator {
    override fun gotoInterface() =
        Intent(context, InterfaceActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

    override fun newGetStartedIntent(flags: Int) =
        Intent(context, GetStartedActivity::class.java).setFlags(flags)

    override fun newHomeIntent(flags: Int) =
        Intent(context, HomeActivity::class.java).setFlags(flags)


    override fun newTilesWithRefresh() =
            Intent(context, HomeActivity::class.java)
                    .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .putExtra(Constants.EXTRA_UPDATE_CHANNELS, true)
                    .putExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS, Constants.Frag.channels)
}