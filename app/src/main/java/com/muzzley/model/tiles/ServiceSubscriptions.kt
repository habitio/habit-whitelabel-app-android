package com.muzzley.model.tiles

class ServiceSubscriptions {

    var serviceSubscriptions: List<Subscription>? = null

    class Subscription {

        var id: String? = null
        var state: Boolean? = null
        var name: String? = null
        var squaredImageUrl: String? = null
        var infoUrl: String? = null
    }
}
