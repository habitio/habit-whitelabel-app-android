package com.muzzley.app.recipes

import android.app.Activity
import com.muzzley.app.profiles.ProfilesInteractor
import com.muzzley.app.profiles.RecipeInteractor2
import com.muzzley.app.profiles.RecipeNavigator
import com.muzzley.model.Subscription
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.model.profiles.RecipeResult
import com.muzzley.model.profiles.RecipeState
import com.muzzley.util.parseJson
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.toJsonString
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RecipeApiMiddleware(
        private val sm: StateMachineStore,
        private val channelService: ChannelService,
        private val recipeInteractor2: RecipeInteractor2,
        private val profilesInteractor: ProfilesInteractor,
        private val recipeNavigator: RecipeNavigator,
        private val activity: Activity?

) {
    private var disposable: Disposable? = null

    fun cancel() =
            disposable?.dispose()

    fun run() {
        disposable = sm.listenState()
                .subscribe(
                        { state ->

                            when (state) {
                                is State.FetchingMeta -> channelService.getRecipeMeta(state.action.recipeState.recipeId)
                                        .compose(RxComposers.applyIo())
                                        .subscribe(
                                                {
                                                    if (validAction(it)) {
                                                        state.action.recipeState.result = it
                                                        sm.onAction(Action.OnStep(state.action.recipeState))
                                                    } else {
                                                        sm.onAction(Action.OnError(Exception("Unknown action version"), null))
                                                    }
                                                },
                                                { sm.onAction(Action.OnError(it, state.action)) }
                                        )
                                is State.Result -> {
                                    if (state.action.recipeState.result?.next_step == null) {
                                        if (state.action.recipeState.action == RecipeAction.send_selected_devices){
                                            // this post gets info from wherever setSelectedIndex left it
                                            state.action.recipeState.localResults?.mapNotNull {
                                                val devicesMap = it as Map<String, Any>
                                                Subscription.Channel(
                                                        devicesMap["id"] as String,
                                                        devicesMap["content"] as? String,
                                                        null
                                                )
                                            }?.let {
                                                profilesInteractor.postSubscription(Subscription(state.action.recipeState.profile!!.id, it))
                                                        .delay(10, TimeUnit.SECONDS)
                                                        .compose(RxComposers.applyIo())
                                                        .subscribe(
                                                                {
                                                                    sm.onAction(Action.Finish(state.action.recipeState))
                                                                },
                                                                {
                                                                    sm.onAction(Action.OnError(it,state.action))
                                                                }
                                                        )
                                            } ?: sm.onAction(Action.OnError(Exception("Empty devices"),state.action))
                                        } else {
                                            sm.onAction(Action.Finish(state.action.recipeState))
                                        }
                                    } else {
                                        try {
                                            state.action.recipeState.variables = recipeInteractor2.unwrapVariables(state.action.recipeState)
                                            sm.onAction(Action.OnRequest(state.action.recipeState))
                                        } catch (t: Throwable) {
                                            sm.onAction(Action.OnError(t, null))
                                        }
                                    }
                                }
                                is State.FetchingResult -> {
                                    (state.action.recipeState.result?.response?.payload)?.also { payload ->
                                        if (payload.action == RecipeAction.oauth && payload.urlRequest == null) {
                                            payload.authorization_url?.let { aurl ->
                                                profilesInteractor.getRecipeAuthorizationUrlRequest(aurl)
                                                        .compose(RxComposers.applyIo())
                                                        .subscribe(
                                                                {
                                                                    payload.urlRequest = it
                                                                    //todo: we should remove this ftom here, use an interface and delegate to it
                                                                    // or send OnResult and have activity handle navigation in State.Result
                                                                    activity?.let {
                                                                        recipeNavigator.navigateTo(it, state)
                                                                    }
                                                                            ?: sm.onAction(Action.OnError(Exception("null activity"), null))

//                                                                sm.onAction(Action.OnResult(state.action.recipeState))
//                                                                recipeNavigator.navigateTo(this, recipeState)
                                                                },
                                                                {
                                                                    sm.onAction(Action.OnError(it, state.action))
//                                                                showError(it,false)
                                                                }
                                                        )
                                            } //?: showError(RuntimeException("Missing authorization_url"))
                                        } else if (payload.action == RecipeAction.list_devices) {
                                            channelService.getProfileChannels(state.action.recipeState.profile!!.id)
                                                    .compose(RxComposers.applyIo())
                                                    .subscribe({
                                                        sm.onAction(Action.OnResult(state.action.recipeState.apply { localResults = it.toJsonString().parseJson() }))

                                                        // put it in
//                                                        updateCard(it)
                                                    }, {
                                                        Timber.e(it, "Error getting profileChannels")
                                                        sm.onAction(Action.OnError(it, state.action))
                                                    }
                                                    )
                                        } else if (payload.action == RecipeAction.send_selected_devices) {
                                            Timber.d("sending devices")
                                        }
                                    }
                                }
                                is State.Request -> {
                                    sm.onAction(Action.SendRequest(state.action.recipeState))
                                }
                                is State.SendingRequest -> with(state.action.recipeState) {
                                    channelService.executeRecipe(result?.next_step?.href,
                                            variables!!["headers"] as? Map<String, Any>,
                                            variables!!["payload"] as? Map<String, Any>)
                                            .map { RecipeState(profile = this.profile, process = this.process, result = it) }
                                            .compose(RxComposers.applyIo())
                                            .subscribe(
                                                    {
                                                        if (validAction(it.result!!)) {
                                                            sm.onAction(Action.OnStep(it))
                                                        } else {
                                                            sm.onAction(Action.OnError(Exception("Unknown action version"), null))
                                                        }
                                                    },
                                                    {
                                                        sm.onAction(Action.OnError(it, state.action))
                                                    }
                                            )
                                }
                            }
                        },
                        {
                            Timber.e(it, "StateMachineError")
                        }
                )
    }

    fun validAction(it: RecipeResult) =
            (it.response?.payload)?.run {
                action == null || action_version.isNullOrEmpty() || action_version == "v1"
            } ?: true

}
