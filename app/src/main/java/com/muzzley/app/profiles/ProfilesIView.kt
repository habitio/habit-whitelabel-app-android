package com.muzzley.app.profiles

import androidx.core.util.Pair

import com.muzzley.model.profiles.ProfilesData
import com.muzzley.model.profiles.Bundles
import com.muzzley.model.profiles.RecipeState

interface ProfilesIView : LoadingAndErrorView {
    fun showData(bundlesProfilesDataPair: Pair<Bundles, ProfilesData>)
    fun showBundleState(state: BundleFlow.BundleState)

    fun executeRecipeAction(recipeResult: RecipeState)
}
