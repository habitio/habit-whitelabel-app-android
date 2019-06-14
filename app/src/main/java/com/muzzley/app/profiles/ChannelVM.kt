package com.muzzley.app.profiles

import com.muzzley.util.ui.ViewModel

import io.reactivex.subjects.PublishSubject

class ChannelVM(
        layout: Int,
        val photoUrl: String?,
        val label: String?,
        var error: Throwable? = null,
        var selected: Boolean? = null,
        var selectedRx: PublishSubject<Boolean>? = null) : ViewModel(layout) {



}
