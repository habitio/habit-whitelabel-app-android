package com.muzzley.app.profiles

import com.muzzley.model.profiles.Profile
import com.muzzley.model.channels.Channel
import com.muzzley.util.ui.ViewModel
import io.reactivex.subjects.PublishSubject

class BundleVM(
        layout: Int,
        val profile: Profile,
        var state: AddState,
        val idx: Int = 0,
        var click: android.view.View.OnClickListener? = null,// FIXME: should be () -> Unit
        var channelData: List<ChannelVM>? = null,
        var fibaro: List<Map<String, Any>>? = null,
        var channels: List<Channel>? = null,
        var error: Throwable? = null,
        var totalSteps: Int? = null,
        var stepNo: Int? = null,
        var stepTitle: String? = null
    ) : ViewModel(layout) {

    val validRx: PublishSubject<Boolean> = PublishSubject.create()
}
