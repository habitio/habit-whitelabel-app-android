package com.muzzley.app.userprofile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.muzzley.App
import com.muzzley.R
import com.muzzley.model.Preferences
import com.muzzley.model.user.Place
import com.muzzley.model.user.Places
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.startActivityForResult
import com.muzzley.util.ui.PlaceContainer
import com.muzzley.util.ui.ViewModel
import com.muzzley.util.ui.ViewModelAdapter
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_settings2.*
import timber.log.Timber
import javax.inject.Inject


class SettingsActivity : AppCompatActivity() , SwipeRefreshLayout.OnRefreshListener {

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var userService: UserService


    lateinit var adapter: ViewModelAdapter<ViewModel>
    enum class ViewState { LOADING, DATA, ERROR }

    var data: MutableList<UserVM> = mutableListOf()
    lateinit var settings: Observable<Settings>
    var cachedPlaces: List<Place>? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_settings2)

        adapter = ViewModelAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false)
        recyclerView.setHasFixedSize(false)
        recyclerView.adapter = adapter

        swipeRefreshLayout.apply {
            setOnRefreshListener(this@SettingsActivity)
            post { isRefreshing = true }
        }
        settings = Observable.zip(
                userService.getPreferences(),
                userService.getPlaces(),
//                Observable.just(Places()),//FIXME: disabled while v3 backend does not implement this
                BiFunction{ preferences: Preferences , places : Places ->
                    Settings(preferences = preferences, places = places.places ?: listOf())
                }
        )

        Timber.d("SettingsActivity run 1")
        onRefresh()
    }

    override
    fun onRestart() {
        super.onRestart()
        adapter.notifyDataSetChanged()
    }

    fun showState(state: ViewState) {
        viewFlipper.displayedChild = state.ordinal
    }

    class Settings(val preferences: Preferences, val places: List<Place> )

    @SuppressLint("CheckResult")
    override
    fun onRefresh() {

        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        settings.compose(RxComposers.applyIoRefresh(swipeRefreshLayout::setRefreshing))
                .subscribe(
                { sett ->
                    data = mutableListOf(
                            UserVM(layout = R.layout.section, label = getString(R.string.mobile_settings_header_units)),
                            UserVM(layout = R.layout.radio_settings, metric = sett.preferences.isMetric(), click = this::saveUnits),
                            UserVM(layout = R.layout.section, label = getString(R.string.mobile_settings_header_time)),
                            UserVM(layout = R.layout.toggle_container, label = getString(R.string.mobile_settings_time_24h), on = sett.preferences.is24hours, click = this::saveHours ),
                            UserVM(layout = R.layout.section, label = getString(R.string.mobile_settings_header_locations))
                    )

                    cachedPlaces = sett.places
                    cachedPlaces?.forEach { place ->
                        data.add(UserVM(layout = R.layout.place_container, place = place, click = { startActivityForResult<PlacesActivity>(45) { putExtra("place",it.place) }}))
                    }
                    data.add(UserVM(layout =  R.layout.link, label =  "+ "+getString(R.string.mobile_device_add_location), click =  { startActivityForResult<PlacesActivity>(45) { putExtra(PlacesActivity.ADD_LOCATION,true) } }))

                    data.add(UserVM(layout= R.layout.section, label = getString(R.string.mobile_settings_header_notifications)))
//                    data << [layout: R.layout.push_container, label: "Push Notification", on: sett.user.preferences.notifications.push.state, click: { Map map -> saveNotifications(map,"push") }]
                    data.add(UserVM(layout= R.layout.push_container, label= getString(R.string.mobile_push_notifications_title), on= preferencesRepository.push, click= this:: savePush))
//                    data << [layout: R.layout.toggle_container, label: getString(R.string.mobile_settings_notifications_email), on: sett.user.preferences.notifications.email.state, click: { Map map -> saveNotifications(map,"email") }]
//                    data << [layout: R.layout.toggle_container, label: getString(R.string.mobile_settings_notifications_sms), on: sett.user.preferences.notifications.sms.state, click: { Map map -> saveNotifications(map,"sms") }]

                    showState(ViewState.DATA)
                    adapter.setData(data)
                },{
                    Timber.e(it, "Error in settings")
                })
    }

    override
    fun onActivityResult(requestCode: Int , resultCode: Int , dataIntent : Intent? ) {
        super.onActivityResult(requestCode, resultCode, dataIntent )
        if (requestCode == 45 && resultCode == RESULT_OK) {
            val place: Place = dataIntent!!.getSerializableExtra("place") as Place

            if (dataIntent.getBooleanExtra("delete", false)) {
                if (PlaceContainer.drawables.containsKey(place.id)) { // special one
                    data.find { it.place?.id == place.id }
                            ?.place?.run {
                                address = null
                                latitude = 0.0
                                longitude =  0.0
                                wifi = null
                    }
                } else {
                    data.removeAll { it.place?.id == place.id}
                }
            } else {
                val data2update = data.find { it.place?.id == place.id }
                data2update?.let{
                    it.place = place
//                    data2update.click = { startActivityForResult(Intent(this,PlacesActivity).putExtra("place",place as Serializable),45) }
                } ?: run {
                    val idx = data.indexOfLast { it.place != null }
                    data.add(idx+1,
                            UserVM(
                                    layout= R.layout.place_container,
                                    place= place,
                                    click= { startActivityForResult<PlacesActivity>(45) { putExtra("place",place ) }}
                            )
                    )
                }
            }
            adapter.notifyDataSetChanged()
        }
    }


    fun saveUnits(vm: UserVM) {
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        val prefs = preferencesRepository.preferences
        prefs!!.setMetric(vm.metric == true)
        userService.updatePreferences(prefs)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .doOnTerminate{ swipeRefreshLayout.isRefreshing = false }
                .subscribe(
                        { Timber.d("saved") },
                        {
                            Timber.e(it, "Error saving settings")
                            FeedbackMessages.showError(viewFlipper)
                            vm.metric = vm.metric != true
                            prefs.setMetric(vm.metric != true)
                            adapter.notifyDataSetChanged()
                        }
                )
    }
    fun saveHours(vm: UserVM) {
        swipeRefreshLayout.post { swipeRefreshLayout.isRefreshing = true }
        val prefs = preferencesRepository.preferences
        prefs!!.is24hours = vm.on == true
        userService.updatePreferences(prefs)
                .compose(RxComposers.applyIo())
                .doOnTerminate{ swipeRefreshLayout.isRefreshing = false }
                .subscribe(
                        { Timber.d("saved") },
                        {
                            Timber.e(it, "Error saving settings")
                            FeedbackMessages.showError(viewFlipper)
                            vm.on = vm.on != true
                            prefs.is24hours = vm.on!!
                            adapter.notifyDataSetChanged()
                        }
                )
    }


    fun savePush(vm: UserVM) {
        preferencesRepository.push = vm.on == true
    }
}
