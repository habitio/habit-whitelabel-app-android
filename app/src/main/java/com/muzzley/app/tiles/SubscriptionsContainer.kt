package com.muzzley.app.tiles

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.AttributeSet
import com.muzzley.app.cards.Container
import com.muzzley.model.tiles.ServiceSubscriptions
import com.muzzley.util.ui.SquareCardView
import com.muzzley.util.ui.loadUrl
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.subscription.view.*

class SubscriptionsContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): SquareCardView(context,attrs,defStyleAttr), Container<ServiceSubscriptions.Subscription> {

    override fun setContainerData(subscription: ServiceSubscriptions.Subscription) {
        label.text = subscription.name
        icon.loadUrl(subscription.squaredImageUrl)
        state.setImageLevel(if (subscription.state == true) 1 else 0)

        if (!TextUtils.isEmpty(subscription.infoUrl)) {
            setOnClickListener { context.startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(subscription.infoUrl))) }
        }


    }
}
