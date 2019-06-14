package com.muzzley.app.tiles

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.ProfilesActivity
import com.muzzley.app.workers.DevicePickerActivity
import com.muzzley.app.cards.ContainerAdapter
import com.muzzley.app.userprofile.UserPreferences
import com.muzzley.model.tiles.ServiceSubscriptions
import com.muzzley.model.tiles.TileGroup
import com.muzzley.providers.BusProvider
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.*
import com.muzzley.util.rx.LogObserver
import com.muzzley.util.ui.*
import com.squareup.otto.Subscribe
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.fragment_tiles.*
import kotlinx.android.synthetic.main.services_layout_blank_states.*
import kotlinx.android.synthetic.main.tiles_layout_blank_states.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by caan on 03-11-2015.
 */


class TilesFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener, TilesGroupHolder.GroupCallBack  {

    val CREATE_GROUP_CODE = 100

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var tilesController: TilesController
    @Inject lateinit var modelsStore: ModelsStore

    private var gridLayoutManager: GridLayoutManager? = null
    var blockUI: Boolean = false

    enum class ViewState { LOADING, BLANK_DEVICES, BLANK_SERVICES, DATA, ERROR }

    private var spanCount: Int = 0
    var isVisibleToUser: Boolean = false

    var active = Constants.Active.PROFILES

    var compositeDisposable: CompositeDisposable? = null

    companion object {
        @JvmStatic
        fun newInstance(): TilesFragment = TilesFragment()
    }

    override
    fun onCreateView(inflater: LayoutInflater , container: ViewGroup?, savedInstanceState: Bundle?): View {
        BusProvider.getInstance().register(this)
        Timber.d("onCreateView")
        return inflater.inflate(R.layout.fragment_tiles, container, false)
    }

    override
    fun onActivityCreated(savedInstance: Bundle?) {
        super.onActivityCreated(savedInstance)
        App.appComponent.inject(this)
        val viewWidth = ScreenInspector.getScreenWidth(activity)
        val cardViewWidth = activity!!.resources.getDimension(R.dimen.tile_min_width)
        spanCount = Math.max(2, Math.floor((viewWidth / cardViewWidth).toDouble()).toInt())
        Timber.d("widths: $recyclerView.width, $viewWidth, $cardViewWidth, ${this.spanCount}")

        gridLayoutManager = GridLayoutManager(activity, this.spanCount).apply {
            recycleChildrenOnDetach = true
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)


        // agentFragment to pull to refresh
        swipeRefresh.setOnRefreshListener(this)

        listOf(buttonAdd,button_add_device,devicesButtonBlankState,servicesButtonBlankState).forEach {
            it.setOnClickListener { launchProfilesActivity(active) }
        }

//        if (getString(R.string.app_id).contains("vodafone")) {
//        if (!getString(R.string.app_id).contains("allianz")) {
//            segmentedGroup.hide()
//        }

        //FIXME: move this to connection
        tilesController.subscribeAll()
        if (resources.getBoolean(R.bool.disable_timeline)) {
//        if (getString(R.string.app_id).contains("vodafone")) {
            userVisibleHint = true
        }
        group.setOnClickListener(this::group)
    }

    override
    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        this.isVisibleToUser = isVisibleToUser
        val visibleView = view != null
        Timber.d("isVisibleToUser: $isVisibleToUser, visbleView: $visibleView, buttonAddVisible: ${buttonAdd != null}")
        super.setUserVisibleHint(isVisibleToUser)

