package com.muzzley.model.workers

data class Fence(var id: String? = null) {

    var latitude: Double? = null
    var longitude: Double? = null
    var radius: Double? = null
    var unit: String? = null

    //como so ha kilometros para ja, e so multiplicar por mil
//    @Transient
    fun getRadiusMeters(): Float = (radius!! * 1000).toFloat()
}
