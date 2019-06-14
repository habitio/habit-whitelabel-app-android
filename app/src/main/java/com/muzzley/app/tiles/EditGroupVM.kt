package com.muzzley.app.tiles

import com.muzzley.model.tiles.Tile

class EditGroupVM(
        val tile: Tile? = null,
        var checked: Boolean = false,
        val label: String? = null,
        val click: (() -> Unit)? = null
)