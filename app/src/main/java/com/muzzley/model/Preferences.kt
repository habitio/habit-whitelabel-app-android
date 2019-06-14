package com.muzzley.model

/**
 * Created by caan on 16-06-2016.
 */
//@ToString
//@EqualsAndHashCode
//@CompileStatic
data class Preferences(
        var units: String? = null,
        var hour_format: String? = null,
        var timezone: String? = null,
        var currency: String? = null,
        var language: String? = null,
        var locale: String? = null,
        var notifications: String? = null

) {
    fun isMetric(): Boolean = "imperial" != units

    var is24hours: Boolean
        get() = hour_format?.contains("24") ?: true
        set(b) {
            hour_format = if (b != false) "24h" else "12h"
        }


    fun setMetric(metric: Boolean) {
        units = if (metric) "metric" else "imperial"
    }
}
