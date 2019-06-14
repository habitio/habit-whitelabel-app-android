package com.muzzley.app.profiles

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.recipes.*
import com.muzzley.model.channels.Channel
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.model.profiles.RecipeCancelType
import com.muzzley.model.profiles.RecipeState
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.retrofit.ChannelService
import io.reactivex.disposables.Disposable
import timber.log.Timber
import javax.inject.Inject
import com.muzzley.model.profiles.Process

class RecipePresenter

    @Inject constructor(
            val recipeNavigator: RecipeNavigator,
            val recipeInteractor: RecipeInteractor,
            val recipeInteractor2: RecipeInteractor2,
            val channelService: ChannelService,
            val realtime: Realtime,
            val preferencesRepository: PreferencesRepository,
            val bundleInteractor: BundleInteractor,
            val navigator: Navigator,
            val profilesInteractor: ProfilesInteractor

            ) {

    var view: Any? = null

//    val sm = StateMachineFactory.stateMachineStore
    val sm = StateMachineStore(recipeNavigator.state!!)
//    val recipeApiMiddleware = RecipeApiMiddleware(sm, channelService, recipeInteractor2)
//    val recipeLocalMiddleware = RecipeLocalMiddleware(sm,channelService,recipeInteractor)
//    val recipeCardsMiddleware = RecipeCardsMiddleware(sm, this)
//    val recipeMqttMiddleware = RecipeMqttMiddleware(sm,realtime,preferencesRepository,bundleInteractor)
    var recipeApiMiddleware : RecipeApiMiddleware? = null
    var recipeLocalMiddleware : RecipeLocalMiddleware? = null
    var recipeCardsMiddleware : RecipeCardsMiddleware? = null
    var recipeMqttMiddleware : RecipeMqttMiddleware? = null


    private var activity: AppCompatActivity? = null

    fun attachView(view: Any) {
        this.view = view
        recipeApiMiddleware = RecipeApiMiddleware(sm, channelService, recipeInteractor2,profilesInteractor,recipeNavigator,activity)
        recipeLocalMiddleware = RecipeLocalMiddleware(sm,channelService,recipeInteractor)
        recipeCardsMiddleware = RecipeCardsMiddleware(sm, this)
        recipeMqttMiddleware = RecipeMqttMiddleware(sm,realtime,preferencesRepository,bundleInteractor)
        recipeApiMiddleware?.run()
        recipeLocalMiddleware?.run()
        recipeCardsMiddleware?.run()
        recipeMqttMiddleware?.run()
    }

    fun dettachView() {
        recipeApiMiddleware?.cancel()
        recipeLocalMiddleware?.cancel()
        recipeCardsMiddleware?.cancel()
        recipeMqttMiddleware?.cancel()
    }

    fun setActivity(activity: AppCompatActivity) {
        this.activity = activity

        val payload = recipeNavigator.recipeState?.result?.response?.payload
        activity.getSupportActionBar()?.apply{
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)

            if (payload?.navigation_bar_title != null) {
                setTitle(payload.navigation_bar_title)
            }
            if  (payload?.cancel_type != RecipeCancelType.back) {
                 setHomeAsUpIndicator(R.drawable.ic_action_clear)
            }
        }
    }

    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> { activity?.onBackPressed() ; true }
            else -> true
        }

    fun sendSelectedDevices(recipeState: RecipeState, selectedDevices: List<Any>?) {
        //todo: it would be better if this was immutable and return a copy
        recipeState.localResults = selectedDevices
        sm.onAction(Action.OnResult(recipeState))
    }
    fun sendSelectedDevicesIndex(selectedDevices: List<Int>?) {
        //todo: it would be better if this was immutable and return a copy
        //assume fetching result
//        val recipeState = (sm.listenState().value as State.FetchingResult).action.recipeState
        val state = sm.listenState().value
        val recipeState = when(state) {
            is State.FetchingResult -> state.action.recipeState
            is State.Error -> when(state.action) {
                is Action.QueryAction -> state.action.recipeState
                is Action.SendRequest -> state.action.recipeState
                else -> null
            }
            else -> null
        }

        recipeState?.localResults = selectedDevices?.mapNotNull {
            recipeState?.result?.response?.payload?.devices?.getOrNull(it)
        }
        if (recipeState != null) {
            sm.onAction(Action.OnResult(recipeState))
//            sm.onAction(Action.QueryAction(recipeState))
        } else {
            sm.onAction(Action.OnError(Exception("no recipeState"),null))
        }
    }

}

class RecipeCardsMiddleware(val sm: StateMachineStore, val presenter: RecipePresenter) { //todo: move this to interface
    private var disposable: Disposable? = null
    private val recipeCardsActivity
            get() = presenter.view as RecipeCardsActivity? //todo: this won't be true to old recipes

    fun cancel() =
            disposable?.dispose()

    fun run(){
        disposable = sm.listenState().subscribe(
                { state ->
                    when(state) {
                        //todo: Should this be here ? whose responsibility is it ?
                        is State.Step -> {
                            if (state.action.recipeState.action != null) {
                                sm.onAction(Action.QueryAction(state.action.recipeState))
                            } else {
                                sm.onAction(Action.OnResult(state.action.recipeState)) // empty result
                            }
                        }
                        is State.FetchingResult -> {
                            when (state.action.recipeState.action) {
                                //should be called show,select and send
                                RecipeAction.show_devices -> {
                                    Timber.d("show_devices")
                                    updateCard(state.action.recipeState)
                                }
                                RecipeAction.udp,
                                RecipeAction.grant_access_user,
                                RecipeAction.grant_access_app -> recipeCardsActivity?.showLoading(true)
                                // other actions not handled here should navigate away. Or, it should be handled in the on step
                                else -> Timber.d("not processing $state")
                            }
                        }
                        is State.Result -> {
                            recipeCardsActivity?.showLoading(false)
                            when (state.action.recipeState.action) {
                                //todp: this is very similar to previous one should we not process it on the same state ?
                                RecipeAction.list_devices -> {
                                    Timber.d("list_devices")
                                    val channels = state.action.recipeState.localResults?.map {
                                        val m = it as Map<String, Any>
                                        Channel().apply {
                                            content = m["content"] as? String
                                            photoUrl = m["photoUrl"] as? String
                                        }
                                    }
                                    //set view with channels
                                    recipeCardsActivity?.updateCard(channels!!)
//                                    updateCard(state.action.recipeState)
                                }

                            }

                        }
                        is State.Finished -> {
                            val activity = presenter.view as Activity
                            if (state.action.recipeState.process == Process.mz_wl_add_device_process) {
                                activity.startActivity(presenter.navigator.newTilesWithRefresh())
                            } else {
                                activity.finish()
                            }
                        }
                        is State.Error -> {
                            recipeCardsActivity?.showError(state)
                        }

                    }

                }, Timber::e
//                {
//                }
        )
    }

    private fun updateCard(recipeState: RecipeState) {
        val channels = recipeState.result?.response?.payload?.devices?.map {
            val m = it as Map<String, Any>
            Channel().apply {
                content = m["content"] as String
                photoUrl = m["photoUrl"] as? String
            }
        }
        //set view with channels
        recipeCardsActivity?.updateCard(channels!!)
    }
}
