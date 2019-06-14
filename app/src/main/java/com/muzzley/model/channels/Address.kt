package com.muzzley.model.channels;

data class Address (var channel: String, var component: String, var property: String) {

    fun toTopic() =
        "/v3/channels/$channel/components/$component/properties/$property/value"


    companion object {
        @JvmStatic
        fun fromTopic(topic: String ): Address  {
            val path = topic.split('/')
            return Address(
                    path[path.indexOf("channels")+1],
                    path[path.indexOf("components")+1],
                    path[path.indexOf("properties")+1]
            )
        }
    }


}