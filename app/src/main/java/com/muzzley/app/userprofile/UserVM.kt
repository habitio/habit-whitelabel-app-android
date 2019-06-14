package com.muzzley.app.userprofile

import com.muzzley.model.user.Place
import com.muzzley.util.ui.ViewModel

class UserVM(
        layout: Int,
        val label: String? = null,
        val icon: Int = 0,
        val click: (UserVM) -> Unit = {},

        var metric: Boolean?=null,
        var on: Boolean?=null,
        var place: Place?=null

        ): ViewModel(layout)