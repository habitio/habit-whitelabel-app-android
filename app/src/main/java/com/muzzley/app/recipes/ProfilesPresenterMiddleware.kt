package com.muzzley.app.recipes

import android.app.Activity
import com.muzzley.app.profiles.ProfilesIView
import com.muzzley.app.profiles.RecipeNavigator
import com.muzzley.model.profiles.RecipeAction
import io.reactivex.disposables.Disposable
import timber.log.Timber

class ProfilesPresenterMiddleware(
        val sm: StateMachineStore,
        var recipeView: ProfilesIView?,
        val recipeNavigator: RecipeNavigator
) {
    var activity: Activity? = null
    private var disposable: Disposable? = null

    fun cancel() =
            disposable?.dispose()

    fun run() {
        disposable = sm.listenState()
                .subscribe(
                        { state ->

                            when (state) {
                                //TODO: recipeError should accept action in order to enable some retry button
                                is State.Error -> recipeView?.showError(state.throwable,false)
                                is State.FetchingMeta -> recipeView?.showLoading(true)

                                //TODO: find something better to display
                                is State.Step -> {
                                    // show some step info, like the step number
                                    // or some info saying "Sending udp"
                                    recipeView?.showLoading(false)
                                    when (state.action.recipeState.action) {
                                        null -> sm.onAction(Action.OnResult(state.action.recipeState)) // empty result
                                        RecipeAction.oauth -> {
                                            if (state.action.recipeState.result?.response?.payload?.urlRequest == null &&
                                                    state.action.recipeState.result?.response?.payload?.authorization_url != null) {
                                                sm.onAction(Action.QueryAction(state.action.recipeState))
                                            } else {
                                                recipeNavigator.navigateTo(activity!!,state)
                                            }
                                        }
                                        RecipeAction.udp -> {
//                                        Session.put("aaa",sm)
//                                        should call view
//                                        recipeView.handleState(state)
                                            recipeNavigator.navigateTo(activity!!,state)
//                                            StateMachineFactory.stateMachineStore = sm
//                                            activity?.startActivity<RecipeCardsActivity>()
//                                                    ?: Timber.d("null activity !")
                                        }
//                                        RecipeAction.oauth ->
                                        else -> sm.onAction(Action.QueryAction(state.action.recipeState))
                                    }

//                                    recipeNavigator.navigateTo(activity!!, sm)
//                                    state.action.recipeState.let {
//                                        recipeNavigator.navigateTo(recipeView as Activity, it)
//                                    }
//                                    recipeNavigator.navigateTo(recipeView as Activity, state.action.recipeState)
//                                    recipeView.showLoading(false)
//                                    state.action.recipeState.result?.response?.payload?.action?.name?.let {
//                                        recipeView.showStepTitle(it)
//                                    }
                                }
                                is State.FetchingResult -> recipeView?.showLoading(true)
                                is State.Result -> recipeView?.showLoading(false)
                                is State.SendingRequest -> recipeView?.showLoading(true)
                            }
                        },
                        {
                            Timber.e(it, "StateMachineError")
                        }
                )

    }
}
