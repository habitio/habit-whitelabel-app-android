package com.muzzley.app.shortcuts

import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.model.workers.RuleUnitResponse
import com.muzzley.app.workers.RulesBuilder
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.model.workers.RuleUnit
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.startActivityForResult
import com.muzzley.util.toJsonString
import com.muzzley.util.ui.DraggableViewModelAdapter
import com.muzzley.util.ui.SectionVM
import com.muzzley.util.ui.ViewModel
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.android.synthetic.main.activity_shortcuts2.*
import timber.log.Timber
import javax.inject.Inject


class ShortcutsActivity2 : AppCompatActivity() , SwipeRefreshLayout.OnRefreshListener {

    val SHORTCUT_CREATE_CODE: Int = 1
    val SHORTCUT_EDIT_CODE: Int = 2


    @Inject lateinit var shortcutsInteractor: ShortcutsInteractor
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var shortcutsPresenter: ShortcutsPresenter


    val executeRx: Subject<ShortcutVM> = PublishSubject.create()
    val deleteRx: Subject<ShortcutVM> = PublishSubject.create()
    val editRx: Subject<ShortcutVM> = PublishSubject.create()
    val dragRx: Subject<ShortcutVM> = PublishSubject.create()
    val editMode: Subject<Boolean> = BehaviorSubject.createDefault(false)
    var inEditMode: Boolean = false //FIXME: we sould be able to do this with just rxjava operators

    lateinit var mWrappedAdapter: RecyclerView.Adapter<*>
    lateinit var adapter: DraggableViewModelAdapter<ViewModel>

    var shortcutVMs: MutableList<ViewModel> = mutableListOf()


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)

        setContentView(R.layout.activity_shortcuts2)
        swipeRefreshLayout.setOnRefreshListener(this)

        adapter = DraggableViewModelAdapter(this)
        val mRecyclerViewDragDropManager = RecyclerViewDragDropManager()
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(adapter);      // wrap for dragging

        recyclerView.adapter = mWrappedAdapter
        mRecyclerViewDragDropManager.attachRecyclerView(recyclerView)
        showLess.setOnClickListener {
            swipeRefreshLayout.isRefreshing = false
        }

        shortcutsPresenter.adapter = mWrappedAdapter
        executeRx.subscribe{ shortcutsPresenter.executeShortcut(it) }

        adapter.fromTo
            .map {
                showLoading(true)
                shortcutVMs.add(it.second, shortcutVMs.removeAt(it.first))
                it
            }.flatMapCompletable{ pair ->
                val shortcuts = shortcutVMs.filterIsInstance<ShortcutVM>().mapNotNull{ it.shortcut }
                shortcutsInteractor.reorderShortcuts(shortcuts)
                    .compose(RxComposers.applyIoRefreshCompletable(this::showLoading))
                    .doOnError {
                        shortcutVMs.add(pair.first, shortcutVMs.removeAt(pair.second))
                        mWrappedAdapter.notifyDataSetChanged()
                        FeedbackMessages.showError(recyclerView)
                    }
                    .onErrorComplete()
            }
            .subscribe ()


        RxView.clicks(showLess)
                .subscribe {
                    if(!inEditMode) {
                        finish()
                    } else {
                        inEditMode = false
                        editMode.onNext(false)
                    }
                }


        editMode.subscribe {
            inEditMode = it
            showLess.text = getString(if (it) R.string.mobile_done else R.string.mobile_show_less)
            swipeRefreshLayout.isEnabled = !it
        }

        deleteRx
                .flatMap { shortcutVM ->

                    if (shortcutVM.shortcut == null) {
                        Timber.e("Tried to delete null shortcut")
                        Observable.error(RuntimeException("Tried to delete null shortcut"))
                    } else {
                        analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_START_EVENT, shortcutVM.shortcut.id)
                        RxDialogs.confirm(this, null,
                                getString(R.string.mobile_shortcut_delete_text),
                                getString(R.string.mobile_delete),
                                getString(R.string.mobile_cancel)
                        )
                                .doOnNext {
                                    if (!it) analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_CANCEL_EVENT, shortcutVM.shortcut.id)
                                }
                                .filter { it }
                                .flatMap {
                                    shortcutsInteractor.deleteShortcut(shortcutVM.shortcut)
                                }
                                .doOnError {
                                    Timber.d(it, "Error deleting shortcut")

                                    analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_FINISH_EVENT,
                                            shortcutVM.shortcut.id,
                                            EventStatus.Error,
                                            it.message)
                                }
                                .doOnNext {
                                    analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_FINISH_EVENT, it.id, EventStatus.Success, "Success")
