package com.muzzley.app.profiles

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import com.google.gson.JsonObject
import com.muzzley.App
import com.muzzley.R
import com.muzzley.app.ProfilesActivity
import com.muzzley.app.auth.AuthActivity
import com.muzzley.app.recipes.RecipeView
import com.muzzley.model.profiles.Profile
import com.muzzley.model.Subscription
import com.muzzley.model.channels.Channel
import com.muzzley.model.profiles.DiscoveryRequiredCapability
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.*
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.ProgDialog
import com.muzzley.util.ui.ViewModelAdapter
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Scheduler
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.activity_bundlecards.*
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class BundleCardsActivity : AppCompatActivity() {

    @Inject lateinit var bundleFlow: BundleFlow
    @Inject lateinit var channelService: ChannelService
    @Inject lateinit var userService: UserService
    @Inject lateinit var realtime: Realtime
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject @field:Named("main") lateinit var mainScheduler: Scheduler
    @Inject lateinit var fibaroApi: FibaroApi
    @Inject lateinit var bundleInteractor: BundleInteractor
    @Inject lateinit var profilesInteractor: ProfilesInteractor

//    @InjectView(R.id.recyclerview) lateinit var recyclerView: RecyclerView
//    @InjectView(R.id.next) lateinit var nextBtn: View

//    List<Map> bundleViewModels
    lateinit var bundleViewModels: List<BundleVM>
    lateinit var adapter: ViewModelAdapter<BundleVM>
//    static enum AddState { idle, running, input, error, finished}
    val disposable = CompositeDisposable()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_bundlecards)

        adapter = ViewModelAdapter(this)
        recyclerView.adapter = adapter
        recyclerView.layoutManager!!.setAutoMeasureEnabled(true)


        bundleViewModels = bundleFlow.bundle?.profiles?.mapIndexed{ i, prof ->
            BundleVM(
                    layout = R.layout.profile_channels_card,
                    profile = prof,
                    state = AddState.idle,
                    click = onAction(i),
                    idx = i
            )
        } ?: listOf()

        val a = bundleViewModels.map { it.validRx }
        disposable += Observable.combineLatest(a) {
            it.all { it == true }
        }.subscribe {
            nextBtn.isEnabled = it
        }



        adapter?.setData(bundleViewModels)

        bundleFlow.currBundleState = BundleFlow.BundleState.profiles

        nextBtn.setOnClickListener {
            submit()
        }

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item);
    }

    fun submit() {
        val fibaros = arrayListOf(getString(R.string.fibaro_smoke_profile_id), getString(R.string.fibaro_motion_profile_id))

        val subscriptions = bundleViewModels.map { bundleVM ->

            val sc = bundleVM.channelData?.foldIndexed(mutableListOf<Subscription.Channel>()) { idx, acc, el ->
                if (el.selected == null || el.selected == true) {
                    bundleVM.channels?.getOrNull(idx)?.let {
                        acc.add(Subscription.Channel(it.id, it.content, it.activity))
                    }
                }
                acc
            }
            Subscription(bundleVM.profile?.id, sc)
        }.filter{ !fibaros.contains(it.profile)}


//        val subscriptions = bundleViewModels.map {
//            val chIdxs = it.channelData.findIndexValues { (it as ChannelVM).selected in [true,null] }
//            Subscription(it.profile.id, it.channels[chIdxs].map { Subscription.Channel(it.id,it.content, it.activity)})
//        }
//        .filter{ !fibaros.contains(it.profile)}
        //FIXME: change this into recipes

        val  fff = bundleViewModels.filter{ it.profile?.id in fibaros }
                .flatMap{ bundleVM ->
                    emptyList<ObservableSource<String>>()

                    val maps = bundleVM.channelData?.foldIndexed(mutableListOf<Map<String,Any>>()){ i, acc, el ->
                        if (el.selected == null || el.selected == true) {

                            bundleVM?.fibaro?.getOrNull(i)?.let {
                                acc.add(it)
                            }
                        }
                        acc
                    }
//                    maps
//                            ?:emptyList<ObservableSource<String>>()
//                    val chIdxs = bundleVM.channelData.findIndexValues { it.selected != false }
//                    Timber.d("published idxs: $chIdxs")
//                    val maps = (bundleVM.fibaro as List<Map<String,Object>>)[chIdxs]

                    maps?.flatMap {
                        val sensor = it["sensor"] as Map<String,String>
                        val deviceId = it["device_id"] as? String
                        val manufacturerName = sensor["name"]
                        val manufacturerId = sensor["id"]
                        if (deviceId != null && manufacturerName != null && manufacturerId != null) {
                            listOf(
                                    bundleInteractor.askApplicationGrant(deviceId, manufacturerName, manufacturerId),
                                    bundleInteractor.askUserGrant(deviceId, manufacturerName, manufacturerId)
                                    )
                        } else {
//                            Observable.empty<JsonObject>()
                            emptyList<Observable<JsonObject>>()
                        }
                    } ?:
                    emptyList<ObservableSource<String>>()
        }

        showLoading(true)
        disposable += Observable.concat(fff + arrayListOf(bundleInteractor.postAllSubscriptions(subscriptions)))
    //            Observable.concat(arrayListOf(bundleInteractor.postAllSubscriptions(subscriptions)))
        .doOnNext { Timber.d("Got some subscriptions list $it")}
    //            postAllSubscriptions(subscriptions)
        .observeOn(mainScheduler)
        .subscribe(
                {
                    Timber.d("Got from subscriptions: $it")
                },
                {
                    showLoading(false)
                    showError(it)
                },
                {
                    showLoading(false)
                    Timber.d("finished subscriptions")
                    bundleFlow.currBundleState = BundleFlow.BundleState.profiles
                    BundleNavigator.navigateTo(this,bundleFlow.nextBundleState)
                }

        )
    }

    fun showError(t: Throwable) {
        Timber.e(t, "showError")

        val text = getString( if (t is RepositoryException && t.code == 5000 )  R.string.mobile_error_code_5000 else  R.string.mobile_error_text);
        FeedbackMessages.showMessage(recyclerView,text)
    }

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Timber.d("AAA onActivityResult ${requestCode}")
        if (requestCode >= 0 ) {
            if (resultCode == RESULT_OK) {
                Timber.d("AAA result OK")
                updateChannels(requestCode) // this runs before onResume, so it's OK
            } else {
                Timber.d("AAA result NOK")
                recipeError(RuntimeException("Result NOK"),requestCode)
            }
        }

    }

    fun onAction(idx: Int) =
            View.OnClickListener {
                doAction(idx)
            }
