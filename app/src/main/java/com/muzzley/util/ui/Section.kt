package com.muzzley.util.ui

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import com.muzzley.app.cards.Container
import com.muzzley.app.userprofile.UserVM

class Section
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = android.R.attr.textViewStyle
    ) : AppCompatTextView(context, attrs, defStyleAttr), Container<UserVM> {

    override fun setContainerData(data: UserVM) {
        super.setText(data.label)
    }

}
