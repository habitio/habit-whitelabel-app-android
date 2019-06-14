package com.muzzley.app.userprofile

import android.content.Context
import androidx.appcompat.widget.AppCompatTextView
import android.util.AttributeSet
import com.muzzley.app.cards.Container


class LinkContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = android.R.attr.textViewStyle
    ) : AppCompatTextView(context, attrs, defStyleAttr), Container<UserVM> {

    override
    fun setContainerData(data: UserVM) {
        text = data.label
        setOnClickListener{
            data.click(data)
        }
    }
}
