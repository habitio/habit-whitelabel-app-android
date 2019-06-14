package com.muzzley.app.profiles

import android.content.Context
import android.util.AttributeSet
import com.muzzley.app.ProfilesActivity
import com.muzzley.app.cards.Container
import com.muzzley.model.profiles.Bundle
import com.muzzley.util.ui.SquareCardView
import com.muzzley.util.ui.loadUrl
import kotlinx.android.synthetic.main.bundle.view.*

class BundleContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): SquareCardView (context,attrs,defStyleAttr) ,Container<Bundle>{

    override
    fun setContainerData(bundle: Bundle) {

        label.text = bundle.name
        icon.loadUrl(bundle.photoUrlSquared)

        setOnClickListener {
            (context as ProfilesActivity).bundleClick(bundle) //FIXME: should we use an interface ?
        }

    }
}