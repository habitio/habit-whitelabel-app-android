package com.muzzley.util;

import android.Manifest

/**
 * Created by bruno.marques on 10/03/2016.
 */
class CapabilityPermissionMap {
    
    companion object {
        @JvmStatic
        val map = mapOf(
//                "trigger-call-incoming" to listOf(Manifest.permission.READ_PHONE_STATE),
//                "trigger-call-incoming-no-number" to listOf(Manifest.permission.READ_PHONE_STATE),
//                "trigger-call-missed" to listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),
//                "trigger-call-missed-no-number" to listOf(Manifest.permission.READ_PHONE_STATE),
                "trigger-call-incoming" to listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),
                "trigger-call-incoming-no-number" to listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),
                "trigger-call-missed" to listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),
                "trigger-call-missed-no-number" to listOf(Manifest.permission.READ_PHONE_STATE, Manifest.permission.READ_CONTACTS, Manifest.permission.READ_CALL_LOG),

                "trigger-sms-incoming" to listOf(Manifest.permission.RECEIVE_SMS),
                "trigger-sms-incoming-no-number" to listOf(Manifest.permission.RECEIVE_SMS),
                "location" to listOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION)
        )

        @JvmStatic
        fun getListPermissionsFromCapability(cap: String ) = map.get(cap)
    }


}