//    Closure onAction(idx: Int) {
//        return { doAction(idx)} as Closure
//    }

    fun doAction(idx: Int) {
//        return {
            Timber.d("processing idx: $idx")
            val bundleVm = bundleViewModels[idx]
            bundleVm.state = AddState.running
            val profile = bundleVm.profile

            adapter.notifyItemChanged(idx) // to trigger refresh state

            if (profile.id == getString(R.string.fibaro_smoke_profile_id)) {
                Timber.d("It's fibaro smoke")
                disposable += fibaroApi.discoverLocalSmoke()
                        .observeOn(mainScheduler)
                        .subscribe(
                        {   maps ->
                            Timber.d("fibaro smoke: $maps")

                            bundleVm.channels = maps.map {
                                Channel().apply {
                                    id = it["device_id"] as? String
                                    content =  (it.get("sensor") as? Map<String,Any>)?.get("name") as? String
                                    photoUrl = "http://www.vesternet.com/media/wysiwyg/Fibaro/Smoke-Sensor_Many_Possibilities_3.jpg"
                                }
                            }
                            bundleVm.state = AddState.finished
                            bundleVm.fibaro = maps
                            adapter.notifyItemChanged(idx)
                        } ,
                        {
                            Timber.e(it,"fibaro smoke error")
                            recipeError(it,idx)
                        }
                )
            } else if (profile.id == getString(R.string.fibaro_motion_profile_id)) {
                Timber.d("It's fibaro motion")
                fibaroApi.discoverLocalMotion()
                        .observeOn(mainScheduler)
                        .subscribe(
                        {   maps ->
                            Timber.d("fibaro motion: $maps")

                            bundleVm.channels = maps.map {
                                Channel().apply {
                                    id = it["device_id"] as? String
                                    content =  (it.get("sensor") as? Map<String,Any>)?.get("name") as? String
                                    photoUrl = "http://www.diyhomeautomation.com.au/261-large_default/fibaro-z-wave-multi-sensor.jpg"
                                }
                            }
                            bundleVm.state = AddState.finished
                            bundleVm.fibaro = maps
                            adapter.notifyItemChanged(idx)
                        } ,
                        {
                            Timber.e(it,"fibaro motion error")
                            recipeError(it,idx)
                        }
                )
            } else if (profile.requiredCapability == DiscoveryRequiredCapability.discovery_recipe) {
                val lv = CardRecipeView(this,idx)
                profilesInteractor.getProfileAuthorizationUrlRequest(profile.id)
                        .compose(RxComposers.applyIo())
//                        .map { it.url = it.url.replace("muzdiscovery","http") ; it}
                        .map { it.copy(url = it.url.replace("muzdiscovery","http")) }
                        .subscribe(
                        { lv.startRecipe(it.url)},
                        { lv.recipeError(it)}
                )

            } else {
                profilesInteractor.getProfileAuthorizationUrlRequest(profile.id)
                        .compose(RxComposers.applyIo())
                        .subscribe(
                        {
                            Timber.d("Got url: ${it.url}")
                            startActivityForResult(Intent(this, AuthActivity::class.java)
                                    .putExtra(ProfilesActivity.URL_REQUEST, it)
                                    .putExtra(ProfilesActivity.WITH_FOOTER,false),
                                    idx)
                        } ,
                        { recipeError(it,idx)}
                )
            }
//        } as Closure
    }


    override
    fun onResume() {
        super.onResume()
//        bundleViewModels.find { it.state == AddState.idle }?.apply { (it.click as Closure)() }
        bundleViewModels.find { it.state == AddState.idle }?.run { click?.onClick(null) }
    }


    fun updateChannels(position: Int) {
        val bundleVm = bundleViewModels[position]
        val profile = bundleVm.profile as Profile
        channelService.getProfileChannels(profile.id)
                .compose(RxComposers.applyIo())
                .subscribe(
                {
                    bundleVm.channels = it
                    bundleVm.state = AddState.finished
                    adapter.notifyItemChanged(position)
                } ,
                { recipeError(it, position) }
        )
    }

    var progressDialog: ProgressDialog? = null
