package com.muzzley.app.profiles

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.muzzley.App
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.recipes.*
import com.muzzley.model.Subscription
import com.muzzley.model.channels.Channel
import com.muzzley.model.profiles.Process
import com.muzzley.model.profiles.Profile
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.model.profiles.RecipeState
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.addTo
import com.muzzley.util.iff
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.ProgDialog
import com.muzzley.util.ui.ViewModelAdapter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_bundlecards.*
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RecipeCardsActivity : AppCompatActivity() {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var recipeNavigator: RecipeNavigator
    @Inject lateinit var channelService: ChannelService
    @Inject lateinit var profilesInteractor: ProfilesInteractor 
    @Inject lateinit var recipeInteractor2: RecipeInteractor2 
    @Inject lateinit var recipePresenter: RecipePresenter

    private lateinit var bundleViewModels: List<BundleVM>
    private var adapter: ViewModelAdapter<BundleVM>? = null
    private val disposable = CompositeDisposable()


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        App.appComponent.inject(this)
        setContentView(R.layout.activity_bundlecards)
//        SwissKnife.inject(this)

        recipePresenter.setActivity(this)

//        val profile = recipeNavigator.recipeState?.profile
//        val recipeState = (RecipeNavigator3.currState as State.Step).action.recipeState
        //fixme: do this properly
//        val recipeState = (RecipeNavigator3.stateMachineStore?.listenState()?.value as? State.Step)?.action?.recipeState

        val sm = recipePresenter.sm
        val value = sm.listenState().value
//        val action = (value as? State.Step)?.action
//        val action = (value as? State.FetchingResult)?.action
//        val action = (value as? State.Step)?.action
        val recipeState= when(value) {
            is State.Step -> value.action.recipeState
            is State.FetchingResult -> value.action.recipeState
            else -> null
        }
//        val recipeState = action?.recipeState

        val profile = recipeState?.profile

        if (profile == null) {
            showError(RuntimeException("No profile found !"))
            finish()
            return
        }

        bundleViewModels = listOf(
                BundleVM(
                    profile =  profile,
                    layout =  R.layout.profile_channels_card,
                    state =  AddState.idle
//                    click: onAction(),
                )
        )
        recyclerView.layoutManager?.setAutoMeasureEnabled(true)
        adapter = ViewModelAdapter(this)
        recyclerView.adapter = adapter
        adapter?.setData(bundleViewModels)

        Observable.combineLatest(bundleViewModels.map { it.validRx }) { it.all { it == true } }
                .subscribe { nextBtn.isEnabled = it }.addTo(disposable)

        nextBtn.setOnClickListener {
            nextBtn.isEnabled = false
            /*
            todo: now, instead of submiting recipeNavigator,
            we have to create action with the correct payload
            and trigger state machine send
            How do I find which particular channel was selected
            */

//            recipePresenter.sendSelectedDevices(recipeState,selectedDevices(recipeState))
            recipePresenter.sendSelectedDevicesIndex(selectedDevicesIndex(recipeState))

//            submit(recipeState)
//            submit(recipeNavigator.recipeState)
        }

        //FIXME: Map yet undefined structure and url to old structure
//        getChannels(profile)

    }

    fun updateCard(channels: List<Channel>){
        Timber.d("updateCard ${Thread.currentThread().name}")
        bundleViewModels[0].channels = channels
        bundleViewModels[0].state = AddState.finished
        recyclerView.adapter?.notifyItemChanged(0)
    }

