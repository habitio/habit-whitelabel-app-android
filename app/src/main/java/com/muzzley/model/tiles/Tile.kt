package com.muzzley.model.tiles


import com.muzzley.model.channels.Interface

import java.util.ArrayList

class Tile (

    val id: String,
    var label: String,
    val groups: List<String> = ArrayList(),
    val profile: String?,
    val channel: String,
    @Deprecated("used by legacy webviews")
    val remoteId: String?,
    val photoUrl: String?,
    val photoUrlAlt: String?,
    val overlayUrl: String?,

    val components: List<Component> = listOf(),
    val information: List<Information> = listOf(),
    val actions: List<Action> = listOf(),
    val isGroupable: Boolean = false
){
    @Transient
    var _interface: Interface? = null

}
