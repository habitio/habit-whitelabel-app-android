package com.muzzley.app.profiles

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.RelativeLayout
import com.jakewharton.rxbinding2.widget.RxCompoundButton
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.util.picasso.RoundedTransformationBuilderCustom
import com.muzzley.util.ui.loadUrlFitCenterCrop
import com.muzzley.util.ui.visible
import com.squareup.picasso.Picasso
import io.reactivex.Observer
import kotlinx.android.synthetic.main.channel_item.view.*


class ChannelItem 
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): RelativeLayout(context,attrs,defStyleAttr) , Container<ChannelVM>{

    override
    fun setContainerData(data: ChannelVM) {

        val transformation = RoundedTransformationBuilderCustom(context)
                .borderColor(ContextCompat.getColor(context,if (data.error != null) R.color.red else R.color.blackish))
                .borderWidthDp(1f)
                .cornerRadiusDp(30f)
                .oval(true)
//                .scaleType(ImageView.ScaleType.FIT_CENTER)
                .build()


//        Picasso.get()
//                .load(data.photoUrl)
//                .fit()
//                .centerCrop()
////                .centerInside()
//                .transform(transformation)
//                .into(imageView)

        imageView.loadUrlFitCenterCrop(data.photoUrl,transformation)

        textView.text = data.label
        error.visible(data.error!= null)
        checkbox.visible(data.selected != null)
        checkbox.isChecked = data.selected == true
        if (data.selectedRx != null) {
            RxCompoundButton.checkedChanges(checkbox)
                    .doOnNext { data.selected = it }
                    .subscribe(data.selectedRx as Observer<Boolean>)
        }

    }
}
