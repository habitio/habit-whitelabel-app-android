package com.muzzley.app.userprofile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.common.api.Status
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.jakewharton.rxbinding2.widget.RxTextView
import com.muzzley.App
import com.muzzley.R
import com.muzzley.model.user.Place
import com.muzzley.model.user.Wifi
import com.muzzley.services.LocationInteractor
import com.muzzley.util.*
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.ui.PlaceContainer
import com.muzzley.util.ui.ProgDialog
import com.patloew.rxlocation.Geocoding
import com.patloew.rxlocation.RxLocation
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.activity_places.*
import timber.log.Timber
import javax.inject.Inject
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity.RESULT_ERROR
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import io.reactivex.Observable
import com.google.android.libraries.places.api.model.Place as GPlace


class PlacesActivity : AppCompatActivity() , OnMapReadyCallback {

    companion object {
        const val ADD_LOCATION: String = "addLocation"
        const val PLACE_AUTOCOMPLETE_REQUEST_CODE: Int = 1
    }

    @Inject lateinit var userService: UserService
    @Inject lateinit var locationInteractor: LocationInteractor
    private lateinit var map: GoogleMap
    private lateinit var geocoding: Geocoding
    private lateinit var place: Place
    private val newLocation: Boolean by lazy { intent.getBooleanExtra(ADD_LOCATION,false)}

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_places)

//        newLocation = intent.getBooleanExtra(ADD_LOCATION,false)
        place = intent.getSerializableExtra("place") as? Place ?: Place()

        setTitle(if (newLocation) R.string.mobile_device_add_location else R.string.mobile_location_edit)

        mapView.onCreate(null)
        mapView.onResume()
        mapView.getMapAsync(this)
        geocoding = RxLocation(this).geocoding()

        if (place.wifi.isNullOrEmpty())
            place.wifi = listOf(Wifi())

        text.setText(place.name)

        PlaceContainer.drawables[place.id]?.let {
            text.isEnabled = false
            text.setCompoundDrawablesWithIntrinsicBounds(it,0,0,0)
        }

        RxTextView.textChanges(text).subscribe { place.name = it.toString() }

        iff(place.wifi!![0].ssid) {
            wifi.text = place.wifi!![0].ssid
            findWifi.setImageResource(R.drawable.icon_clean)
        } ?:
            findWifi.setImageResource(R.drawable.ic_find_wifi_round)

        wifi.text = place.wifi!![0].ssid

        findWifi.setOnClickListener {
            place.wifi!![0].let {
                iff (it.ssid) {
                    wifi.text = null
                    findWifi.setImageResource(R.drawable.ic_find_wifi_round)
                    place.wifi = listOf(Wifi())
                } ffi {
                    val wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
                    val wifiInfo = wifiManager.connectionInfo
                    it.ssid = wifiInfo.ssid.replace("""^"(.*)"$""".toRegex(), "$1")
                    it.bssid = wifiInfo.bssid
                    Timber.d("SSID: ${it.ssid} , BSSID: ${it.bssid}")
                    wifi.text = it.ssid
                    findWifi.setImageResource(R.drawable.icon_clean)
                }
            }
        }

        address.text = place.address
        address.setOnClickListener{
            try {
                startActivityForResult(
                        Autocomplete.IntentBuilder(
                                AutocompleteActivityMode.OVERLAY,
                                listOf(
                                        GPlace.Field.ID,
                                        GPlace.Field.NAME,
                                        GPlace.Field.LAT_LNG,
                                        GPlace.Field.ADDRESS
                                )
                        ).build(this),
                        PLACE_AUTOCOMPLETE_REQUEST_CODE
                )

            } catch (e: GooglePlayServicesRepairableException) {
                // TODO: Handle the error.
            } catch (e: GooglePlayServicesNotAvailableException) {
                // TODO: Handle the error.
            }
        }

//        Timber.d("color : ${android.R.color.white}, ${Color.parseColor("#ffffff")}, ${getColor(android.R.color.white)}")