//                        ShortcutWidgetProvider.sendUpdateWidgetsBroadcast(ShortcutsActivity.this)

                                }
//                    .onErrorResumeNext(Observable.empty()) //swallow exceptions and keep hot observable working
                    }
                }
        .retry() //in case of error, it resubscribes again
        .subscribe({
            Timber.d("onDelete !")
            //remove data and adapter notify
            onRefresh()
        }, {
            Timber.e(it)
        })

        //FIXME: handle result onActivityResult
        editRx.subscribe {
            Timber.d("onEdit!")
            onItemEdit(it)
        }

        onRefresh()
    }

    fun showLoading(show: Boolean) {
        swipeRefreshLayout.post {
            swipeRefreshLayout.isRefreshing = show
        }
    }

    override
    fun onRefresh() {
        showLoading(true)
        shortcutsInteractor.getShortcuts()
                .subscribe(
                {
                    Timber.d("thread1: ${Thread.currentThread().name}")
                    showLoading(false)
                    val data: MutableList<ViewModel> = mutableListOf()


                    data.add(SectionVM(R.layout.section_view, getString(R.string.title_activity_shortcuts)))
                    it.shortcuts?.forEach {
                        data.add(
                                ShortcutVM(R.layout.shortcut_horizontal, it, executeRx).apply {
                                    delete = deleteRx
                                    edit = editRx
                                    drag = dragRx
                                    editMode = editMode
                                }
                        )
                    }
                    data.add(SectionVM(R.layout.section_view, getString(R.string.mobile_shortcut_suggestion)))

                    shortcutVMs = data
                    adapter.setData(shortcutVMs)
                    mWrappedAdapter.notifyDataSetChanged()
                    shortcutsPresenter.shortcutVMS = data

                },
                {
                    Timber.d("thread2: ${Thread.currentThread().name}")
                    showLoading(false)
                    Timber.e(it)
                }
        )
    }

    fun onItemEdit(shortcutVM: ShortcutVM) {
        shortcutVM.shortcut?.let { shortcut ->
            val ruleUnit= RuleUnit(Constants.BRIDGE_AGENTS_ACTIONABLE, shortcut.actions)

            startActivityForResult<RulesBuilder>(SHORTCUT_EDIT_CODE){
                putExtra(Constants.EXTRA_AGENTS_IS_EDITING, true)
                putExtra(Constants.EXTRA_AGENTS_ID, shortcut.id)
                putExtra(Constants.EXTRA_AGENTS_NAME, shortcut.label)
                putExtra(Constants.EXTRA_AGENTS_ACTIONABLE, RuleUnitResponse(ruleUnit).toJsonString())
                putExtra(RulesBuilder.EXTRA_SHOW_IN_WATCH, shortcut.isShowInWatch)
                putExtra(RulesBuilder.EXTRA_TYPE, RulesBuilder.TYPE_SHORTCUT)
            }

            analyticsTracker.trackShortcutAction(AnalyticsEvents.EDIT_SHORTCUT_START_EVENT, shortcut.getId())
        } ?: Timber.e("Tried to edit null shortcut")

    }

    override
    fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent? ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            onRefresh()
        } else if (resultCode == RESULT_CANCELED) {
            when (requestCode) {
                SHORTCUT_EDIT_CODE ->
                    analyticsTracker.trackShortcutAction(
                            AnalyticsEvents.EDIT_SHORTCUT_CANCEL_EVENT,
                            data?.getStringExtra(Constants.EXTRA_AGENTS_ID)
                    )
            }
        }
    }
}
