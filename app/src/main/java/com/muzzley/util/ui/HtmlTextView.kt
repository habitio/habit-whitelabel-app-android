package com.muzzley.util.ui

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.muzzley.util.fromHtml


class HtmlTextView
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.textViewStyle
) : AppCompatTextView(context, attrs, defStyleAttr) {

    override fun setText(text: CharSequence?, type: BufferType?) {

        super.setText(text.toString().fromHtml(),type)
    }
}
