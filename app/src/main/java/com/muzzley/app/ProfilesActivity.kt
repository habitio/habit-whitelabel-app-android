package com.muzzley.app

import android.content.Intent
import androidx.core.util.Pair
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.cards.ContainerAdapter
import com.muzzley.app.profiles.*
import com.muzzley.model.discovery.Action
import com.muzzley.model.discovery.NetworkInfo
import com.muzzley.model.discovery.Param
import com.muzzley.model.profiles.*
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.*
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.show
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_profiles.*
import timber.log.Timber
import javax.inject.Inject

class ProfilesActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener, ProfilesIView{
    
    companion object {
        const val LOCAL_DISCOVERY_PREFIX = "muzdiscovery";
        const val HTTP_PREFIX = "http"
        const val URL_REQUEST = "urlRequest"
        const val WITH_FOOTER = "footer"
        const val EXTRA_LOCATION = "extra-location"
        const val EXTRA_PROFILE_NAME = "extra-profile-name"
        const val EXTRA_PROFILE_ID = "profile_id";
    }

    enum class ViewState { LOADING, BLANK, DATA, ERROR }
//    enum class Active { PROFILES, BUNDLES, SERVICES }

    @Inject lateinit var navigator: Navigator 
    @Inject lateinit var profilesPresenter: ProfilesPresenter 
    @Inject lateinit var bundleFlow: BundleFlow 
    @Inject lateinit var recipeNavigator: RecipeNavigator
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var recipeInteractor: RecipeInteractor

//    @Inject lateinit var modelsStore: ModelsStore

    var bundlesAndProfiles: Pair<Bundles, ProfilesData>? = null

    var profile: Profile? = null
    var spanCount: Int = 0 
    var gridLayoutManager: GridLayoutManager? = null
    var adapter: ContainerAdapter<*>? = null
    var active = Constants.Active.PROFILES

    private var doingStuff = false
    private var runRefresh = true

    override
    fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this)
        setContentView(R.layout.activity_profiles)
        configActionBar()

        swipeRefresh.setOnRefreshListener(this)
        val viewWidth = ScreenInspector.getScreenWidth(this);
        val cardViewWidth = getResources().getDimension(R.dimen.tile_min_width);
        val spanCount = Math.max(2, Math.floor((viewWidth / cardViewWidth).toDouble()).toInt()) 

        gridLayoutManager = GridLayoutManager(this, spanCount).apply {
            recycleChildrenOnDetach = true
            orientation = LinearLayoutManager.VERTICAL;
        }
        recyclerView.apply { 
            layoutManager = gridLayoutManager
            setHasFixedSize(true)
        }

        //todo: move this to onResume
//        profilesPresenter.attachView(this)

        onRefresh()

    }

    override
    fun onDestroy() {
        super.onDestroy()
        profilesPresenter.dettachView()
    }

    fun showState(state: ViewState) {
        viewFlipper.displayedChild = state.ordinal;
    }


    fun configActionBar() {
        getSupportActionBar()?.apply{
            setDisplayHomeAsUpEnabled(true);
            setHomeButtonEnabled(true);
        }
    }


    fun updateControl() {
        val segments = mutableListOf(Constants.Active.PROFILES to getString(R.string.mobile_devices))

        if (bundlesAndProfiles?.first?.bundles.isNotNullOrEmpty())
            segments.add(Constants.Active.BUNDLES to getString(R.string.mobile_bundles))
        if (bundlesAndProfiles?.first?.services.isNotNullOrEmpty())
            segments.add(Constants.Active.SERVICES to getString(R.string.mobile_services))
        if (segments.size == 1)
            segmentedGroup.hide()
        else {
            segmentedGroup.show()
            segmentedGroup.setContainerData(segments.map { it.second })
            if (intent.hasExtra(Constants.DEFAULT_SEGMENT)){
                active = intent.getSerializableExtra(Constants.DEFAULT_SEGMENT) as Constants.Active
                intent.removeExtra(Constants.DEFAULT_SEGMENT)
                val i = segments.indexOfFirst { it.first == active }
                segmentedGroup.setSelected(i)
            }
            segmentedGroup.listenSelected().subscribe {
                active = segments[it].first
                updateUi()
            }
        }

    }

    fun updateUi() {
        if (active == Constants.Active.SERVICES && bundlesAndProfiles?.first?.services.isNotNullOrEmpty()) {
            adapter = ContainerAdapter<Bundle>(this, R.layout.bundle).apply {
                recyclerView.adapter = this
                setData(bundlesAndProfiles!!.first!!.services)
            }
            showState(ViewState.DATA)
        } else if (active == Constants.Active.BUNDLES && bundlesAndProfiles?.first?.bundles.isNotNullOrEmpty()) {
            adapter = ContainerAdapter<Bundle>(this, R.layout.bundle).apply {
                recyclerView.adapter = this
                setData(bundlesAndProfiles!!.first!!.bundles)
            }
            showState(ViewState.DATA)
        } else if (active == Constants.Active.PROFILES && bundlesAndProfiles?.second?.profiles.isNotNullOrEmpty()) {
            adapter = ContainerAdapter<Profile>(this, R.layout.profile).apply {
                recyclerView.adapter = this
                setData(bundlesAndProfiles!!.second!!.profiles)
            }
            showState(ViewState.DATA)
        } else {
            showState(ViewState.BLANK)
        }

    }

    override 
    fun showData(bundlesProfilesDataPair: Pair<Bundles, ProfilesData>) {
        this.bundlesAndProfiles = bundlesProfilesDataPair
        updateControl()
        updateUi()
    }


    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
