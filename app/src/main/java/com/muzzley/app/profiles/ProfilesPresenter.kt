package com.muzzley.app.profiles

import android.app.Activity
import android.content.Context
import androidx.core.util.Pair
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.recipes.*
import com.muzzley.model.profiles.*
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxComposers
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import timber.log.Timber
import javax.inject.Inject

// The Presenter in MVP
class ProfilesPresenter
    @Inject constructor(
            val channelService: ChannelService,
            val analyticsTracker: AnalyticsTracker,
            var bundleFlow: BundleFlow,
            val bundleInteractor: BundleInteractor,
            val recipeInteractor2: RecipeInteractor2,
            val recipeInteractor: RecipeInteractor,
            val profilesInteractor: ProfilesInteractor,
            val recipeNavigator: RecipeNavigator
    ){

    private var view: ProfilesIView? = null  // view
    var disposable: CompositeDisposable? = null

//    val sm = StateMachineFactory.stateMachineStore
//    val sm = StateMachineStore(State.Idle)
//    val recipeApiMiddleware = RecipeApiMiddleware(sm, channelService, recipeInteractor2)
//    val recipeViewPresenter = ProfilesPresenterMiddleware(sm,view,recipeNavigator)
    var sm : StateMachineStore? = null
    var recipeApiMiddleware : RecipeApiMiddleware? = null
    var recipeViewPresenter : ProfilesPresenterMiddleware? = null

    fun attachView(v: ProfilesIView ) {
        view = v
        analyticsTracker.trackSimpleEvent(AnalyticsEvents.ADD_DEVICE_START_EVENT);
        disposable = CompositeDisposable()
        sm = StateMachineStore(State.Idle).also {
            recipeApiMiddleware = RecipeApiMiddleware(it, channelService, recipeInteractor2, profilesInteractor, recipeNavigator, v as? Activity).apply { run() }
            recipeViewPresenter = ProfilesPresenterMiddleware(it,view,recipeNavigator).apply {
                activity = v as Activity
                run()
            }
        }
    }

    fun dettachView() {
        view = null
        disposable?.dispose()
        recipeApiMiddleware?.cancel()
        recipeViewPresenter?.cancel()
    }

    fun addProfile(profile: Profile){
        analyticsTracker.trackDeviceAction(AnalyticsEvents.ADD_DEVICE_SELECT_DEVICE_EVENT, profile.id);
//        bundleFlow.onProfileClick(profile,view)

//        //FIXME: should we add foscam ?
//        def customLocalDiscoveries = [R.string.fibaro_smoke_profile_id,R.string.fibaro_motion_profile_id].collect { (view as Context).getString(it)}
//        if (profile.id in customLocalDiscoveries) {
//            showProfile(profile)
//        } else {
//            bundleFlow.getProfileAuthorizationUrlRequest(profile.id)
//                    .compose(RxComposers.<UrlRequest>applyIoRefresh(view.&showLoading))
//                    .subscribe(
//                    { showProfile(profile)},
//                    { view.showError(it,false)}
//            )
//        }


        profile.recipe?.let {
            sm?.onAction(Action.GetMeta(RecipeState(profile,Process.mz_wl_add_device_process,null,it)))
        } ?: showProfile(profile)


//        if (profile.recipe != null) {
//
//            view?.let { view ->
//                val d = recipeInteractor2.startRecipe(profile,Process.mz_wl_add_device_process)
//                        .compose(RxComposers.applyIoRefresh(view::showLoading)) //<RecipeState>
//                        .subscribe(
//                                {
//                                    view.executeRecipeAction(it)
//                                },
//                                {
//                                    view.showError(it,false)
//                                }
//                        )
//                disposable?.add(d)
//
//            }
//        } else { // legacy non recipe flow
//            showProfile(profile)
//        }

    }

    fun showProfile(profile: Profile){
        profile.occurrences = -1
        bundleFlow.bundle = Bundle(profiles = listOf(profile)) // fake bundle
        bundleFlow.currBundleState = BundleFlow.BundleState.profiles
        view?.showBundleState( BundleFlow.BundleState.profiles)
    }



    fun getRecipeAuthorizationUrlRequest(relativePath: String): Observable<UrlRequest> =
        profilesInteractor.getRecipeAuthorizationUrlRequest(relativePath)
//        channelService.getTemplateAuthorization(relativePath)
//                .onErrorReturn(profilesInteractor.&ignoreRedirectError as Function)
//                .map(profilesInteractor.&parseResponseHeaders)


    fun onBundleClick(bundle: Bundle){
        bundleFlow.currBundleState = null // make sure we start fresh
        bundleFlow.bundle = null

        analyticsTracker.trackDeviceAction(AnalyticsEvents.ADD_BUNDLE_SELECT_BUNDLE_EVENT, bundle.id);

        view?.let { view ->
        val d = Observable.fromIterable(bundle.profiles ?: listOf())
                .concatMap { profile ->
//                    channelService.getProfile(profile.id).map{ it.occurrences = profile.occurrences ; it }
                    profile.id?.run {
                        profilesInteractor.getProfile(this).map{ it.occurrences = profile.occurrences ; it }
                    } ?: Observable.error(IllegalArgumentException("no profile id"))
                }
                .toList().toObservable()
                .flatMap{
                    bundle.profiles = it
                    bundle.id?.run {
                        if (bundle.requiredCapability != null) {
                            bundleInteractor.getServiceBundleAuthorizationUrlRequest(this)
                        } else {
                            null
                        }
                    } ?: Observable.just(UrlRequest(""))
                }
                .compose(RxComposers.applyIoRefresh(view::showLoading))
                .subscribe(
                    {
                        Timber.d("auth url: ${it.url}")
                        bundle.authorizationUrl = it.url.takeIf { it != "" }
                        bundleFlow.bundle = bundle
                        view.showBundleState(bundleFlow.nextBundleState)
                    }, //FIXME: start the service
                    { view.showError(it,false) }
                )
        disposable?.add(d)
        }
    }


    fun getBundlesAndProfiles() = view?.let { view ->


//        Observable<ProfilesData> profilesDataObservable = channelService.getProfiles()
        val profilesDataObservable = profilesInteractor.getProfiles()
//        Observable<ProfilesData> profilesDataObservable = jsonBlob.getProfiles()
        val  bundlesObservable = channelService.serviceBundles

//        val  bundlesObservable = if (getContext().getString(R.string.app_namespace).contains("vodafone")) {
////            bundlesObservable = jsonBlob.serviceBundles
//            cdnService.serviceBundles
////            profilesDataObservable = Observable.just(new ProfilesData(profiles: []))
//        //FIXME: remove this in v3 architecture
//        } else if (getContext().getString(R.string.api_base_url).contains("/v1") && !getContext().getString(R.string.app_namespace).contains("muzzley")) {
//            Timber.e("hack: removing non muzzley bundles and services")
//            Observable.just(Bundles(bundles= listOf(), services= listOf()))
//        } else {
//            channelService.getServiceBundles()
//        }


        disposable?.add(
//            Observable.zip(channelService.getServiceBundles(), channelService.getProfiles(),
//            Observable.zip(jsonBlob.getServiceBundles(), channelService.getProfiles(),
              Observable.zip(bundlesObservable, profilesDataObservable,
                    BiFunction { bundles: Bundles , profilesData: ProfilesData -> Pair.create(bundles, profilesData) }  )
                    .compose(RxComposers.applyIoRefresh(view::showLoading))
                    .subscribe(
                        { view.showData(it) },
                        { view.showError(it,true) }
                    )
        )
    }

    fun getContext(): Context =
        view as Context

    fun cancelActivity() =
        analyticsTracker.trackSimpleEvent(AnalyticsEvents.ADD_DEVICE_CANCEL_EVENT);


}