package com.muzzley.app.recipes

import io.reactivex.Observable

interface RecipeView {
    fun getAuthenticationCode(): Observable<String>
    fun recipeSuccess(): Unit
    fun recipeError(throwable: Throwable): Unit
    fun showLoading(b: Boolean): Unit
    fun showTotalSteps(totalSteps: Int): Unit
    fun showStepNumber(stepNo: Int): Unit
    fun showStepTitle(title: String): Unit
}
