package com.muzzley.services

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.muzzley.R
import com.muzzley.app.receivers.CallReceiver
import com.muzzley.model.workers.Fence
import com.muzzley.model.Preferences
import com.muzzley.model.User
import com.muzzley.model.productDetails.Location
import com.muzzley.model.stores.InterfacesStore
import com.muzzley.model.user.Authorization
import com.muzzley.util.parseJson
import com.muzzley.util.toJsonString
import java.util.*
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

class PreferencesRepositoryImpl(context: Context,sharedPreferences: SharedPreferences) : PreferencesRepository {

    override var user: User? by JsonProp(sharedPreferences,key="key-profile")
    override var authorization: Authorization? by JsonProp(sharedPreferences)
    override var preferences: Preferences? by JsonProp(sharedPreferences)

    override var push: Boolean? by sharedPreferences.bool(true) // FIXME: make sure we want true default values here ...
    override var userChannelId: String? by sharedPreferences.string()
    override var customerUserId: String? by sharedPreferences.string(key ="key-customer-user-id")
    override var fences: List<Fence>? by JsonProp(sharedPreferences)
    override var lastKnownLocation: Location? by JsonProp(sharedPreferences)
    //FIXME: this don't really need to be persisted do non-volatile
    override var muzzCapabilities: Set<String>? by JsonProp(sharedPreferences, key = "key-muzz-capabilities")
    override var muzzDevicePermissions: Set<String>? by JsonProp(sharedPreferences, key = "key-device-permissions")

    override var expirationdate: Date? by JsonProp(sharedPreferences, defaultValue = "1970-01-01T00:00:00.000+0000") // FIXME: check this
    override var calls: Map<String, CallReceiver.PhoneStateStamp>? by JsonProp(sharedPreferences)
    override var interfacesStore: InterfacesStore? by JsonProp(sharedPreferences)

    override var tags: String? by sharedPreferences.string()
    override var firebaseToken: String? by sharedPreferences.string()
    override var azureId: String? by sharedPreferences.string()

    override val applicationId = context.getString(R.string.app_namespace)
    override val appClientId = context.getString(R.string.app_client_id)
    override var azureEndpoint: String? by sharedPreferences.string()

    //we should be able to unify both aproaches to shared prefs delegated properties ...
    class JsonProp(val sharedPreferences: SharedPreferences, val defaultValue: String? = null, val key: String? = null) {
        var cached: Any? = null
        inline operator fun <reified T> getValue(thisRef: Any, property: KProperty<*>): T? {
            if (cached == null) {
                cached = sharedPreferences.getString(key ?: property.name, defaultValue).parseJson<T>()
            }
            return cached as T?
        }


        @SuppressLint("ApplySharedPref")
        inline operator fun setValue(thisRef: Any, property: KProperty<*>, value: Any?) {
            if (value == null) {
                sharedPreferences.edit().remove(key ?: property.name).commit()
            } else {
                sharedPreferences.edit().putString(key ?: property.name, value.toJsonString()).apply()
            }
            cached = value
        }
    }


// runtime error : This function has a reified type parameter and thus can only be inlined at compilation time, not called directly
//    inline fun <reified T >SharedPreferences.json(def: T? = null, key: String? = null) =
//            delegate(def, key, { k:String, b:T? -> this.getString(k,null).parseJson<T>()}, { s:String,b:T? -> this.putString(s,b.toJsonString() )}  )
//
    fun SharedPreferences.bool(def: Boolean? = null, key: String? = null) =
            delegate(def, key, { s, b -> this.getBoolean(s,b==true)}, { s,b -> this.putBoolean(s,b == true)}  )

    fun SharedPreferences.string(def: String? = null, key: String? = null) =
            delegate(def, key, SharedPreferences::getString, SharedPreferences.Editor::putString)

    inline fun <T> SharedPreferences.delegate(
            defaultValue: T?,
            key: String?,
            crossinline getter: SharedPreferences.(String, T?) -> T?,
            crossinline setter: SharedPreferences.Editor.(String, T?) -> SharedPreferences.Editor
    ): ReadWriteProperty<Any, T?> {
        return object : ReadWriteProperty<Any, T?> {
            var cached: T? = null
            override fun getValue(thisRef: Any, property: KProperty<*>): T? {
                if (cached == null) {
                    cached = getter(key ?: property.name, defaultValue)
                }
                return cached
            }


            @SuppressLint("ApplySharedPref")
            override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
                if (value == null)
                    edit().remove(key ?: property.name).commit()
                else
                    edit().setter(key ?: property.name, value).apply()
                cached = value
            }

        }
    }

}
