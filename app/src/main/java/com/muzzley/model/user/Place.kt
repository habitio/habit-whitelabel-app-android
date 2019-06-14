package com.muzzley.model.user

import java.io.Serializable

data class Place
//{
(
        var id: String? = null,
        var name: String? = null,
        var address: String? = null,
        var latitude: Double = 0.0,
        var longitude: Double = 0.0,
        var wifi: List<Wifi>? = null
): Serializable, Cloneable {

    @Throws(CloneNotSupportedException::class)
    public override fun clone(): Place {//todo
        return copy()
    }

}
//
//    public override fun clone(): Place {//todo
//        return super.clone()
//    }
//}