//    synchronized
    fun showLoading(show: Boolean) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = ProgDialog.show(this as Context)
            }
        } else {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }


    fun recipeError(throwable: Throwable, position: Int) {
        Timber.d(throwable,"Recipe error in position: $position. bundleViewModels.size = ${bundleViewModels.size}")
        if (position >= bundleViewModels.size)
            return
        bundleViewModels[position].error = throwable
        bundleViewModels[position].state = AddState.error
        adapter.notifyItemChanged(position)
    }

    fun showTotalSteps(totalSteps: Int, position: Int) {
        bundleViewModels[position].totalSteps = totalSteps
        adapter.notifyItemChanged(position)
    }

    fun showStepNumber(stepNo: Int, position: Int) {
        bundleViewModels[position].stepNo = stepNo
        adapter.notifyItemChanged(position)
    }

    fun showStepTitle(title: String, position: Int) {
        bundleViewModels[position].stepTitle = title
        adapter.notifyItemChanged(position)
    }


    //this should be a "model" and not tied to real view, because we don't want to loose state
    //when views are being rebound in recyclerView
    inner class CardRecipeView(context: Context,val position: Int) : RecipeView {

        @Inject lateinit var recipeInteractor: RecipeInteractor
//        RecipeInteractorMock recipeInteractor = RecipeInteractorMock()

        init {
            App.appComponent.inject(this)
            recipeInteractor.view = this
        }

        fun startRecipe(url: String) {
            Timber.d("BBB startRecipe")
            recipeInteractor.startRecipe(url)
        }

        override
        fun getAuthenticationCode(): Observable<String>  {
            throw UnsupportedOperationException()
        }

        override
        fun recipeSuccess() {
            Timber.d("BBB recipeSuccess $position")
            updateChannels(position)
        }

        override
        fun recipeError(throwable: Throwable) {
            Timber.e(throwable,"BBB recipeError")
            recipeError(throwable, position)
        }

        override
        fun showLoading(b: Boolean) {

        }

        override
        fun showTotalSteps(totalSteps: Int) {
            showTotalSteps(totalSteps, position)
        }

        override
        fun showStepNumber(stepNo: Int) {
            showStepNumber(stepNo,position)
        }

        override
        fun showStepTitle(title: String) {
            showStepTitle(title,position)
        }
    }

//    class RecipeInteractorMock {
//        var view: RecipeInteractor.RecipeView
//
//        fun startRecipe(String url){
//            view.showLoading(true)
//            view.showTotalSteps(3)
//
//            Observable.interval(2,TimeUnit.SECONDS)
//            .take(3)
//            .observeOn(AndroidSchedulers.mainThread())
////            .doAfterTerminate{ view.showLoading(false)}
////            .doOnNext{ view.showStepNumber((it as int)+1)}
////            .doOnError{ view.recipeError(it)}
////            .doOnCompleted{view.recipeSuccess()}
////            .subscribe(
////                    {view.recipeSuccess()},
////                    {view.recipeError(it)},
////                    {view.showStepTitle("Step title ${it+1}") ; view.showStepNumber((it as int)+1) }
////            )
//            .subscribe(Observer<Long>() {
//                override
//                fun onComplete() {
//                    view.recipeSuccess()
//                }
//
//                override
//                fun onError(Throwable e) {
//                    view.recipeError(e)
//                }
//
//                override
//                fun onSubscribe(@NonNull Disposable d) {
//                }
//
//                override
//                fun onNext(Long l) {
//                    view.showStepTitle("Step title ${l+1}")
//                    view.showStepNumber((l as int)+1)
//                }
//            })
//
//        }
//    }

}