//            if (requestCode == 45) {
//                startActivity(navigator.newSubscriptionsIntent(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        .putExtra(ProfilesActivity.EXTRA_PROFILE_NAME, profile.name)
//                        .putExtra(ProfilesActivity.EXTRA_PROFILE_ID, profile.id)
//                )
//            }
            if (requestCode == 46) {
                bundleFlow.currBundleState = BundleFlow.BundleState.auth
                Timber.d("got back from AuthActivity and is auth")
                BundleNavigator.navigateTo(this,bundleFlow.nextBundleState)
            }
        }
    }

    override
    fun showBundleState(state: BundleFlow.BundleState) {
        BundleNavigator.navigateTo(this,state)
    }

    override
//    fun executeRecipeAction(recipeState: RecipeState) {
//        recipeState.result?.response?.payload?.let { payload ->
//            if (recipeState.action == RecipeAction.oauth && payload.urlRequest == null) {
//                val aurl = payload.authorization_url
//                if (aurl != null) {
//                    profilesPresenter.getRecipeAuthorizationUrlRequest(aurl)
//                            .compose(RxComposers.applyIoRefresh(this::showLoading))
//                            .subscribe(
//                                    {
//                                        payload.urlRequest = it
//                                        recipeNavigator.navigateTo(this, recipeState)
//                                    },
//                                    {
//                                        showError(it,false)
//                                    }
//                            )
//                }
//            } else {
//                if (recipeState.action == null) {
//                    showError(RuntimeException("Unknown action"))
//                } else {
//                    recipeNavigator.navigateTo(this, recipeState)
//                }
//            }
//        } ?: showError(RuntimeException("Missing payload"))
//
//    }
    fun executeRecipeAction(recipeState: RecipeState) {
        recipeState.result?.response?.payload?.let { payload ->
            when (recipeState.action) {
                null -> showError(RuntimeException("Unknown action"))
                RecipeAction.oauth -> iff (payload.urlRequest == null ) {

                    payload.authorization_url?.let { aurl ->
                        profilesPresenter.getRecipeAuthorizationUrlRequest(aurl)
                                .compose(RxComposers.applyIoRefresh(this::showLoading))
                                .subscribe(
                                        {
                                            payload.urlRequest = it
                                            recipeNavigator.navigateTo(this, recipeState)
                                        },
                                        {
                                            showError(it,false)
                                        }
                                )
                    } ?: showError(RuntimeException("Missing authorization_url"))

                } ?: recipeNavigator.navigateTo(this, recipeState)

                RecipeAction.udp -> {
                    Observable.just( Action(null,Action.TYPE_NETWORK_INFO, Param().apply { pInterface = "wifi";  isBroadcast = true }))
                            .flatMap { recipeInteractor.getActionResultString(it) }
                            .flatMap {
                                val networkInfo = it.parseJson<NetworkInfo>()
                                val action = Action(null, Action.TYPE_UDP, Param().apply {
                                    host = networkInfo!!.broadcast!!
                                    port = payload.port!!
                                    ttl = payload.ttl!!
                                    isExpectResponse = payload.expect_response!!
                                    data = payload.data
                                })
                                recipeInteractor.getActionResultString(action)
                                        .toList()
                                        .toObservable()
//                                        .map { responses ->
//                                            val fibaroPackets = responses.filter{ it.contains("com.fibaro.")}
//
//                                            val results = fibaroPackets.map { toMap(it) }
//                                            Timber.d("results: $results")
//                                            results
//                                        }

                            }
                            .compose(RxComposers.applyIo())
                            .subscribe(
                                    {
                                        Timber.d("udp results: $it")
                                    },
                                    {
                                        Timber.e(it,"Error sending udp")
                                    }
                            )

                }
                else -> recipeNavigator.navigateTo(this, recipeState)
            }

        } ?: showError(RuntimeException("Missing payload"))

    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                profilesPresenter.cancelActivity()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }



    override
    fun onBackPressed() {
        profilesPresenter.cancelActivity()
        super.onBackPressed();
    }


    override
    fun showLoading(show: Boolean) {
        swipeRefresh.post { swipeRefresh.isRefreshing = show }
    }

    fun showError(t: Throwable) = showError(t,false)

    override
    fun showError(t: Throwable, showErrorPage: Boolean) {
        doingStuff = false
        showLoading(false)
        Timber.d(t, "showError")
        FeedbackMessages.showError(swipeRefresh)
        if (showErrorPage) {
            showState(ViewState.ERROR)
        }
    }

    override
    fun onRefresh() {
        profilesPresenter.getBundlesAndProfiles()
    }

    override fun onResume() {
        super.onResume()
        doingStuff = false
        showLoading(false)
        profilesPresenter.attachView(this)
        if (runRefresh)
            onRefresh()
    }

    override fun onPause() {
        super.onPause()
        profilesPresenter.dettachView()
    }

    override fun onRestart() {
        super.onRestart()
        runRefresh = false
    }


    fun profileClick(profile: Profile) {
        doingStuff = doingStuff && return || true

        this.profile = profile
        profilesPresenter.addProfile(profile)
    }

    fun bundleClick(bundle: Bundle) {
        doingStuff = doingStuff && return || true

        profilesPresenter.onBundleClick(bundle)
    }

}
