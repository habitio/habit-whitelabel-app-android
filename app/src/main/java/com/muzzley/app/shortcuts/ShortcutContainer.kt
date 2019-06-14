package com.muzzley.app.shortcuts

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.ContextCompat
import android.util.AttributeSet
import android.widget.FrameLayout
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.model.shortcuts.Shortcut
import com.muzzley.util.ui.visible
import kotlinx.android.synthetic.main.shortcut_horizontal.view.*


class ShortcutContainer 
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): FrameLayout(context,attrs,defStyleAttr) , Container<ShortcutVM> {

    var isEditMode: Boolean = false


    override
    fun setContainerData(shortcutVm: ShortcutVM) {

        btn_shortcut.setOnClickListener{ shortcutVm.execute?.onNext(shortcutVm)}

        shortcutVm.editMode?.subscribe{
            isEditMode = it
            btn_remove.visible(it)
            btn_edit.visible(it)
            btn_drag.visible(it)
        }


        shortcutVm.editMode?.let{  subject ->
            RxView.longClicks(this).subscribe{subject.onNext(!isEditMode)}
        }


        if (shortcutVm.shortcut != null) {
            shortcut_title.text = shortcutVm.shortcut.label
            btn_shortcut.setImageResource(R.drawable.ic_arrow)
            btn_shortcut.setBackgroundResource(R.drawable.circle_shape)
            shortcutVm.shortcut.color?.let {
                btn_shortcut.backgroundTintList = ColorStateList.valueOf(Color.parseColor(it))
            }
//            if (!shortcut.isShowInWatch()) {
//                iconShowOnWatch.setVisibility(View.GONE)
//            }

            btn_remove?.setOnClickListener { shortcutVm.delete?.onNext(shortcutVm) }
            btn_edit?.setOnClickListener { shortcutVm.edit?.onNext(shortcutVm) }
//            btnDrag?.setOnLongClickListener(View.OnLongClickListener() {
//                override
//                Boolean onLongClick(view: View) {
//                    shortcutVm?.drag?.onNext(shortcutVm)
//                    return false
//                }
//            })
        } else {
            shortcut_title.text = context.getString(R.string.mobile_shortcut_new)
            btn_shortcut.setImageResource(R.drawable.ic_new)
            btn_shortcut.setBackgroundResource(R.drawable.background_shortcut_add)
//            btnShortcut.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            btn_shortcut.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
//            iconShowOnWatch.setVisibility(View.GONE)
        }
        progress.visible(shortcutVm.state == ShortcutVM.State.running)

        if (shortcutVm.state == ShortcutVM.State.error) {
            btn_shortcut.setImageResource(R.drawable.ic_action_clear)
            btn_shortcut.postDelayed( {
                btn_shortcut.setImageResource(R.drawable.ic_arrow)
                shortcutVm.state = ShortcutVM.State.shortcut
            },2000)
        }

    }

    interface ShortcutClickListener {
        fun onShortcutClick(shortcut: Shortcut)
    }
}
