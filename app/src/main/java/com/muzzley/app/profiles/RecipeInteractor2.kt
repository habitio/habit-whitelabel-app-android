package com.muzzley.app.profiles

import com.muzzley.model.profiles.Profile
import com.muzzley.model.profiles.Process
import com.muzzley.model.profiles.RecipeResult
import com.muzzley.model.profiles.RecipeState
import com.muzzley.services.CustomServicesModule
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.retrofit.ChannelService
import io.reactivex.Observable
import timber.log.Timber
import javax.inject.Inject

class RecipeInteractor2

    @Inject constructor(
            val channelService: ChannelService,
            val preferencesRepository: PreferencesRepository
    ){


    fun startRecipe(profile: Profile, process: Process): Observable<RecipeState>  =
        channelService.getRecipeMeta(profile.recipe)
            .map { RecipeState(result = RecipeResult().apply { next_step = it.next_step }, profile = profile, process = process) }
            .flatMap { nextStep(it)}


    fun unwrapVariables(recipeState: RecipeState): Map<String, Any>  {
        val map = HashMap<String,Any>()
        val w = recipeState.result?.next_step?.meta?.variables ?: mapOf()
//        w.forEach { path, attributes ->
        for((path, attributes) in w ) {
            val pathElements = path.split(".")
//            Timber.d("yyy: $pathElements")
            var curr: HashMap<String,Any> = map
            for ( i in 0 until pathElements.size - 1) {
//                Timber.d("yyy: $i")
                if (curr[pathElements[i]] == null ) {
                    curr[pathElements[i]] = HashMap<String, Any>()
                }
                curr = curr[pathElements[i]] as HashMap<String,Any>
            }

            val key = pathElements.last()
            val value = when (key) {
                "process" -> recipeState.process?.name
                "channeltemplate_id" -> recipeState.profile?.id
                "client_id" -> preferencesRepository.appClientId
                "Authorization" -> CustomServicesModule.getOAuthHeaderValue(preferencesRepository)
                "owner_id" -> preferencesRepository.user?.id
                "devices" -> recipeState.localResults
//                "devices" -> recipeState.result?.response?.payload?.devices
                else -> null
            }
            if (value != null) {
                curr[key] = value
            } else {
                if (attributes.contains("required")) {
                    throw NoSuchElementException("$key not found")
                }
            }
        }
        return map
    }

    fun nextStep(recipeState: RecipeState ): Observable<RecipeState> =

        Observable.fromCallable {
            unwrapVariables(recipeState)
        }
        .flatMap {
            channelService.executeRecipe(recipeState.result?.next_step?.href,
                    it["headers"] as? Map<String,String>,
                    it["payload"] as? Map<String,String>)
        }
        .map { RecipeState(profile = recipeState.profile, process = recipeState.process, result = it) }

}