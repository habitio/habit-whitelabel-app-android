package com.muzzley.app.tiles

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.LinearLayout
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.util.picasso.CircleBorderTransform
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.loadUrl
import com.muzzley.util.ui.show
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_item_edit_group.view.*


/**
 * Created by caan on 28-12-2015.
 */

class EditGroupContainer 
    @JvmOverloads constructor(
            context: Context ,
            attrs: AttributeSet? = null ,
            defStyleAttr: Int = 0
    ): LinearLayout (context,attrs,defStyleAttr), Container<EditGroupVM>{

    override
    fun setContainerData(data: EditGroupVM) {

        textView.text = data.label
        if (data.tile != null) {
            val tile = data.tile
            imageView.show()
//            Picasso.get()
//                    .load(tile.photoUrlAlt ?: tile.photoUrl)
//                    .transform(CircleBorderTransform(context))
//                    .into(imageView)
//
            imageView.loadUrl(tile.photoUrlAlt ?: tile.photoUrl, CircleBorderTransform(context))

            textView.isChecked = data.checked
            setOnClickListener {
                data.checked = !data.checked
                textView.isChecked = data.checked
            }
            textView.text = tile.label
        } else {
            imageView.hide()
            textView.isChecked = false
            setOnClickListener{
                data.click?.invoke()
            }
            textView.text = data.label
        }
        textView.setTextColor(ContextCompat.getColor(context,if (data.click != null) R.color.blue else R.color.black))

    }


}