        fake?.let { view ->
            isVisibleToUser && view.postDelayed({ //is null on slow devices ?!
                ShowcaseBuilder.showcase(activity!!,
                        getString(R.string.mobile_onboarding_devices_1_title),
                        getString(R.string.mobile_onboarding_devices_1_text),
                        getString(R.string.mobile_onboarding_devices_1_close),
                        view,
                        R.string.on_boarding_add_device
                ).subscribe(LogObserver<Boolean>("onboarding tiles1"))

            },500)
        }
    }


    fun showState(state: ViewState) {
        viewFlipper.displayedChild = state.ordinal
    }

    override
    fun onStart() {
        super.onStart()
        if (modelsStore.models == null) {
            showState(ViewState.LOADING)
            onRefresh()
        } else {
            updateControl()
            updateUi(modelsStore.models)
        }
    }


    @Subscribe fun onTilesRefresh(tilesRefresh: TilesRefresh){

        activity?.supportFragmentManager?.apply {
            if (findFragmentByTag(Constants.FRAGMENT_GROUP) != null) {
                popBackStack()
            }
        }
        blockUI = true
        onRefresh()
    }

    private var progressDialog: ProgressDialog? = null
    fun showLoading(show: Boolean) {
        if (blockUI) {
            if (show) {
                progressDialog = ProgDialog.show(context!!)
            } else {
                progressDialog?.dismiss()
                blockUI = false
            }
        } else {
            if (show) {
                swipeRefresh.post { swipeRefresh.isRefreshing = true }
            } else {
                swipeRefresh.isRefreshing = false
            }
        }
    }

    override
    fun onRefresh() {
        val tilesCount = modelsStore.models?.tilesData?.tiles?.size

        showLoading(true)
        tilesController.getModels().subscribe({ models ->
            modelsStore.models = models

            models.preferences?.let {
                userPreferences.update(it)
            }

            showLoading(false)
            updateControl()
            updateUi(models)
            tilesController.subscribeTilesDevicesStatus(models)
            tilesController.getTilesDevicesStatus(models)

            // not the first run , which means it"s a refresh, and there"s something new
            if (tilesCount != null && tilesCount < models?.tilesData?.tiles?.size ?: 0) {

//            if (tilesCount != null ) {
                RxView.globalLayouts(recyclerView)
                .take(1)
                .flatMap {
                    val v = gridLayoutManager?.findViewByPosition(1)
                    if (v == null)
                        Observable.just(false)
                    else
                        ShowcaseBuilder.showcase(
                            activity!!,
                            getString(R.string.mobile_onboarding_tile_added_1_title),
                            getString(R.string.mobile_onboarding_tile_added_1_text),
                            getString(R.string.mobile_onboarding_tile_added_1_close),
                            v,
                            R.string.on_boarding_new_tile
                    )
                }
//                .flatMap{
//                    if (modelsStore?.models?.anythingGroupable()) {
//                        ShowcaseBuilder.showcase(
//                                activity,
//                                getString(R.string.mobile_onboarding_create_group_1_title),
//                                getString(R.string.mobile_onboarding_create_group_1_text),
//                                getString(R.string.mobile_onboarding_create_group_1_close),
//                                R.id.group,
//                                R.string.on_boarding_group_create
//                        )
//                    } else {
//                        Observable.just(true)
//                    }
//                }
//                .flatMap{
//                    val firstGroup = models.tilesViewModel.findIndexOf {it is TileGroup && (it as TileGroup).parent != null}
//                    Timber.d("first group : $firstGroup")
//                    if (firstGroup >= 0) {
//                        gridLayoutManager.scrollToPositionWithOffset(firstGroup, 40)
//                        RxView.globalLayouts(recyclerView)
//                                .take(1)
////                                .singleOrError()
//                                .flatMap {
//                                    ShowcaseBuilder.showcase(
//                                            activity,
//                                            getString(R.string.mobile_onboarding_group_created_1_title),
//                                            getString(R.string.mobile_onboarding_group_created_1_text),
//                                            getString(R.string.mobile_onboarding_group_created_1_close),
//                                            gridLayoutManager.findViewByPosition(firstGroup),
//                                            R.string.on_boarding_new_group
//                                    )
//                                }
//                    } else {
//                        Observable.just(false)
//                    }
//                }
                .subscribe(LogObserver<Boolean>("onboarding tiles2"))
            }
        },
                {
                    Timber.d(it, "Tile error")
                    fail(it.message)
                    showState(ViewState.ERROR)
                }).addTo(compositeDisposable)

    }

    fun updateControl() {
        val segments = mutableListOf(Constants.Active.PROFILES to getString(R.string.mobile_devices))

        if (modelsStore.models?.bundlesAndServices?.bundles.isNotNullOrEmpty())
            segments.add(Constants.Active.BUNDLES to getString(R.string.mobile_bundles))
        if (modelsStore.models?.bundlesAndServices?.services.isNotNullOrEmpty())
            segments.add(Constants.Active.SERVICES to getString(R.string.mobile_services))
        if (segments.size == 1) {
            buttonAdd.invisible()
            segmentedGroup.hide()
            button_add_device.show()
        } else {
            segmentedGroup.show()
            buttonAdd.show()
            segmentedGroup.setContainerData(segments.map { it.second })
            segmentedGroup.setSelected(segments.indexOfFirst { it.first == active })
            segmentedGroup.listenSelected().subscribe {
                active = segments[it].first
                updateUi(modelsStore.models)
            }
        }
    }


    fun updateUi(models: Models?) {
        //FIXME: comented until we have groups again in v3
        //group.setVisibility(active == R.id.devices ? View.VISIBLE: View.INVISIBLE)
        if (models == null) {
            return
        }
        if (active == Constants.Active.PROFILES) {
            gridLayoutManager?.spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override
                fun getSpanSize(position: Int): Int =
                    if (models.tilesViewModel[position] is TileGroup && (models.tilesViewModel[position] as TileGroup).parent == null ) spanCount else 1
            }

            val tilesAdapter = TileGroupsAdapter(activity!!, this)
            tilesAdapter.setData(models.tilesViewModel)

            recyclerView.adapter = tilesAdapter
            showState(if (models.tilesViewModel.isNotEmpty()) ViewState.DATA else ViewState.BLANK_DEVICES)
        } else {
            gridLayoutManager?.spanSizeLookup = GridLayoutManager.DefaultSpanSizeLookup()
            val servicesAdapter = ContainerAdapter<ServiceSubscriptions.Subscription>(activity, R.layout.subscription)
            servicesAdapter.setData(models.serviceSubscriptions)
            recyclerView.adapter = servicesAdapter
            showState(if (models.serviceSubscriptions.isNotNullOrEmpty()) ViewState.DATA else ViewState.BLANK_SERVICES)
        }
        //TODO: handle bundles also ?
    }

    fun fail(reason: String?) {
        showLoading(false)
        FeedbackMessages.showError(recyclerView)
    }

    private fun launchProfilesActivity(defaultView: Constants.Active) {
        context?.startActivity<ProfilesActivity> {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(Constants.DEFAULT_SEGMENT,defaultView)
        }
    }

    fun group(view: View) {

        if (modelsStore.models?.anythingGroupable() != true)
            FeedbackMessages.showMessage(recyclerView,getString(R.string.mobile_no_groupable_devices_text))
        else {
            startActivityForResult<DevicePickerActivity>(CREATE_GROUP_CODE) {
                putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, getString(R.string.mobile_group_create))
                putExtra(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT, "Component name")
                putExtra(Constants.EXTRA_DEVICE_PICKER_CREATE_GROUP, true)
            }
        }
    }

    override
    fun onGroupClick(groupId: String) {
        if (modelsStore.models?.tileGroupsData?.tileGroups?.any {it.id == groupId} == true) { // just making sure group wasn"t removed meanwhile
            val fragment = TileGroupsFragment.newInstance(groupId)

            activity?.supportFragmentManager
                    ?.beginTransaction()
                    ?.setCustomAnimations(R.anim.abc_slide_in_bottom, 0, 0, R.anim.slide_out_down)
                    ?.add(R.id.group_menu_container, fragment, Constants.FRAGMENT_GROUP)
                    ?.addToBackStack("TileGroupsFragment")
                    ?.commit()
        }
    }

    override fun onResume() {
        super.onResume()
        compositeDisposable = compositeDisposable ?: CompositeDisposable()
    }

    override fun onPause() {
        compositeDisposable?.dispose()
        compositeDisposable = null
        super.onPause()
    }

    override
    fun onDestroyView() {
        BusProvider.getInstance().unregister(this)
        super.onDestroyView()
    }

    override
    fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent? ) {
        if(resultCode == Activity.RESULT_OK) {
            if(requestCode == CREATE_GROUP_CODE) {
                onRefresh()
            }
        }
    }
}
