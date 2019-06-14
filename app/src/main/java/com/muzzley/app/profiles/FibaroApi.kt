package com.muzzley.app.profiles

import com.muzzley.model.discovery.Action
import com.muzzley.model.discovery.Param
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.parseJson
import io.reactivex.Observable
import io.reactivex.Scheduler
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class FibaroApi 
    @Inject constructor(
            @Named("io") var ioScheduler: Scheduler,
            var realtime: Realtime,
            var preferencesRepository: PreferencesRepository,
            var recipeInteractor: RecipeInteractor
    ) {

    fun discoverLocal(payload: String): Observable<List<Map<String,Any>>> =
//                Observable.just(new UrlRequest(url: "muzdiscovery://cdn.muzzley.com/things/white-label-assets/vodafone/discoveryProcess_mock.json"))
        Observable.just( Action(null,Action.TYPE_NETWORK_INFO, Param().apply { pInterface = "wifi";  isBroadcast = true }))
                .observeOn(ioScheduler)
                .flatMap { recipeInteractor.getActionResultString(it) }
                .map { toMap(it).get("broadcast") as String }
                .flatMap {
                    val action = Action(null,Action.TYPE_UDP,Param().apply {
                        host = it
                        port = 44445
                        ttl = 3000
                        isExpectResponse = true
                        data = payload
                    })
                    recipeInteractor.getActionResultString(action)
                            .toList()
                            .toObservable()
                            .map { responses ->
                                val fibaroPackets = responses.filter{ it.contains("com.fibaro.")}

                                val results = fibaroPackets.map { toMap(it) }
                            Timber.d("results: $results")
                            results
                        }
                }

    fun toMap(s: String) = s.parseJson<Map<String,Any>>()!!

    fun discoverLocalSmoke(): Observable<List<Map<String,Any>>> =
        discoverLocal("X-FIBARO-GET com.fibaro.muzzleyCloud /v1/sensors/smoke")
    
    fun discoverLocalMotion(): Observable<List<Map<String,Any>>> =
        discoverLocal("X-FIBARO-GET com.fibaro.muzzleyCloud /v1/sensors/motion")

}