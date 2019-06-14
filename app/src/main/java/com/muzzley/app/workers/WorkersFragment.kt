package com.muzzley.app.workers

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.Refresh
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.app.tiles.Models
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.app.tiles.TilesController
import com.muzzley.app.userprofile.UserPreferences
import com.muzzley.model.*
import com.muzzley.model.tiles.TilesData
import com.muzzley.model.units.UnitsTable
import com.muzzley.model.workers.*
import com.muzzley.providers.BusProvider
import com.muzzley.services.LocationInteractor
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.*
import com.muzzley.util.retrofit.CdnService
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.LogObserver
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.Dialogs
import com.muzzley.util.ui.ProgDialog
import com.muzzley.util.ui.ShowcaseBuilder
import com.squareup.otto.Subscribe
import com.tbruyelle.rxpermissions2.Permission
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function5
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.agent_layout_blank_states.*
import kotlinx.android.synthetic.main.fragment_agents.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


/**
 * Created by bruno.marques on 27/11/15.
 */

class WorkersFragment : Fragment() , SwipeRefreshLayout.OnRefreshListener {

    companion object {
        @JvmStatic
        fun newInstance(): WorkersFragment =
                WorkersFragment()

        private const val AGENTS_SETTINGS_PERMISSIONS_INTENT_REQ: Int = 177
    }
    
    @Inject lateinit var userService: UserService
    //    @Inject @field:Named("mock") UserService mock
    @Inject lateinit var tilesController: TilesController
    @Inject lateinit var analyticsTracker: AnalyticsTracker

    @Inject lateinit var modelsStore: ModelsStore
    @Inject lateinit var locationInteractor: LocationInteractor
    @Inject lateinit var cdnService: CdnService
    @Inject lateinit var channelService: ChannelService
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var userPreferences: UserPreferences

//    @InjectView(R.id.list_workers) lateinit var recyclerView: RecyclerView
//    @InjectView(R.id.swipe_container) lateinit var swipeRefresh: SwipeRefreshLayout

//    @InjectViews([R.id.button_add_agent,R.id.button_blank_state]) 
//    lateinit var addAgentButtons: List<Button>
//    @InjectView(R.id.agents_view_flipper) lateinit var viewFlipper: ViewFlipper

    private lateinit var adapter: WorkersAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var locale: Locale
    val controller: WorkersController by lazy {WorkersController(this) }
//    var isVisibleToUser: Boolean? = null
    var dataSubscription: Disposable? = null

    enum class ViewState { LOADING, BLANK, DATA, ERROR }

//    private Subscription dataSubscription


    var workers: MutableList<Worker>? = null

    lateinit var disposable: CompositeDisposable


    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View  {
        val view: View = inflater.inflate(R.layout.fragment_agents, container, false)
        locale = resources.configuration.locale
        Timber.d("Locale = $locale")
        BusProvider.getInstance().register(this)
        disposable = CompositeDisposable()

        return view
    }

    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent.inject(this)
//        controller = WorkersController(this)

        linearLayoutManager = LinearLayoutManager(activity).apply {
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.setHasFixedSize(false)

        adapter = WorkersAdapter(requireActivity(), this)
        recyclerView.adapter = adapter

        // agentFragment to pull to refresh
        swipeRefresh.setOnRefreshListener(this)

        listOf(button_add_agent,button_blank_state).forEach{
            it.setOnClickListener {
                launchAgentsBuilderActivity()
//                showState(ViewState.DATA)
            }
        }
//        addAgentButtons*.setOnClickListener {
//            launchAgentsBuilderActivity()
//            showState(ViewState.DATA)
//        }

        showState(ViewState.LOADING)
        onRefresh()
    }


