package com.muzzley.util.ui

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet

import com.muzzley.app.cards.Container

class SectionView
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = android.R.attr.textViewStyle
    ) : AppCompatTextView(context, attrs, defStyleAttr), Container<SectionVM> {


    override fun setContainerData(data: SectionVM) {
        super.setText(data.label)
    }

}
