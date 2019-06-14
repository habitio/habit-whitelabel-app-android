package com.muzzley.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CallLog
import android.telephony.TelephonyManager
import com.muzzley.App
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.query
import com.muzzley.util.retrofit.MuzzleyCoreService
import com.muzzley.util.rx.LogCompletableObserver
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by bruno.marques on 26/12/2015.
 */
class CallReceiver : BroadcastReceiver() {

    companion object {

        private val DATA_TYPE_RECEIVING_CALL = "incoming-call"
        private val DATA_TYPE_LOST_CALL = "missed-call"
        private val ONE_SECOND = 1000
    }

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var muzzleyCoreService: MuzzleyCoreService

    private lateinit var context: Context


    override fun onReceive(context: Context, intent: Intent) {
        Timber.d("CallReceiver onReceive")
        App.appComponent.inject(this)
        this.context = context

        if (preferencesRepository.userChannelId != null) {

            if (preferencesRepository.calls == null) {
                preferencesRepository.calls = mapOf()
            }

            val event: String?  = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
            val incomingNumber: String? = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
            Timber.e("CallListening : The received event : $event, incoming_number : $incomingNumber")

            if (event == TelephonyManager.EXTRA_STATE_RINGING && incomingNumber != null) {
                Timber.e("TelephonyManager.EXTRA_STATE_RINGING: $incomingNumber")
                val lastState = getCurrentPrefState(incomingNumber)
                //tem de ser null quando comeca
                if (lastState == null) {
                    onUpdateCallInfo(incomingNumber, DATA_TYPE_RECEIVING_CALL)
                    saveCurrentPref(TelephonyManager.EXTRA_STATE_RINGING, incomingNumber)
                }
            }

            if (event == TelephonyManager.EXTRA_STATE_OFFHOOK && incomingNumber != null) {
                Timber.e("TelephonyManager.EXTRA_STATE_OFFHOOK: $incomingNumber")
                saveCurrentPref(TelephonyManager.EXTRA_STATE_OFFHOOK, incomingNumber)
            }

            if (event == TelephonyManager.EXTRA_STATE_IDLE) {
                Timber.e("TelephonyManager.EXTRA_STATE_IDLE: $incomingNumber")
                if (incomingNumber != null) {
                    val lastState = getCurrentPrefState(incomingNumber)

                    if (lastState != null) {
                        if (lastState == TelephonyManager.EXTRA_STATE_RINGING || lastState == TelephonyManager.EXTRA_STATE_IDLE) {
                            onUpdateCallInfo(incomingNumber, DATA_TYPE_LOST_CALL)
                        } else {
                            if (getNumberOfCallsInPrefs() > 1) {
                                analDataFromLogs(getCurrentPrefStamp(incomingNumber)!!)
                            }
                        }
                    } else {
                        if (getNumberOfCallsInPrefs() > 1) {
                            analDataFromLogs(getCurrentPrefStamp(incomingNumber)!!)
                        }
                    }

                } else {
                    //se o numero for null vai ver se tem algum nas preferencias
                    if (getNumberOfCallsInPrefs() == 1) {
                        //se houve uma chamada que comecou, o numero é o que está nas preferencias
                        val lastState = getCurrentPrefState(getFirstNumber())
                        Timber.e("TelephonyManager.EXTRA_STATE_IDLE: getCurrentPrefState(getFirstNumber()) ${getFirstNumber()}")
                        Timber.e("lastState = getCurrentPrefState(getFirstNumber()) : $lastState")

                        if (lastState != null && lastState == TelephonyManager.EXTRA_STATE_RINGING) {
                            onUpdateCallInfo(getFirstNumber(), DATA_TYPE_LOST_CALL)
                        }
                    } else {
                        //se o numero chegou null, e houve mais que uma chamada ao mesmo tempo
                        //nao vai ser facil
                        val number = getFirstNumber()
                        if (number != null && !number.isEmpty()) {
                            if (getNumberOfCallsInPrefs() > 1) {
                                analDataFromLogs(getCurrentPrefStamp(number)!!)
                            }
                        }
                    }
                }

                deleteCurrentPref()
            }
        }
    }

    /**
     * Analise data from logs, check if there is missed calls in logs after the time the call started
     *
     * @param dateMili - data in milliseconds when the call started
     */
    fun analDataFromLogs(dateMili: Long) {
        val allCalls = Uri.parse("content://call_log/calls")

        context.contentResolver.query(allCalls)?.use { cursor ->

            val numberCol = cursor.getColumnIndex(CallLog.Calls.NUMBER)
            val nameCol = cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)
            val durationCol = cursor.getColumnIndex(CallLog.Calls.DURATION)
            val dateCol = cursor.getColumnIndex(CallLog.Calls.DATE)
            val typeCol = cursor.getColumnIndex(CallLog.Calls.TYPE)

            while (cursor.moveToNext()) {
                val type = cursor.getInt(typeCol) // for call type, Incoming or out going.
                val date = cursor.getString(dateCol) // for date
                val callTime = Date(date.toLong())

                when (type) {
//                    CallLog.Calls.OUTGOING_TYPE -> "OUTGOING"
//                    CallLog.Calls.INCOMING_TYPE -> "INCOMING"
                    CallLog.Calls.MISSED_TYPE -> {
                        //se a chamada do log for mais recente que a chamada que foi desligada
                        if (callTime.time > dateMili - ONE_SECOND) {
                            val number = cursor.getString(numberCol)// for  number
                            val name = cursor.getString(nameCol)// for name
                            val duration = cursor.getString(durationCol)// for duration

                            Timber.d("------------------------")
                            Timber.d("number : $number")
                            Timber.d("name : $name")
                            Timber.d("duration : $duration")
                            Timber.d("date : $date")
                            Timber.d("callTime : $callTime")
                            Timber.d("type : $type")
                            Timber.d("typeString : MISSED")

                            onUpdateCallInfo(number, DATA_TYPE_LOST_CALL)
                        }
                    }
                }
            }
        }
    }

    //it have the state and the date
    fun getNumberOfCallsInPrefs(): Int =
        preferencesRepository.calls?.size ?: 0

    //return the first value
    fun getFirstNumber(): String? =
        preferencesRepository.calls?.keys?.firstOrNull()


    fun saveCurrentPref(state: String, phoneNumber: String) {
        preferencesRepository.apply {
            calls = (calls ?: emptyMap()) + (phoneNumber to PhoneStateStamp(state,Date().time))
        }
    }

    fun deleteCurrentPref() {
        preferencesRepository.calls = null
    }

    fun getCurrentPrefState(phoneNumber: String?): String? =
        preferencesRepository.calls?.get(phoneNumber)?.state

    fun getCurrentPrefStamp(phoneNumber: String): Long? =
            preferencesRepository.calls?.get(phoneNumber)?.time

    fun onUpdateCallInfo(phoneNumber: String?, type: String) {

        val payload = mapOf("data" to mapOf("caller" to phoneNumber))
        muzzleyCoreService.sendProperty(preferencesRepository.userChannelId, "phone", type, payload)
                .subscribe(LogCompletableObserver("call info"))
    }

    data class PhoneStateStamp(val state: String, val time: Long)

}
