package com.muzzley.model.profiles

//todo: turn this into data class (with vals) and use .copy to make knew ones
class RecipeState (

        var profile: Profile? = null,
        var process: Process? = null,
        var result: RecipeResult? = null,

        var recipeId: String? = null,
        var localResults: List<Any>? = null,
        var variables: Map<String,Any>? = null

) {

    val action: RecipeAction?
        get() = result?.response?.payload?.action
}
