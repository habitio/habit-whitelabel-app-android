package com.muzzley.app.recipes

import com.google.gson.JsonElement
import com.muzzley.app.profiles.RecipeInteractor
import com.muzzley.model.discovery.NetworkInfo
import com.muzzley.model.discovery.Param
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.util.parseJson
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxComposers
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import timber.log.Timber

class RecipeLocalMiddleware(
    val sm : StateMachineStore,
    var channelService: ChannelService,
    var recipeInteractor: RecipeInteractor
){
    private var disposable: Disposable? = null

    fun cancel() =
            disposable?.dispose()

    fun run() {
        disposable = sm.listenState()
                .subscribe(
                        { state ->

                            when (state) {
                                is State.FetchingResult -> {

                                    state.action.recipeState.result?.response?.payload?.let { payload ->

                                        when (payload.action) {
                                            RecipeAction.udp -> {
                                                recipeInteractor.getActionResultString(
                                                        com.muzzley.model.discovery.Action(
                                                                null,
                                                                com.muzzley.model.discovery.Action.TYPE_NETWORK_INFO,
                                                                Param().apply {
                                                                    pInterface = "wifi";
                                                                    isBroadcast = true
                                                                }
                                                        )
                                                )
//                                                        .subscribeOn(Schedulers.io())
                                                        .flatMap {
                                                            val networkInfo = it.parseJson<NetworkInfo>()
                                                            val action = com.muzzley.model.discovery.Action(null, com.muzzley.model.discovery.Action.TYPE_UDP, Param().apply {
                                                                host = networkInfo!!.broadcast!!
                                                                port = payload.port!!
                                                                ttl = payload.ttl!!
                                                                isExpectResponse = payload.expect_response!!
                                                                data = payload.data
                                                            })
                                                            recipeInteractor.getActionResultString(action)
                                                                    .toList()
                                                                    .toObservable()
//                                                            val s = """
//                                                                        {
//                                                                            "device_id": "a85ca482-15b0-11e9-848f-000d3a264304",
//                                                                            "content": "Habit Door",
//                                                                            "id": "75ed6900-17f4-11e9-b292-000d3a264304",
//                                                                            "channel_template_id": "b144d40e-0ab4-11e9-b766-000d3a264304",
//                                                                            "photoUrl": "https://assets.landing.jobs/attachments/companies/logos/006a9267a637171c8c1a97b8cf86a43b0d5f96de/small.png?1530719538"
//                                                                        }
//                                                            """.trimIndent()
//                                                            val s2 = """
//                                                                        {
//                                                                            "device_id": "fake_device_id",
//                                                                            "content": "Habit Door2",
//                                                                            "id": "fake_id",
//                                                                            "channel_template_id": "fake_channel_template",
//                                                                            "photoUrl": "https://assets.landing.jobs/attachments/companies/logos/006a9267a637171c8c1a97b8cf86a43b0d5f96de/small.png?1530719538"
//                                                                        }
//                                                            """.trimIndent()
//                                                            Observable.just(listOf(s,s2))
//                                        .map { responses ->
//                                            val fibaroPackets = responses.filter{ it.contains("com.fibaro.")}
//
//                                            val results = fibaroPackets.map { toMap(it) }
//                                            Timber.d("results: $results")
//                                            results
//                                        }

                                                        }
//                                                        .observeOn(AndroidSchedulers.mainThread())
                                                        .compose(RxComposers.applyIo())
                                                        .subscribe(
                                                                {
                                                                    Timber.d("udp results: $it")
                                                                    val list = it?.map { it.parseJson<JsonElement>() }?.filterNotNull()
                                                                    if (list.isNullOrEmpty()) {
                                                                        sm.onAction(Action.OnError(Exception("empty results"),state.action))
                                                                    } else {
                                                                        state.action.recipeState.localResults = list
                                                                        sm.onAction(Action.OnResult(state.action.recipeState))
                                                                    }
                                                                },
                                                                {
                                                                    Timber.e(it, "Error sending udp")
//                                                                    sm.onAction(Action.OnError(it, Action.QueryAction(state.action.recipeState)))
                                                                    sm.onAction(Action.OnError(it, state.action))
                                                                }
                                                        )

                                            }
                                            RecipeAction.convert_for_show -> {
                                                if (payload.received_format == "fibaro") {
                                                    try {
                                                        //this could be a good place to filter non fibaro
                                                        var list = payload.devices!!.map {
                                                            val fibaroMap = it as Map<String, Any>
                                                            val sensor = fibaroMap["sensor"] as Map<String, String>
                                                            mapOf(
                                                                    "device_id" to fibaroMap["device_id"],
                                                                    "content" to sensor["name"],
                                                                    "id" to sensor["id"]
                                                            )
                                                        }
                                                        state.action.recipeState.localResults = list
                                                        sm.onAction(Action.OnResult(state.action.recipeState))
                                                    } catch (t: Throwable) {
                                                        Timber.e(t, "Error converting")
                                                        sm.onAction(Action.OnError(t, state.action))
                                                    }
                                                } else {
                                                    null
                                                }
                                            }
                                            // do nothing and move on to fill variables
                                            // has someone else handled it ?
                                            else -> null
                                        }
                                    } ?: Timber.d("No payload should not be fetching result")
                                    //execute old code
                                    //some will depend on views, like get auth code
                                    //that will be done in their own Interactor/Middleware

                                }

                            }
                        },
                        {
                            Timber.e(it, "StateMachineError")
                        }
                )
    }
}
