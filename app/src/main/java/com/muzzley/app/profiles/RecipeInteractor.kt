package com.muzzley.app.profiles

import android.content.Context
import android.net.ConnectivityManager
import com.muzzley.R
import com.muzzley.app.recipes.RecipeView
import com.muzzley.model.discovery.*
import com.muzzley.services.NetworkInfoService
import com.muzzley.services.UPnPService
import com.muzzley.services.UdpService
import com.muzzley.util.okhttp.HttpRequest
import com.muzzley.util.parseJson
import com.muzzley.util.rx.RxComposers
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Named

class RecipeInteractor 

    @Inject constructor(
            val context: Context,
            val httpRequest: HttpRequest,
            val uPnPService: UPnPService,
            val networkInfoService: NetworkInfoService,
            val udpService: UdpService,
            @Named("main") val mainScheduler: Scheduler,
            @Named("io")   val ioScheduler: Scheduler
    ){

    var view: RecipeView? = null


    fun startRecipe(url: String) =
        getInit(url) // ex: http://belkin-wemo-manager.office.muzzley.com/discovery/1000569
                .compose(RxComposers.applyIo())
                .doOnNext { view?.showTotalSteps(it.steps) }
                .observeOn(ioScheduler)
                .flatMap {
                    if (it.connectionType == Init.CONNECTION_TYPE_WIFI && !isConnected(ConnectivityManager.TYPE_WIFI)) {
                        throw RuntimeException(context.getString(R.string.wifi_not_connected))
                    } else if (it.connectionType == Init.CONNECTION_TYPE_CELL && !isConnected(ConnectivityManager.TYPE_MOBILE)) {
                        throw RuntimeException(context.getString(R.string.cell_not_connected))
                    }
                    getStep(it.nextStepUrl)
                }
                .flatMap { recursiveSteps(it)}
                .observeOn(mainScheduler)
                .ignoreElements()
                .subscribe(
                    { view?.recipeSuccess()},
                    { view?.recipeError(it)}
                )



    fun recursiveSteps(step: Step ): Observable<Step>  {
        val obs = Observable.just(step).observeOn(mainScheduler)
        return if (step == NStep)
            obs
        else obs
                .doOnNext { view?.showStepNumber(it.step); view?.showStepTitle(it.title ?: "") }
                .observeOn(ioScheduler)
                .flatMap { getResults(step)}
                .observeOn(ioScheduler) // because previous step could have switched to main thread in case of UI
                .flatMap { sendResults(step, it)}
                .flatMap { recursiveSteps(it)}
    }


    fun getResults(step: Step): Observable<List<Result>> =
        Observable.fromIterable(step.actions)
                .flatMap { action ->
                        getActionResultString(action)
                                .map { Result(action.id, it) }
                                .defaultIfEmpty(Result(action.id, null  ))
                }.toList().toObservable()



    fun getActionResultString(action: Action): Observable<String> =
        when (action.type) {
            Action.TYPE_UPNP -> Observable.fromIterable(uPnPService.discover(action.params))
            Action.TYPE_UDP -> Observable.fromIterable(udpService.sendPacket(action.params).distinct()) //FIXME: unique because fibaro has bug
            Action.TYPE_HTTP ->
                when(action.params.method) {
                    "GET" -> httpRequest.getResponseString(action.params.url)
                    "POST" -> httpRequest.doXmlPostRequest(action.params.url, action.params.body, action.params.headersMap)
                    else -> throw RuntimeException("${action.type} ${action.params.method} not implemented yet")
                }
//            Action.TYPE_NETWORK_INFO -> Observable.just(networkInfoService.getInfo(action.params)).subscribeOn(Schedulers.io())
            Action.TYPE_NETWORK_INFO -> Observable.fromCallable { networkInfoService.getInfo(action.params) }
            Action.TYPE_ACTIVATION_CODE -> view?.getAuthenticationCode() ?: Observable.empty()
            else -> throw RuntimeException("${action.type} not implemented yet")
        }

    fun sendResults(step:Step, results: List<Result>): Observable<Step> =
        httpRequest.doPostRequest(step.resultUrl, ActionsResults(results)).flatMap {
            when {
                it.code() == 201 -> Observable.just(NStep) // because stupid rxjava2 does not allow nulls to be emitted
                it.isSuccessful -> {
                    val body = it.body()?.string()
                    Observable.just(body.parseJson())
                }
                else -> Observable.error(RuntimeException("Error code: ${it.code()} when posting results to ${step.resultUrl}"))
            }
        }

    fun getInit(url: String ): Observable<Init> =
        httpRequest.getResponse(url, Init::class.java)

    fun getStep(url: String): Observable<Step> =
        httpRequest.getResponse(url, Step::class.java)

    fun isConnected(connectionType: Int): Boolean  {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = manager.getNetworkInfo(connectionType);

        return networkInfo.isConnected();
    }
}