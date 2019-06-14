package com.muzzley.app.profiles

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Browser
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import com.muzzley.R
import com.muzzley.app.recipes.State
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.model.profiles.RecipeState;
import com.muzzley.util.startActivity
import saschpe.android.customtabs.CustomTabsHelper
import saschpe.android.customtabs.WebViewFallback
import timber.log.Timber

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeNavigator
    @Inject constructor(){


    var recipeState:RecipeState? = null
    var state: State? = null

    fun navigateTo(activity: Activity , state: State ) {
        this.state = state

        when(state) {
            is State.Step -> state.action.recipeState
            is State.FetchingResult -> state.action.recipeState
            else -> null
        }?.let {
            navigateTo(activity, it)
        } ?: Timber.e("Not navigating with ${state::class.java.simpleName}")


    }

    fun navigateTo(activity: Activity , recipeState: RecipeState ) {
        Timber.d("action: ${recipeState.action}")
        this.recipeState = recipeState
        when (recipeState.action) {
            RecipeAction.show_info -> activity.startActivity<RecipeShowInfoActivity>()
            RecipeAction.oauth -> {

                val customTabsIntent = CustomTabsIntent.Builder()
//                        .addDefaultShareMenuItem()
                        .setToolbarColor(ContextCompat.getColor(activity, R.color.colorPrimary))
                        .setShowTitle(true)
//                        .setCloseButtonIcon(backArrow)
                        .build()

                // This is optional but recommended
                CustomTabsHelper.addKeepAliveExtra(activity, customTabsIntent.intent);

                // This is where the magic happens...

                recipeState.result?.response?.payload?.urlRequest?.run {
                    headers?.let {
                        val bundle = Bundle()
//                        it.forEach { key, value ->
                        for ( (key, value) in it)
                            bundle.putString(key, value)

                        customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, bundle)
                    }
                    customTabsIntent.intent.flags = Intent.FLAG_ACTIVITY_NO_HISTORY
                    CustomTabsHelper.openCustomTab(activity, customTabsIntent, Uri.parse(this.url), WebViewFallback ());
                }
//                activity.startActivity(RecipeOAuthActivity)
            }
            RecipeAction.list_devices -> activity.startActivity<RecipeCardsActivity>()
            RecipeAction.udp -> activity.startActivity<RecipeCardsActivity>()
            else -> Timber.d("unexpected state ${recipeState.action}")
        }
    }

}