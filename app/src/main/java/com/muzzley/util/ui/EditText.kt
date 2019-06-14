package com.muzzley.util.ui

import android.content.Context
import androidx.appcompat.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.MotionEvent
import com.jakewharton.rxbinding2.widget.RxTextView
import com.muzzley.R

class EditText
    @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = android.R.attr.textViewStyle
    ) : AppCompatEditText(context, attrs, defStyleAttr){
//http://stackoverflow.com/questions/3554377/handling-click-events-on-a-drawable-within-an-edittext


    override
    fun onFinishInflate() {
        super.onFinishInflate()
        setOnTouchListener{ _, event ->
//                val DRAWABLE_LEFT = 0;
//                val DRAWABLE_TOP = 1;
//                val DRAWABLE_RIGHT = 2;
//                val DRAWABLE_BOTTOM = 3;

                if(event.action == MotionEvent.ACTION_UP) {
//                    if(event.rawX >= (right - compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                    //left,top,right,bottom
                    val (_,_,cright,_) = compoundDrawables
                    if(event.rawX >= (right - cright.bounds.width())) {

                        text?.clear()
                        return@setOnTouchListener true
                    }
                }
                false
            }
        RxTextView.textChanges(this)
                .subscribe { s ->
                    val drawable = if (s.isNotEmpty()) R.drawable.icon_clean else R.drawable.icon_edit_group_gray
                    setCompoundDrawablesWithIntrinsicBounds(0,0,drawable,0)
                }
    }
}