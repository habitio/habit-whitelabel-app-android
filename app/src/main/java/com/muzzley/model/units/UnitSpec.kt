package com.muzzley.model.units

import com.google.gson.JsonElement


/**
 * Created by caan on 22-06-2016.
 */
class UnitSpec {

    var name: Name? = null
    var prefix: String? = null
    var suffix: String? = null
    var context: JsonElement? = null
    var decimalPlaces: Int = 0
}
