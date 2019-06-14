package com.muzzley.util.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import com.muzzley.app.cards.Container
import com.muzzley.app.userprofile.UserVM
import com.muzzley.util.Utils
import timber.log.Timber


class CellContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = android.R.attr.textViewStyle
    ) : AppCompatTextView(context, attrs, defStyleAttr), Container<UserVM> {

    override
    fun setContainerData(data: UserVM) {
        super.setText(data.label)

//        setCompoundDrawablesWithIntrinsicBounds(
//            ContextCompat.getDrawable(context, data.icon)!!.apply {
////                Utils.px(context,24).let { setBounds(0, 0,it, it) }
//                24.let { setBounds(0, 0,it, it) }
//            }, null,null,null
//        )

        val vd = ContextCompat.getDrawable(context, data.icon)?.apply {

//            Timber.d("intrinsic: height: $intrinsicHeight, width: $intrinsicWidth")
        }!!
//        val drawable = ScaleDrawable(vd, Gravity.NO_GRAVITY,  1f, 1f).apply {
////            mutate()
//            setBounds(0, 0, 12, 12)
////            if (vd.intrinsicHeight > 48) {
//                setLevel((48f/vd.intrinsicHeight*10000).toInt())
////            }
////            setBounds(0, 0, 24, 24)
//        }
////                .drawable
//        setCompoundDrawablesWithIntrinsicBounds(drawable,null, null, null)

//        val bitmap = Bitmap.createBitmap(vd.getIntrinsicWidth(), vd.getIntrinsicHeight(), Bitmap.Config.ARGB_8888)
        val px = Utils.px(context,48)
        Timber.d("px: $px")
        val bitmap = Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        vd.setBounds(0, 0, canvas.getWidth(), canvas.getHeight())
//        vd.setBounds(0, 0, 96, 96)
        vd.draw(canvas)

        val bd = BitmapDrawable(bitmap)
        setCompoundDrawablesRelativeWithIntrinsicBounds(bd, null, null, null)


//        setCompoundDrawablesWithIntrinsicBounds(
//                VectorMasterDrawable(context, data.icon ).apply {
//                    setBounds(0,0,24,24)
//                },
//                null,null,null)


//        setCompoundDrawablesWithIntrinsicBounds(vd,null, null, null)

//        var ratio = vd.intrinsicHeight / 48f
//        textSize *= ratio
//        scaleX = 1/ratio
//        scaleY = 1/ratio


//        setCompoundDrawablesWithIntrinsicBounds(data.icon,0,0,0)
        setOnClickListener{
            data.click(data)
        }
    }
}