    override
    fun setUserVisibleHint(isVisibleToUser: Boolean) {
        Timber.d("isVisibleToUser: $isVisibleToUser")
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            activity?.let { // this sometimes gets called before activity being attached ¯\_(ツ)_/¯
                ShowcaseBuilder.showcase(it,
                        getString(R.string.mobile_onboarding_worker_construction_1_title),
                        getString(R.string.mobile_onboarding_worker_construction_1_text),
                        getString(R.string.mobile_onboarding_worker_construction_1_close),
                        R.id.button_add_agent,
                        R.string.on_boarding_agent_builder
                )
                        .subscribe(LogObserver<Boolean>("onboarding agents"))
            }
        }
    }

    fun showState(state: ViewState) {
        viewFlipper.displayedChild = state.ordinal
    }

    @Subscribe fun onRefresh(refresh: Refresh){
        onRefresh()
    }

    override
    fun onRefresh() {
        swipeRefresh.post { swipeRefresh.setRefreshing(true) }

        val workers: Observable<Workers> = userService.getWorkers()
//        val workers: Observable<Workers> = mock.getWorkers()
        val tiles: Observable<TilesData> = userService.getUserTileDataContext()
        val stateModels: Observable<Models> = tilesController.getModelsWithType(Constants.AGENTS_STATEFULL)

        dataSubscription = Observable.zip(tiles, stateModels, workers,
//                channelService.getUser(preferencesRepository.getUser().id),
                userService.preferences,
                cdnService.unitsTable,
                Function5 { tilesData: TilesData, models: Models, workers1: Workers, preferences: Preferences, unitsTable : UnitsTable ->
                    WorkerDataModel(tilesData, models, workers1, preferences, unitsTable)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        { 
                            prepareAllData(it)
                        },
                        {
                            Timber.d(it, "Error getting workers")
                            onItemsUpdateError()
                        }
                )
    }

    private fun prepareAllData(adm: WorkerDataModel) {
        //FIXME: shouldn"t this be just field properties ?
        modelsStore.tileDataAgents = adm.tileData
        modelsStore.modelsStates = adm.model
        userPreferences.update(modelsStore.modelsStates!!.preferences!!)
        workers = adm.workers.workers
        controller!!.updateWorkers(workers!!)
        locationInteractor.registerGeofences(workers!!.mapNotNull { it.fence})
        controller!!.configurePhoneServices(workers!!)
        onItemsUpdate(workers!!)
        swipeRefresh.isRefreshing = false
        handleBlankState()
    }

    private fun handleBlankState() {
        showState(if(adapter?.getItemCount() == 0)  ViewState.BLANK else ViewState.DATA)
    }

    fun success(worker: Worker) {
        analyticsTracker.trackRoutineAction(
                if(worker.enabled) AnalyticsEvents.ENABLE_ROUTINE_EVENT else AnalyticsEvents.DISABLE_ROUTINE_EVENT,
                worker.id,
                EventStatus.Success,
                "Success"
        )
    }

    fun error(worker: Worker, message: String?) {
        analyticsTracker.trackRoutineAction(
                if(worker.enabled) AnalyticsEvents.ENABLE_ROUTINE_EVENT else AnalyticsEvents.DISABLE_ROUTINE_EVENT,
                worker.id,
                EventStatus.Error,
                message
        )
    }

    fun onSwitchClick(worker: Worker) {
//        val track = analyticsTracker::trackRoutineAction.curry(!worker.enabled ? AnalyticsEvents.ENABLE_ROUTINE_EVENT : AnalyticsEvents.DISABLE_ROUTINE_EVENT,worker.id)
//        val track = { analyticsTracker.trackRoutineAction( if(worker.enabled)  AnalyticsEvents.ENABLE_ROUTINE_EVENT else AnalyticsEvents.DISABLE_ROUTINE_EVENT,worker.id)
        disposable += userService.enableWorker(worker.id,mapOf("enabled" to !worker.enabled))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        success(worker)
                        worker.enabled = !worker.enabled
                        adapter.notifyItemChanged(worker.position)
                    },
                    {
                        showError(it)
                        analyticsTracker.trackRoutineAction(
                                if(worker.enabled) AnalyticsEvents.ENABLE_ROUTINE_EVENT else AnalyticsEvents.DISABLE_ROUTINE_EVENT,
                                worker.id,
                                EventStatus.Error,
                                it.message
                        )
                        error(worker,it.message)
                    }
                )

    }

