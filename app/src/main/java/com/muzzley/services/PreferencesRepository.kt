package com.muzzley.services

import com.muzzley.app.receivers.CallReceiver
import com.muzzley.model.workers.Fence
import com.muzzley.model.Preferences
import com.muzzley.model.User
import com.muzzley.model.productDetails.Location
import com.muzzley.model.stores.InterfacesStore
import com.muzzley.model.user.Authorization
import java.util.*

interface PreferencesRepository {
    var authorization: Authorization?
    var user: User?
    var preferences: Preferences?
    var push: Boolean?
    var userChannelId: String?
    val applicationId: String
    val appClientId: String
    var tags: String?
    var firebaseToken: String?
    var azureId: String?
    var customerUserId: String?
    var fences: List<Fence>?
    var lastKnownLocation: Location?
    var muzzCapabilities: Set<String>?
    var muzzDevicePermissions: Set<String>?
    var expirationdate: Date?
    var calls: Map<String,CallReceiver.PhoneStateStamp>?
    var interfacesStore: InterfacesStore?
    var azureEndpoint: String?

}
