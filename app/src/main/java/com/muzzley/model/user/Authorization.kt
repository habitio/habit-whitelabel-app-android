package com.muzzley.model.user

import com.google.gson.annotations.SerializedName

import java.util.Date

class Authorization {
    @SerializedName("access_token")
    var accessToken: String? = null
    @SerializedName("client_id")
    var clientId: String? = null
    var code: String? = null
    var expires: Date? = null
    @SerializedName("grant_type")
    var grantType: String? = null
    var id: String? = null
    @SerializedName("owner_id")
    var ownerId: String? = null
    @SerializedName("refresh_token")
    var refreshToken: String? = null
    var scope: List<String>? = null
    var endpoints: Endpoints? = null

    class Endpoints {
        var http: String? = null
        var mqtt: String? = null
    }
}
