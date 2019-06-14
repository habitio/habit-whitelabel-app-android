package com.muzzley.model.workers

import com.google.gson.JsonElement

/**
 * Created by ruigoncalo on 05/12/14.
 */
class WorkerUnit {

    var photoUrl: String? = null
    var photoUrlAlt: String? = null
    var profile: String? = null
    var channel: String? = null
    var component: String? = null
    var choices: JsonElement? = null
    var labels: Labels? = null
    var label: String? = null
    var hasLocation: Boolean? = null
    var property: String? = null
    var isInvalid: Boolean? = null

    class Labels {
        var component: String? = null
        var profile: String? = null
        var channel: String? = null

//        val isValid: Boolean
//            get() = component != null && channel != null && profile != null
    }
}
