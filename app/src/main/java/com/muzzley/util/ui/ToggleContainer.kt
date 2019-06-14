package com.muzzley.util.ui

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.muzzley.app.cards.Container
import com.muzzley.app.userprofile.UserVM
import com.muzzley.util.ui.triStateToggle.TriStateToggle
import kotlinx.android.synthetic.main.toggle_container.view.*

open class ToggleContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): LinearLayout(context,attrs,defStyleAttr) , Container<UserVM> {

//    @InjectView(R.id.toggle) lateinit var toggle: TriStateToggle
//    @InjectView(R.id.text) lateinit var textView: TextView

    override
    fun setContainerData(data: UserVM) {
        textView.text = data.label
        toggle.setOnClickListener(null)
        toggle.state = if (data.on == true) TriStateToggle.STATE_ON else TriStateToggle.STATE_OFF
        toggle.setOnStateChangeListener{ _: View , state : Int ->
            data.on = state == TriStateToggle.STATE_ON
            data.click(data)
        }
    }
}