//    fun getChannels(profile: Profile) {
//        channelService.getProfileChannels(profile.id)
//                .compose(RxComposers.applyIo())
//                .subscribe({
//                    updateCard(it)
//                }, {
//                    Timber.e(it, "Error getting profileChannels")
//                    bundleViewModels[0].apply {
//                        channels = listOf()
//                        state = AddState.error
//                        click = View.OnClickListener {
//                            bundleViewModels[0].state = AddState.running
//                            recyclerView.adapter?.notifyItemChanged(0)
//                            getChannels(profile)
//                        }
//                    }
//                    recyclerView.adapter?.notifyItemChanged(0)
//                }
//                ).addTo(disposable)
//    }

    fun showError(throwable: Throwable) {
        Timber.e(throwable)
//        FeedbackMessages.showError(recyclerView)

        FeedbackMessages.showMessageWithAction(recyclerView,getString(R.string.mobile_ok), Snackbar.LENGTH_INDEFINITE) {
            Timber.d("action clicked")
            finish()
        }
    }

    fun showError(error: State.Error) {
        Timber.e(error.throwable)
//        if (error.action == null) { // unrecoverable
            FeedbackMessages.showMessageWithAction(recyclerView,getString(R.string.mobile_ok), Snackbar.LENGTH_INDEFINITE) {
                Timber.d("action clicked")
                finish()
            }
//        } else {
        val bundleVM = bundleViewModels[0]
        bundleVM.state = AddState.finished
        adapter?.notifyItemChanged(0)

//            bundleVM.state = AddState.error
//            bundleVM.click = View.OnClickListener {
//                recipePresenter.sm.onAction(error.action)
//            }
//            adapter?.notifyItemChanged(0)
//            FeedbackMessages.showMessage(recyclerView,R.string.mobile_error_text)
//        }
    }

    fun showLoading(n: Boolean) {
        val bundleVM = bundleViewModels[0]
        bundleVM.state = AddState.running
        adapter?.notifyItemChanged(0)
    }

    override
    fun onOptionsItemSelected(item: MenuItem ): Boolean =
        recipePresenter.onOptionsItemSelected(item)

    //fixme: send selected indexes to presenter
    //it should know how to retrieve devices from latest recipeState
    fun selectedDevices(recipeState: RecipeState): List<Any>?{
        val bundleVM = bundleViewModels[0]
        return bundleVM.channelData?.mapIndexedNotNull { index, channelVM ->
            iff (channelVM.selected != false) {
                recipeState.localResults?.getOrNull(index)
            }
        }
    }

    fun selectedDevicesIndex(recipeState: RecipeState): List<Int>?{
        val bundleVM = bundleViewModels[0]
        return bundleVM.channelData?.mapIndexedNotNull { index, channelVM ->
            iff (channelVM.selected != false) {
                index
            }
        }
    }

/*
    fun submit(recipeState: RecipeState?) {

        //delegate action to presenter ?
        //what's being posted ?
        //try with 2 objects


        if (recipeState?.result?.next_step != null) {
            recipeInteractor2.nextStep(recipeState)
                .compose(RxComposers.applyIoRefresh(ProgDialog.getLoader(this)))
//                .compose(RxComposers.applyIoRefresh(this::showLoading)
                .subscribe({
                    when (it.action) {
                        RecipeAction.send_selected_devices -> sendSelectedDevices(it)
                        else -> {
                            recipeNavigator.navigateTo(this,it)
                            finish()
                        }
                    }
                }, this::showError).addTo(disposable)
        } else {
            if (recipeState?.process == Process.mz_wl_add_device_process) {
                startActivity(navigator.newTilesWithRefresh())
            } else {
                finish()
            }
        }
    }

    fun sendSelectedDevices(recipeState: RecipeState) =
        Observable.fromCallable{
            val bundleVM = bundleViewModels[0]
//            val chIdxs = bundleVM.channelData.findIndexValues {
//                (it as ChannelVM).selected in [true, null]
//            }
//            Subscription(bundleVM.profile.id,bundleVM.channels[chIdxs].map {
//                Subscription.Channel(it.id, it.content, it.activity)
//            })
            val sc = bundleVM.channelData?.foldIndexed(mutableListOf<Subscription.Channel>()) {
                idx, acc, el ->
                if (el.selected == null || el.selected == true) {
                    bundleVM.channels?.getOrNull(idx)?.let {
                        acc.add(Subscription.Channel(it.id, it.content, it.activity))
                    }
                }
                acc
            }
            Subscription(bundleVM.profile.id,sc)


        }
        .flatMap{
            profilesInteractor.postSubscription(it)
                .delay(10,TimeUnit.SECONDS)
        }
//        .flatMap { recipeInteractor2.nextStep()}
        .compose(RxComposers.applyIoRefresh(ProgDialog.getLoader(this)))
        .subscribe(
            {
                submit(recipeState)
            },
            this::showError
        )
*/
    override fun onResume() {
        super.onResume()
        recipePresenter.attachView(this)
    }

    override fun onPause() {
        super.onPause()
        recipePresenter.dettachView()
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }


}