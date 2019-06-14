package com.muzzley.services

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import android.util.StringBuilderPrinter
import com.google.android.gms.location.*
import com.muzzley.Constants
import com.muzzley.app.location.Loc
import com.muzzley.app.location.LocationService
import com.muzzley.app.receivers.LocationReceiver2
import com.muzzley.model.workers.Fence
import com.muzzley.util.*
import com.muzzley.util.retrofit.MuzzleyCoreService
import com.muzzley.util.rx.LogCompletableObserver
import com.patloew.rxlocation.RxLocation
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Single
import timber.log.Timber
import java.net.UnknownHostException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class LocationInteractor

    @Inject constructor(
        val context: Context,
        val preferencesRepository: PreferencesRepository,
        val muzzleyCoreService: MuzzleyCoreService
    ){

    companion object {
////        val UPDATE_INTERVAL = TimeUnit.MILLISECONDS.convert(2, TimeUnit.MINUTES)
//        val UPDATE_INTERVAL = TimeUnit.MINUTES.toMillis(2)
//        val FASTEST_INTERVAL = TimeUnit.MILLISECONDS.convert(1, TimeUnit.MINUTES)
//        val MAX_WAIT_TIME = UPDATE_INTERVAL * 6
////        val DISPLACEMENT_INTERVAL_IN_METERS = 1000f
//        val DISPLACEMENT_INTERVAL_IN_METERS = 200f
        val GEOFENCE_EXPIRATION = TimeUnit.MILLISECONDS.convert(7,TimeUnit.DAYS)
    }



//    private Context context

//    @Inject LocationInteractor(Context context) {
//        this.context = context
//    }

//    @SuppressLint("MissingPermission")
//    fun requestLocationUpdates(): Unit {
//        if (hasLocationPermission()) {
//            LocationServices
//                    .getFusedLocationProviderClient(context)
//                    .requestLocationUpdates(createLocationRequest(), getPendingIntent(context))
//            Timber.d("Requested location updates")
//
//        } else {
//            Timber.d("No location permissions, so not requesting")
//        }
//    }


    fun hasLocationPermission(): Boolean =
        PermissionChecker.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

//    fun removeLocationUpdates() {
//        if (hasLocationPermission()) {
//            LocationServices
//                    .getFusedLocationProviderClient(context)
//                    .removeLocationUpdates(getPendingIntent(context));
//        } else {
//            Timber.d("No location permissions, so not requesting");
//        }
//    }

//    private fun createLocationRequest(): LocationRequest =
//        LocationRequest.create().apply {
//            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
//            interval = UPDATE_INTERVAL
//            fastestInterval = FASTEST_INTERVAL
//            smallestDisplacement = DISPLACEMENT_INTERVAL_IN_METERS
//            maxWaitTime = MAX_WAIT_TIME
////            maxWaitTime = UPDATE_INTERVAL * 3
//        }

//    private fun getPendingIntent(context: Context ) =
//        PendingIntent.getBroadcast(context,
//                4321,
////                Intent(Constants.ACTION_LOCATION),
//                Intent(context,LocationReceiver2::class.java).setAction(Constants.ACTION_LOCATION),
//                PendingIntent.FLAG_UPDATE_CURRENT
//        )

    // ===================== geofences ====================

    fun toGeofence(fence: Fence): Geofence  =
        Geofence.Builder()
                .setRequestId(fence.id)
                .setCircularRegion(fence.latitude?: 0.0,fence.longitude?: 0.0,fence.getRadiusMeters()) //FIXME: review this defaults
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(GEOFENCE_EXPIRATION)
                .build()

    private fun getGeofencingRequest(fences: Collection<Fence>): GeofencingRequest =
        GeofencingRequest.Builder().apply {
            setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
            addGeofences( fences.map{ toGeofence(it)})
        }.build()

    private fun getAllFences(): List<Fence> =
            preferencesRepository.fences ?: emptyList()

    private fun setFences(fences: List<Fence>? ){
        preferencesRepository.fences = fences
    }

    private fun getGeofencePendingIntent() =
        PendingIntent.getBroadcast(context,
                4322,
                Intent(context,LocationReceiver2::class.java).setAction(Constants.ACTION_GEOFENCE),
                PendingIntent.FLAG_UPDATE_CURRENT
        )

    fun registerAllGeofences(){
        Timber.d("registering All Geofences")
//        def fences = getAllFences()
//        if (fences) {
//            addGeofences(fences)
//        }
        addGeofences(getAllFences())
    }

    fun unregisterAllGeofences(){
        if (hasLocationPermission()) {
            LocationServices.getGeofencingClient(context).removeGeofences(getGeofencePendingIntent())
            setFences(null)
        } else {
            Timber.d("No location permissions, so not updating")
        }
    }

    fun  registerGeofences(fences: List<Fence> ){
        Timber.d("registering fences: ${fences.map{ it.id }}")
        if (fences.isNotEmpty()) {
            val currFences = getAllFences()

            val fences2add = fences - currFences

            if (fences2add.isNotEmpty()) {
//                def matches = fences.any { it.id in currFences*.id}
//                def f = fences2add[0]
//                currFences.each {
//                    def m = (it.id == f.id)
//                    def e = it.equals(f)
//                    Timber.d("matches: $m, equals $e")
//                }
//                Timber.d("really new fences: ${fences2add*.id}, ${fences2add*.hashCode()}, $matches ")
                addGeofences(fences2add)

//                currFences.addAll(fences2add)
//                setFences(currFences)

                setFences(currFences+fences2add)
            }
        }
    }

    fun unregisterGeofences(fences: List<Fence> ){
        if(fences.isNotEmpty()) {
            removeGeofences(fences)
            setFences(getAllFences() - fences)
        }

    }

    @SuppressLint("MissingPermission")
    private fun addGeofences(fences: Collection<Fence>) {
        Timber.d("GGG Adding geofences")
        helper(fences) {
            it.addGeofences(getGeofencingRequest(fences), getGeofencePendingIntent())
                    .addOnSuccessListener{ Timber.e("GGG geofence added")}
                    .addOnFailureListener {Timber.e(it,"GGG geofence not added") }

        }
    }
    private fun removeGeofences(fences: Collection<Fence>) {
        helper(fences) {it.removeGeofences(fences.map {it.id })}
    }
    private fun helper(fences: Collection<Fence>?, block: (GeofencingClient) -> Unit) {
        if (hasLocationPermission()) {
            if (fences != null && fences.isNotEmpty()) {
                block(LocationServices.getGeofencingClient(context))
            } else {
                Timber.d("Not removing empty fences list")
            }
        } else {
            Timber.d("No location permissions, so not doing anything")
        }
    }

    // ==================================
    fun sendLocation(foreGroundService: Boolean , vararg locations: Location ) {


        locations.forEachIndexed { index, location ->

            val sb = StringBuilder()
            location.dump(StringBuilderPrinter(sb), "Location")
            Timber.d("Location: $sb")

            val loc = with(location) {
                Loc(
                        latitude = latitude, //double
                        longitude = longitude, // double
                        provider = provider, // string
                        timestamp = Date(time),
                        elapsed_realtime_nanos = elapsedRealtimeNanos,
//                        user_agent = System.getProperty("http.agent", "NA"),
                        device_id = preferencesRepository.customerUserId,
                        horizontal_accuracy = iff(hasAccuracy()) { accuracy /*float*/ },
                        altitude = iff(hasAltitude()) { altitude /*double*/ },
                        bearing = iff(hasBearing()) { bearing /*float*/ },
                        speed = iff(hasSpeed()) { speed /* float */ },
                        satellites = iff(extras?.containsKey("satellites") == true) { extras.getInt("satellites") },
                        total_satellites = iff(extras?.containsKey("total_satellites") == true) { extras.getInt("total_satellites") },
                        vertical_accuracy = if (api26() && hasVerticalAccuracy()) {
                            verticalAccuracyMeters /* float */
                        } else null,
                        bearing_accuracy = if (api26() && hasBearingAccuracy()) {
                            bearingAccuracyDegrees /* float */
                        } else null,
                        speed_accuracy = if (api26() && hasSpeedAccuracy()) {
                            speedAccuracyMetersPerSecond /* float */
                        } else null
                )
            }

            preferencesRepository.lastKnownLocation = com.muzzley.model.productDetails.Location(location.latitude, location.longitude)

//        val payload = mapOf(
//                "data" to mapOf(
//                        "latitude" to location.latitude,
//                        "longitude" to location.longitude
//                )
//        )

//            val payload = mapOf("data" to locationMap)
            val payload = mapOf("data" to loc)

            Timber.d("location: ${payload.toJsonString()}")

            muzzleyCoreService.sendProperty(
                    preferencesRepository.userChannelId,
                    "location",
                    "location",
                    payload)
//                    .subscribe(LogCompletableObserver("location send"))
//                    .andThen(Completable.error(RuntimeException("fake error")))
//                    Completable.error(RuntimeException("fake error"))
                    .subscribe(
                            {
                                Timber.d("Location sent")
                            },
                            {
                                if(it is UnknownHostException) {
                                    Timber.e("error sending location: ${it.message}")
                                } else {
                                    Timber.e(it,"error sending location")
                                }
                                if (foreGroundService) {
                                    ContextCompat.startForegroundService(
                                            context,
                                            Intent(context,LocationService::class.java)
                                                    .putExtra("property","location")
                                //                                                .putExtra("payload",payload as Serializable)
                                                    .putObjectExtra("payload",payload)
                                    )
                                }
                            }
                    )

            //deprecate this in the future
            if (index == locations.size-1) {
                muzzleyCoreService.sendProperty(
                        preferencesRepository.userChannelId,
                        "location",
                        "latlon",
                        payload)
                        .subscribe(LogCompletableObserver("location send"))
            }


        }
    }

    fun api26() = Build.VERSION.SDK_INT >= 26


    @SuppressLint("CheckResult")
    fun handleIntent(intent: Intent) {
        Timber.d("GGG intent: ${intent.toUri(0)}")
//        val bundle = intent.getExtras();
//        if (bundle != null) {
//            for (String key : bundle.keySet()) {
//                Object value = bundle.get(key);
//                Timber.d("GGG intent extras "+ String.format("%s %s (%s)", key, value.toString(), value.getClass().getName()));
//            }
//        }

        when (intent?.action) {
//            Constants.ACTION_LOCATION -> {
//                LocationResult.extractResult(intent)?.locations?.toTypedArray()?.let {
//                    Timber.d("Extracted LocationResult(s)")
//                    if (it.size > 1) {
//                        Timber.d("Got a batch of ${it.size}")
//                    }
//                    sendLocation(true,*it)
//                } ?: Timber.d("No locations found with LocationResult")
//            }
            Constants.ACTION_GEOFENCE ->
                with(GeofencingEvent.fromIntent(intent)) {
                    when {
                        this == null -> Timber.e("Null geofence intent")
                        hasError() -> Timber.e(GeofenceStatusCodes.getStatusCodeString(errorCode))
                        else -> sendLocation(true,triggeringLocation)
                    }
                }
        }

//        RxGps2(context).timedSingleLocationWithSatellites()
//                .subscribeOn(AndroidSchedulers.mainThread())
//                .subscribe(
//                        {
//                            with(it.extras) {
//                                Timber.d("fixed: ${getInt("satellites")} total: ${getInt("total_satellites")}")
//                                sendLocation(true,it)
//                            }
//                        },
//                        rxerr("Error getting Single location")
//                )
//        Awareness.snapshot(false)
//        ConnectivityInteractor.pollConnectivity(false)
    }

    private fun locationSettings(context: Context): Single<Boolean> =
            RxLocation(context)
                    .settings()
                    .checkAndHandleResolution(
                            LocationRequest.create()
                                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                    .setNumUpdates(5)
                                    .setInterval(100)
                    )

    fun requestLocation(activity: Activity): Single<Boolean> =
            RxPermissions(activity)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION)
                    .first(false)
                    .flatMap {
                        if (it) {
                            locationSettings(activity)
                        } else {
                            Single.just(false)
                        }
                    }
    fun hasLocation(activity: Activity): Boolean =
            RxPermissions(activity).isGranted(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    activity.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)

}