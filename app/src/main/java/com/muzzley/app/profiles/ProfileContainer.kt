package com.muzzley.app.profiles

import android.content.Context
import android.util.AttributeSet
import com.muzzley.app.ProfilesActivity
import com.muzzley.app.cards.Container
import com.muzzley.model.profiles.Profile
import com.muzzley.util.ui.SquareCardView
import com.muzzley.util.ui.loadUrl
import kotlinx.android.synthetic.main.profile.view.*

class ProfileContainer : SquareCardView , Container<Profile> {


    constructor(context: Context) : super(context)

    constructor(context:Context , attrs: AttributeSet) : super(context, attrs)


    override
    fun setContainerData(profile: Profile) {

        label.text = profile.name;
        icon.loadUrl(profile.photoUrlSquared ?: profile.photoUrl)
        mOverlay.loadUrl(profile.overlay)
        setOnClickListener{
            (context as ProfilesActivity).profileClick(profile) //FIXME: should we use an interface ?
        }
    }
}