//        val pb: ProgressBar = ProgressBar(this)
//        if (val >: Build.VERSION.SDK_INT = 21){
//            val csl: ColorStateList = ColorStateList(
//                    [[]] as int[][],
////                [ android.R.color.white ] as int[]
//                    [Color.parseColor("#ffffff") ] as int[]
//            )
//            pb.setIndeterminateTintList(csl)
//        }
//        pb.setIndeterminate(true)
//        progressDialog = AppCompatDialog(this,R.style.full_screen_dialog)
//        progressDialog.setCancelable(false)
//        progressDialog.setContentView(pb)

        submit.setOnClickListener { submit(false) }

    }

    fun submit(delete: Boolean) {

        if (text.text.isEmpty()) {
            text.requestFocus()
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
            return
        }

        val localPlace: Place = place.clone()
        ifno (localPlace.wifi!![0].ssid) {
            localPlace.wifi = listOf()
        }
        val pl =
                when {
//                    delete -> userService.deletePlace(localPlace.id).map { localPlace }
                    delete ->
                        RxDialogs.confirm(
                                this,
                                null,
                                getString(R.string.mobile_settings_delete_location_dialog_text),
                                getString(R.string.mobile_delete),
                                getString(R.string.mobile_cancel)
                        )
                        .filter { it }
                        .flatMap { userService.deletePlace(localPlace.id).map { localPlace } }
                    place.id != null -> userService.setPlace(localPlace.id, localPlace).map { localPlace }
                    else -> userService.setPlace(localPlace).map { localPlace.id = it.id; localPlace }
                }

        pl.compose(RxComposers.applyIoRefresh(ProgDialog.getLoader(this)))
            .subscribe(
                {
//                    progressDialog.cancel()
                    setResult(RESULT_OK, Intent().putExtra("place", it as java.io.Serializable).putExtra("delete", delete))
                    finish()
                }, {
                    FeedbackMessages.showError(address)
                }
            )
    }


    override
    fun onCreateOptionsMenu(menu: Menu) : Boolean {
        if (!newLocation) {
            menuInflater.inflate(R.menu.delete, menu)
        }
        return true
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            R.id.menu_delete -> {
                submit(true)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                RESULT_OK -> {
                    data?.let {
                        val place = Autocomplete.getPlaceFromIntent(it)
                        Timber.d("place: $place")
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom( place.latLng,13f))
                        address.text = place.name
                        this.place.address = place.name.toString()
                    }
//                    val place: com.google.android.gms.location.places.Place = PlaceAutocomplete.getPlace(this, data)
//                    //                mAddress.setText("${place.address} ; ${place.name}")
//                    map.moveCamera(CameraUpdateFactory.newLatLngZoom( place.latLng,13f))
//                    address.text = place.name
//                    this.place.address = place.name.toString()
                }
                RESULT_ERROR -> {
                    data?.let {
                        val status: Status = Autocomplete.getStatusFromIntent(it)
                        Timber.e("place autocomplete error status: ${status.statusMessage}")
                    }
//                    val status: Status = PlaceAutocomplete.getStatus(this, data)
//                    Timber.e("place autocomplete error status: $status")
                }

            // TODO: Handle the error.
                RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override
    fun onMapReady(map: GoogleMap) {
        this.map = map
        MapsInitializer.initialize(this)
        if (place.latitude != 0.0|| place.longitude != 0.0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom( LatLng(place.latitude, place.longitude),13f))
        } // else current location ?



        locationInteractor.requestLocation(this)
                .subscribe(
                    {
                        Timber.d("""Location active: $it""")
                        if (it) {
                            map.isMyLocationEnabled = true
                            map.uiSettings.isMyLocationButtonEnabled = true
                        }
                    }, {
                        Timber.e(it,"Error getting permission or location")
                    }
                )

        mapPositionLatLng(map)
            .doOnNext { place.latitude = it.latitude ; place.longitude = it.longitude}
            .observeOn(Schedulers.io())
            .flatMapMaybe {
                geocoding.fromLocation(it.latitude,it.longitude)
            }
            .map {
                (0..it.maxAddressLineIndex).map { i -> it.getAddressLine(i) }.joinToString("\n")
            }
//            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    place.address = it
                    address.text = it
                },
                {
                    FeedbackMessages.showMessage(address,"Error getting address")
                    Timber.d(it, "Error getting address")
                }
            )


    }

    fun mapPositionLatLng(map: GoogleMap) =
            Observable.create<LatLng>{ emitter ->
                map.setOnCameraIdleListener {
                    Timber.d("new position")
                    emitter.onNext(map.cameraPosition.target)
                }
            }
}