//    fun getPosition(worker: Worker) =
//            workers?.indexOfFirst { it.id == worker.id } ?: -1

    private val Worker.position
            get()= workers?.indexOfFirst { it.id == this.id } ?: -1

    fun executeWorker(worker: Worker){
//        val track = analyticsTracker::trackRoutineExecute.curry(worker.getId())
        if (Network.isConnected(activity)) {
            disposable += userService.executeWorker(worker.id)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe {
                        val currentThread = Thread.currentThread()
                        Timber.d("thread0: $currentThread")
                        worker.executionState = WorkerExecutionState.running
                        adapter.notifyItemChanged(worker.position)
                    }
                    .doOnComplete{
                        val currentThread = Thread.currentThread()
                        Timber.d("thread1: $currentThread")
                        worker.executionState = WorkerExecutionState.success
                        adapter.notifyItemChanged(worker.position)
                    }
                    .observeOn(Schedulers.io())
                    .delay(2,TimeUnit.SECONDS)
                    .andThen(userService.getWorker(worker.id)) // we hope the delay is enough to have lastExecuted updated
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe( {
                        val currentThread = Thread.currentThread()
                        Timber.d("thread2: $currentThread")

                        val pos = it.position
                        if (pos >= 0) {
                            controller.updateWorker(it)
                            workers?.set(pos,it)
                            adapter.notifyItemChanged(pos)
                            analyticsTracker.trackRoutineExecute(it.id,EventStatus.Success,"Success")
                        }

//                        worker.executionState = WorkerExecutionState.idle
//                        adapter.notifyItemChanged(worker.position)
//                        analyticsTracker.trackRoutineExecute(worker.id,EventStatus.Success,"Success")
                    }, {
                        val currentThread = Thread.currentThread()
                        Timber.d(it,"thread3: $currentThread")
                        worker.executionState = WorkerExecutionState.error
                        adapter.notifyItemChanged(worker.position)
//                        adapter.updateLayoutExecutingAgent(WorkersAdapter.AGENT_EXECUTER_ERROR, position)
                        FeedbackMessages.showMessage(viewFlipper, "Execute failed. Please check your network")
                        analyticsTracker.trackRoutineExecute(worker.id,EventStatus.Error,it.message)
//                        error(it,track)
                    })
        } else {
            FeedbackMessages.showMessage(viewFlipper, R.string.mobile_no_internet_title)
            analyticsTracker.trackRoutineExecute(worker.id,EventStatus.Error,getString(R.string.mobile_no_internet_title))
//            error(Exception(getString(R.string.mobile_no_internet_title)),track)
        }

    }

    fun deleteAgentDialog(cardsView: Worker){
        AlertDialog.Builder(activity!!,R.style.AlertDialogStyle)
                .setMessage(getResources().getString(R.string.mobile_worker_delete_text,cardsView.label))
                .setPositiveButton(R.string.mobile_delete) { _, _ -> deleteAgent(cardsView)}
                .setNegativeButton(android.R.string.cancel) { _, _ ->
                    analyticsTracker.trackRoutineAction(AnalyticsEvents.REMOVE_ROUTINE_CANCEL_EVENT,cardsView.id)
                }
                .show()
        analyticsTracker.trackRoutineAction(AnalyticsEvents.REMOVE_ROUTINE_START_EVENT,cardsView.id)

    }

    private fun deleteAgent(cardsView: Worker){
        //currying just for fun :-)
//        val trackRemove: val = analyticsTracker::trackRoutineAction.curry(AnalyticsEvents.REMOVE_ROUTINE_FINISH_EVENT, cardsView.id)
        disposable += userService.deleteWorker(cardsView.id)
                .compose(RxComposers.applyIoRefreshCompletable(ProgDialog.getLoader(context!!)))
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                {
                    onWorkerDeleted(cardsView)
                    analyticsTracker.trackRoutineAction(AnalyticsEvents.REMOVE_ROUTINE_FINISH_EVENT, cardsView.id,EventStatus.Success,"Success")
//                    success(trackRemove)
                },
                {
                    showError(it)
                    analyticsTracker.trackRoutineAction(AnalyticsEvents.REMOVE_ROUTINE_FINISH_EVENT, cardsView.id,EventStatus.Error,it.message)
                }
        )
    }

    fun onWorkerDeleted(worker: Worker) {
        val position = worker.position

        workers?.removeAt(position)
        adapter.notifyItemRemoved(position)
        //todo: we need this to force rebinding, because clickListeners are holding their original positions
        //if that gets refactored, we can remove this
//        adapter.notifyItemRangeChanged(position, adapter.itemCount - position)

        handleBlankState()

        worker.fence?.let {
            locationInteractor.unregisterGeofences(listOf(it))
        }
    }

    fun onWorkerClick(worker: Worker) {

        worker.let {
            launchAgentsBuilderActivity(it.id!!, it.label!!,
                    createRulesUnit(Constants.BRIDGE_AGENTS_TRIGGERABLE, it.triggers),
                    createRulesUnit(Constants.BRIDGE_AGENTS_ACTIONABLE, it.actions),
                    createRulesUnit(Constants.BRIDGE_AGENTS_STATEFULL, it.states)
            )
        }

    }


    fun createRulesUnit(type: String, workerUnit: List<WorkerUnit>?): RuleUnitResponse {
        return RuleUnitResponse(RuleUnit(type, workerUnit?.map {
            WorkerUnit().apply {
                profile = it.profile
                channel = it.channel
                component = it.component
                label = it.label
                choices = it.choices
            }
        } ?: listOf()))
    }

    override
    fun onResume() {
        super.onResume()

        Timber.d("adapter.getItemCount(): ${adapter.getItemCount()}")
        if(adapter.getItemCount()>0) {
            controller.updateWorkers(workers!!)
            locationInteractor.registerGeofences(workers!!.mapNotNull { it.fence})
            controller.configurePhoneServices(workers!!)
            onItemsUpdate(workers!!)
        }
    }

    override fun onPause() {
        dataSubscription?.dispose()
        super.onPause()
    }

    override
    fun onDestroyView() {
        BusProvider.getInstance().unregister(this)
        disposable.clear()
        super.onDestroyView()
    }

    fun onItemsUpdate(viewList: MutableList<Worker>) {
        swipeRefresh.isRefreshing = false
        adapter.setItems(viewList)
        handleBlankState()
    }

    fun onItemsUpdateError() {
        swipeRefresh?.isRefreshing = false
        showState(ViewState.ERROR)
    }

    fun showError(t: Throwable) {
        Timber.e(t)
        swipeRefresh.isRefreshing = false
        FeedbackMessages.showError(viewFlipper)
    }

    private fun launchAgentsBuilderActivity() {
        swipeRefresh.isRefreshing = false
        startActivityForResult<RulesBuilder>(Constants.REQUEST_CODE_INTERFACES) {
            putExtra(RulesBuilder.EXTRA_TYPE, RulesBuilder.TYPE_AGENT)
        }
    }

    private fun launchAgentsBuilderActivity(id: String, name: String, rulesTrigger: RuleUnitResponse, rulesActions: RuleUnitResponse, rulesStates: RuleUnitResponse) {
        swipeRefresh.isRefreshing = false

        startActivityForResult<RulesBuilder>(Constants.REQUEST_CODE_INTERFACES) {
                putExtra(Constants.EXTRA_AGENTS_NAME, name)
                putExtra(Constants.EXTRA_AGENTS_ID, id)
                putExtra(Constants.EXTRA_AGENTS_IS_EDITING, true)
                putObjectExtra(Constants.EXTRA_AGENTS_TRIGGERABLE, rulesTrigger)
                putObjectExtra(Constants.EXTRA_AGENTS_ACTIONABLE, rulesActions)
                putObjectExtra(Constants.EXTRA_AGENTS_STATEFULL, rulesStates)
                putExtra(RulesBuilder.EXTRA_TYPE, RulesBuilder.TYPE_AGENT)
        }
    }

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == Constants.REQUEST_CODE_INTERFACES && resultCode == Activity.RESULT_OK && data != null) {

            val isEditing: Boolean = data.getBooleanExtra(Constants.EXTRA_NEW_AGENTS_EDITING, false)
            val worker: Worker = data.getObjectExtra(Constants.EXTRA_NEW_AGENTS_JSON)!!

            controller.updateWorker(worker)

            Timber.d("onActivityResult editing: $isEditing")
            if (isEditing) {
                val idx = workers!!.indexOfFirst{ it.id == worker.id }
                Timber.d("onActivityResult editing: $isEditing, idx: $idx")
                if (idx >= 0) {
                    workers?.set(idx, worker)
                    adapter.notifyItemChanged(idx)
//                    adapter.replaceItem(worker, idx)
                    recyclerView.smoothScrollToPosition(idx)
                }
            } else {
                workers?.add(0,worker)
                adapter.notifyItemInserted(0)

                recyclerView.smoothScrollToPosition(0)
                RxView.globalLayouts(recyclerView)
                        .take(1)
                        .flatMap {
                            ShowcaseBuilder.showcase(activity!!,
                                    getString(R.string.mobile_onboarding_worker_created_1_title),
                                    getString(R.string.mobile_onboarding_worker_created_1_text),
                                    getString(R.string.mobile_onboarding_worker_created_1_close),
                                    linearLayoutManager?.findViewByPosition(0)?.findViewById<View>(R.id.toggle_agent_card)!!,
                                    R.string.on_boarding_new_agent
                            )
                        }
//                    .toCompletable()
//                    .andThen(
//                        ShowcaseBuilder.showcase(activity,
//                                getString(R.string.mobile_onboarding_worker_created_1_title),
//                                getString(R.string.mobile_onboarding_worker_created_1_text),
//                                getString(R.string.mobile_onboarding_worker_created_1_close),
//                                linearLayoutManager.findViewByPosition(0)?.findViewById(R.id.toggle_agent_card),
//                                R.string.on_boarding_new_agent
//                        )
//                    )
                        .subscribe(LogObserver<Boolean>("onboarding workers"))
            }
            handleBlankState()
        }

        if (requestCode == AGENTS_SETTINGS_PERMISSIONS_INTENT_REQ) {
            App.obtain(context!!).updateCurrentPermissions()
        }
    }

    //============= permissions =================

    private fun isIntentSafe(intent: Intent): Boolean{
        val packageManager: PackageManager = activity!!.packageManager
        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return activities.size > 0
    }

    fun goToSettings() {
        val myAppSettings = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + activity!!.getPackageName()))
        if(isIntentSafe(myAppSettings)) {
            myAppSettings.addCategory(Intent.CATEGORY_DEFAULT)
            //myAppSettings.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivityForResult(myAppSettings, AGENTS_SETTINGS_PERMISSIONS_INTENT_REQ)
        } else {
            startActivityForResult(Intent(android.provider.Settings.ACTION_SETTINGS), AGENTS_SETTINGS_PERMISSIONS_INTENT_REQ)
        }
    }


    fun onAskNotifications(){
        Dialogs.unblockNotifications(activity!!)
    }

    fun onAskPermissions(agentCardView: Worker) {
        disposable += RxPermissions(activity!!).requestEach(*agentCardView.missingPermissions.toTypedArray())
        .toList()
                .subscribe({ permissionList ->
                    App.obtain(context!!).updateCurrentPermissions()
                    if (permissionList.any { it.granted}) {
                        Timber.d("refreshing")
                        controller.updateWorkers(workers!!)
                        onItemsUpdate(workers!!)
                    }
                    if (permissionList.all { !it.granted && !shouldShowRequestPermissionRationale(it.name)}) {
                        val pm: PackageManager = context!!.packageManager
                        val groups: List<String> = permissionList.mapNotNull { p: Permission  ->
                            try {
                                val pi: PermissionInfo = pm.getPermissionInfo(p.name, PackageManager.GET_META_DATA)
                                pm.getPermissionGroupInfo(pi.group, PackageManager.GET_META_DATA).loadLabel(pm).toString()
                            } catch (e: PackageManager.NameNotFoundException) {
                                Timber.e(e, "Error getting pem info")
                                null
                            }
                        }.distinct()
                        if (groups.isNotEmpty()) {
//                    val message: String = """
//                        <b>Grant access</b><br /><br />
//                        Follow these steps to enable necessary permissions.
//                        You can change this later in your phone\"s settings.<br /><br />
//                        &nbsp;<b>1.</b> Open Android Settings <br />
//                        &nbsp;<b>2.</b> Select Apps <br/>
//                        &nbsp;<b>3.</b> Find muzzley App <br/>
//                        &nbsp;<b>4.</b> Tap permissions <br/>""".stripIndent()
//                    groups.forEachWithIndex { String group, Int i ->
//                        message += "&nbsp;<b>${i+5}.</b> Turn $group ON<br/>"
//                    }
                            var message: String = getString(R.string.android_text_permission_1)
                            groups.forEachIndexed { i, group ->
                                message += getString(R.string.android_text_permission_2,i+5,group)
                            }


                            AlertDialog.Builder(activity!!)
                                    .setMessage(message.fromHtml())
                                    .setPositiveButton(R.string.mobile_go_settings) { _, _ -> goToSettings()}
                                    .setNegativeButton(R.string.mobile_not_now,null)
                                    .setCancelable(true)
                                    .show()

                        }
                    }
                },{
                    Timber.e(it,"Error getting permissions")
                })
    }

    fun onAskLocation() {
        disposable += locationInteractor.requestLocation(activity!!)
                .subscribe(
                {
                    Timber.d("locationUsable: $it")
                    if (it) {
                        Timber.d("refreshing")
                        controller.updateWorkers(workers!!)
                        onItemsUpdate(workers!!)
                    }
                },
                { Timber.e(it,"Error getting settings")}
        )
    }

}
