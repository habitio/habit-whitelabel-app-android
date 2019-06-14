package com.muzzley.model.grants

import com.muzzley.services.PreferencesRepository

data class GrantMessage (var topic: String, var payload: Map<*,*>) {


    companion object {

        @JvmStatic
        fun request(pr: PreferencesRepository , role: String, deviceId: String, manufacturerParameters: Map<*,*> = mutableMapOf<Any,Any>()) : GrantMessage {

            return GrantMessage(
                    topic = "/v3/devices/$deviceId/grants",
                    payload= mutableMapOf(
                        "io" to "w",
                        "data" to mutableMapOf("role" to role )
                            + if (role == "application") {
                                mutableMapOf("client_id" to pr.appClientId)
                            } else {
                                mutableMapOf("client_id" to pr.user!!.id, "requesting_client_id" to  pr.appClientId)
                            }
                            + manufacturerParameters
//                                content: manufacturerName, //foscam does not have this field
//                                id: manufacturerId  //foscam does not have this field
            )


            )

        }
    }
}