package com.muzzley.util.ui

import android.content.Context
import androidx.annotation.StringRes
import android.text.Html
import android.text.Spanned
import android.util.AttributeSet
import android.widget.RadioGroup
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.app.userprofile.UserVM
import com.muzzley.util.fromHtml
import kotlinx.android.synthetic.main.radio_settings.view.*


class RadioContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int? = 0
    ) : RadioGroup(context, attrs), Container<UserVM> {

    override
    fun setContainerData(data: UserVM) {
        check(if (data.metric == true) R.id.metric else R.id.imperial)

        metric.text = spanned(R.string.mobile_metric_title, R.string.mobile_metric_text)
        imperial.text = spanned(R.string.mobile_imperial_title, R.string.mobile_imperial_text)

        setOnCheckedChangeListener { _: RadioGroup , checkedId : Int ->
            data.metric = checkedId == R.id.metric
            data.click(data)
        }
    }

    fun spanned(@StringRes title: Int , @StringRes text: Int): Spanned  =
        String.format("""%s<br/><font color="#99a7aa">%s</font>""",
                resources.getString(title), resources.getString(text)).fromHtml()
}
