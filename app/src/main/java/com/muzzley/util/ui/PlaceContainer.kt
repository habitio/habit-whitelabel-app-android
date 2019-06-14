package com.muzzley.util.ui

import android.content.Context
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.RelativeLayout
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.app.userprofile.UserVM
import com.muzzley.model.user.Place
import kotlinx.android.synthetic.main.place_container.view.*

class PlaceContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): RelativeLayout(context,attrs,defStyleAttr) , Container<UserVM>{

    companion object {
        val drawables = mapOf(
                "home" to R.drawable.icon_home,
                "work" to R.drawable.icon_work,
                "gym" to R.drawable.icon_gym,
                "school" to R.drawable.icon_school
        )
    }

    private val defaultColor by lazy {wifi.textColors.defaultColor}
    private val linkColor by lazy { ContextCompat.getColor(context, R.color.blue) }

    override
    fun onFinishInflate() {
        super.onFinishInflate()
        wifi.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_wifi,0,0,0)
    }

    override
    fun setContainerData(data: UserVM) {

        val place: Place = data.place!!

        title.text = place.name
        val hasAddress = !place.address.isNullOrBlank()
        add.visible(!hasAddress)
        address.visible(hasAddress)
        wifi.visible(hasAddress)

        if (hasAddress) {
            address.text = place.address
            val ssid = place.wifi?.getOrNull(0)?.ssid
            wifi.text = ssid ?: context.getString(R.string.mobile_settings_locations_empty_wifi)
            wifi.setTextColor(if (!ssid.isNullOrBlank())  defaultColor else linkColor)
        }

        drawables[place.id]?.let {
            icon.setImageResource(it)
            icon.show()
        } ?: icon.invisible()



        setOnClickListener{
            data.click(data)
        }
    }
}
