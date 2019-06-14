package com.muzzley.app.workers

import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.fragment.app.Fragment
import androidx.core.app.NotificationManagerCompat
import androidx.core.os.ConfigurationCompat
import com.google.gson.Gson
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.receivers.CallReceiver
import com.muzzley.app.receivers.SmsReceiver
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.model.workers.Fence
import com.muzzley.model.workers.Worker
import com.muzzley.model.workers.WorkerUnit
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.CapabilityPermissionMap
import com.muzzley.util.Time
import com.muzzley.util.isNullOrEmpty
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by bruno.marques on 27/11/15.
 */
class WorkersController(agentsFragment: Fragment) {


    @Inject lateinit var gson: Gson
    @Inject lateinit var modelsStore: ModelsStore
    @Inject lateinit var preferencesRepository: PreferencesRepository

    val context:Context =  agentsFragment.context!!
    val packageManager: PackageManager = context.packageManager
    val manager: LocationManager = context.getSystemService( Context.LOCATION_SERVICE ) as LocationManager
    val locale = ConfigurationCompat.getLocales(context.resources.configuration)[0]
    var locationDisabled: Boolean = false
    var notificationsDisabled: Boolean = false



    init {
        App.appComponent.inject(this)
    }

    fun updateWorkers(workers: List<Worker>){
        locationDisabled = !manager.isProviderEnabled( LocationManager.GPS_PROVIDER )
        notificationsDisabled = !NotificationManagerCompat.from(context).areNotificationsEnabled()
        workers.forEach { updateWorker(it) }
    }

    //to be run after an update
    fun configurePhoneServices(workers: List<Worker>) {

        fun enableFeature(feature: String, clazz: Class<out BroadcastReceiver>) {
            val enabled = workers.any { worker ->
                worker.requiredCapabilities.any { it.contains(feature)} && worker.missingCapabilities.all{ !it.contains(feature)}
            }
            val componentName = ComponentName(context, clazz)
            packageManager.setComponentEnabledSetting(componentName,
                    if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP)
        }
        enableFeature("sms", SmsReceiver::class.java)
        enableFeature("call", CallReceiver::class.java)
    }

    fun requiresPhone(worker: Worker): Boolean =
        Constants.AGENTS_COMPONENT_PHONE == worker.triggers.getOrNull(0)?.component

    fun updateModelValidity(worker: Worker): Worker {

        if (worker.invalid) {
            Timber.d("worker $worker.id invalid because server says so")
        } else if (worker.editable && worker.triggers.isNullOrEmpty()) {
            Timber.d("worker $worker.id invalid because triggers empty")
            worker.invalid = true
        } else {
            worker.apply {
                invalid = (triggers + actions + states + unsorted).any {
                    updateValidity(worker, it)
                }
                if (!invalid) {
                    missingCapabilities = requiredCapabilities - (preferencesRepository.muzzCapabilities ?: emptySet())
                    missingPermissions  -= preferencesRepository.muzzDevicePermissions ?: emptySet()
                    if (requiredCapabilities.contains(Constants.MUZ_CAP_LOCATION_GPS))
                        locationDisabled = this@WorkersController.locationDisabled
                    if (requiredCapabilities.contains(Constants.MUZ_CAP_PUSH_NOTIFICATIONS)) {
                        notificationsDisabled = this@WorkersController.notificationsDisabled
                    }
                    if(missingCapabilities.isNotEmpty()) Timber.d("missing capabilities = $missingCapabilities")
                    if(missingPermissions.isNotEmpty())  Timber.d("missing permissions = $missingPermissions")
                }
            }
        }
        return worker
    }


    fun updateValidity(worker: Worker, workerUnit: WorkerUnit): Boolean  {
        if (workerUnit.isInvalid == true) {
            Timber.d("worker $worker.id invalid because WorkerUnit invalid")
        } else if ( modelsStore.getTileAgents(workerUnit.profile, workerUnit.channel, workerUnit.component) == null) {
            Timber.d("worker $worker.id invalid because didn't find tile: prof: $workerUnit.profile, ch: $workerUnit.channel, comp: $workerUnit.component ")
        } else {
            val prop = modelsStore.modelsStates?.getChannelProperty(workerUnit.channel!!, workerUnit.property!!)
            if (prop == null) {
                Timber.d("worker $worker.id invalid because didn't find prop: prof: $workerUnit.profile, ch: $workerUnit.channel, prop: $workerUnit.property ")
            } else {
                worker.requiredCapabilities += prop.requiredCapabilities
                worker.missingPermissions += prop.requiredCapabilities.flatMap{ tileCapa ->
                    CapabilityPermissionMap.getListPermissionsFromCapability(tileCapa) ?: listOf()
                }

                return false // because this gets checked afterwards
            }
        }
        return true
    }

    fun parseLocation(worker: Worker): Boolean =
        worker.run {
            (triggers + states + unsorted).any {
                if (it.component == Constants.AGENTS_COMPONENT_LOCATION) {
                    fence = getFence(it)?.apply {
                        id = worker.id
                    }
                    if (fence != null) {
                        return@any true
                    }
                }
                false
            }
        }


    fun getFence(workerUnit: WorkerUnit): Fence? {

        // choices*.choice.fence.first
        try {

//            for (JsonElement json : workerUnit.getChoices().getAsJsonArray()) {
            workerUnit?.choices?.asJsonArray?.forEach { json ->
                if (json.isJsonObject()) {
                    var jsonObject = json.getAsJsonObject();
                    val choice = jsonObject.get("choice");
                    if (choice != null && choice.isJsonObject()) {
                        jsonObject = choice.getAsJsonObject();
                        if (jsonObject.has("fence")) {
                            return gson.fromJson(jsonObject.get("fence").asJsonObject, Fence::class.java);
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "AC: Error getting fence");
        }
        return null
    }

    fun updateWorker(worker: Worker?) {
        worker?.run{
            subtitle = buildSubtitleLabel(lastRun)
            description =  description?: buildDescription(this)
            parseLocation(this)
            updateModelValidity(this)
        }
    }

    fun buildSubtitleLabel(rawString: String? ): String  {
        if(rawString == null || rawString.isEmpty()){
//            return "Last executed | Never";
            return "";
        }

        val year = Time.getYear(rawString, locale)

        if (year == "1900" || year == "1899") {
//            return "Last executed | Never";
            return ""
        } else {
            val hour = Time.getTime(rawString, locale, preferencesRepository.preferences!!.is24hours).toLowerCase();
            return context.getString(R.string.mobile_worker_executed,Time.getDay(rawString, locale),hour)
//            return "Last executed | " + Time.getDay(rawString, locale) + " | " + hour;
        }
    }

    private fun buildDescription(worker: Worker): String? =
            worker.triggers?.getOrNull(0)?.label


}
