package com.muzzley.util.ui

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import android.util.AttributeSet
import com.muzzley.R
import com.muzzley.app.userprofile.UserVM
import kotlinx.android.synthetic.main.toggle_container.view.*

class PushContainer
@JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
): ToggleContainer (context,attrs,defStyleAttr) {

    override
    fun setContainerData(data: UserVM) {
        super.setContainerData(data)


        if (NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            setOnClickListener(null)
            textView.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0)
            toggle.isEnabled = true
        } else {
            setOnClickListener { Dialogs.unblockNotifications(context) }
            val drawable: Drawable = ContextCompat.getDrawable(context, R.drawable.icon_alert)!!.mutate()
            DrawableCompat.setTint(drawable, ContextCompat.getColor(context, R.color.red))
            textView.setCompoundDrawablesWithIntrinsicBounds(null,null,drawable,null)
//            textView.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.icon_alert_red,0)
            toggle.isEnabled = false
        }

    }
}
