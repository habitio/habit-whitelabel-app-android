package com.muzzley.util.ui

import android.content.Context
import androidx.cardview.widget.CardView
import android.util.AttributeSet

/**
 * Created by kyryloryabin on 03/03/16.
 */
open class SquareCardView
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ) : CardView(context, attrs,defStyleAttr) {

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec)
    }
}
