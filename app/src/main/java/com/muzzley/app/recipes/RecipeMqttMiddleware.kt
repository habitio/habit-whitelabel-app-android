package com.muzzley.app.recipes

import com.muzzley.app.profiles.BundleInteractor
import com.muzzley.model.grants.GrantMessage
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.rx.RxComposers
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.lang.Exception

class RecipeMqttMiddleware(
        val sm: StateMachineStore,
        val realtime: Realtime,
        val preferencesRepository: PreferencesRepository,
        val bundleInteractor: BundleInteractor
) {
    private var disposable: Disposable? = null

    fun cancel() =
            disposable?.dispose()

    fun run() {
        disposable = sm.listenState()
                .subscribe { state ->

                    when (state) {
                        is State.FetchingResult -> try {
                            Timber.d("YYYYYYYYYYY")
                            state.action.recipeState.result?.response?.payload?.let { payload ->
                                Timber.d("payload not null")
                                when (payload.action) {
                                    RecipeAction.grant_access_app -> {
                                        payload.devices?.let {
                                            val devicesMap = it as List<Map<String, Any>>

                                            val grants = devicesMap.map { deviceMap ->
                                                bundleInteractor.askApplicationGrant(
                                                        deviceMap["device_id"] as String,
                                                        deviceMap["content"] as String,
                                                        deviceMap["id"] as String
                                                )
                                            }
                                            Observable.concat(grants)
                                                    .compose(RxComposers.applyIo())
                                                    .subscribe(
                                                            {
                                                                Timber.d("now emit action")
                                                                sm.onAction(Action.OnResult(state.action.recipeState))
                                                            },
                                                            {
                                                                Timber.e(it)
                                                                sm.onAction(Action.OnError(it,state.action))
                                                            }
                                                    )
                                        } ?: Timber.d("No devices !") // todo: should be an error
                                    }
                                    RecipeAction.grant_access_user -> {
                                        Timber.d("XXXXXXXXXXXXX")
//                                        sm.onAction(Action.OnError(Exception("test exception"),state.action))
                                        payload.devices?.let {
                                            Timber.d("AAAAAAAAAAAAAAAA")
                                            val devicesMap = it as List<Map<String, Any>>
                                            Timber.d("BBBBBBBBBBBBBBBB")
                                            val grants = devicesMap.map { deviceMap ->
                                                bundleInteractor.askUserGrant(
                                                        deviceMap["device_id"] as String,
                                                        deviceMap["content"] as String,
                                                        deviceMap["id"] as String

                                                )
                                            }
                                            Timber.d("CCCCCCCCCCCCCCCC")
                                            Observable.concat(grants)
                                                    .compose(RxComposers.applyIo())
                                                    .subscribe(
                                                            {
                                                                Timber.d("now emit action")
                                                                sm.onAction(
                                                                        Action.OnResult(
                                                                                state.action.recipeState.apply {
                                                                                    localResults = payload.devices
                                                                                }
                                                                        )
                                                                )
                                                            },
                                                            {
                                                                Timber.e(it)
                                                                sm.onAction(Action.OnError(it,state.action))
                                                            }

                                                    )
                                        }
                                    }
                                    else -> null
                                }
                            } ?: Timber.d("No payload should not be fetching result")
                        } catch (t: Throwable) {
                            Timber.e(t)
                        }
                    }
                }
    }

}