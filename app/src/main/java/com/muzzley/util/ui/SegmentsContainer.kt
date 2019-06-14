package com.muzzley.util.ui

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import com.muzzley.R
import com.muzzley.app.cards.Container
import info.hoang8f.android.segmented.SegmentedGroup
import io.reactivex.Observable
import timber.log.Timber

class SegmentsContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int? = 0
    ) : LinearLayout(context, attrs), Container<List<String>> {

    var data: List<String>? = null
    var group: SegmentedGroup? = null

    override fun onFinishInflate() {
        super.onFinishInflate()
        Timber.d("SegmentsContainer")
    }


    override fun setContainerData(data: List<String>?) {
        if (data != null) {
            removeAllViews()
        }
        this.data = data
        group = inflate(R.layout.segmented_group) as SegmentedGroup
        group?.let { group ->
            data?.forEachIndexed { index, s ->
                val segment = group.inflate(R.layout.segmented_item) as RadioButton
                segment.text = s
                segment.id = index
                if (index == 0) {
                    segment.isChecked = true
                }
                group.addView(segment)
            }
            group.updateBackground()
            addView(group)
            invalidate()
        }
        show()
    }

    fun setSelected(pos: Int) {
        val segment = group?.getChildAt(pos) as RadioButton
        segment.isChecked = true
    }

    fun setSelected(s: String) {
        val pos = data?.indexOf(s) ?: 0
        val segment = group?.getChildAt(pos) as RadioButton
        segment.isChecked = true
    }

    fun listenSelected() =
        Observable.create<Int> { emitter ->
            group?.setOnCheckedChangeListener { _: RadioGroup, i: Int ->
                emitter.onNext(i)
            }
        }

}