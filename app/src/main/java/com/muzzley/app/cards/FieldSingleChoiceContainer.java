package com.muzzley.app.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;
import com.muzzley.util.Utils;

import timber.log.Timber;

/**
 * Created by caan on 14-03-2016.
 */
public class FieldSingleChoiceContainer extends RadioGroup implements Container<Field> {

//    private ViewGroup.LayoutParams layoutParams;

    public FieldSingleChoiceContainer(Context context) {
        super(context);
    }

    public FieldSingleChoiceContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setContainerData(final Field data) {

//        if (layoutParams == null) {
//            View childAt = getChildAt(0);
//            layoutParams = childAt.getLayoutParams();
//            removeAllViews();
//        }

        int id = 0;
        for (Placeholder ph : data.placeholder) {
            LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_field_choice_item, this);
            TextView rb = (TextView) getChildAt(id);

//            TextView rb = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_field_choice_item, null);
            rb.setId(id++);
            rb.setText(ph.label);

//            LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1f);
//            int px = Utils.px(getContext(), 10);
//            params.setMargins(px,px,px,px);

//            rb.setLayoutParams(layoutParams);
//            addView(rb);

            if (ph.selected != null && ph.selected) {
                ((Checkable) rb).setChecked(ph.selected);
            }
        }

        setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < data.placeholder.size(); i++) {
                    data.placeholder.get(i).selected = (i == checkedId);
                }
            }
        });
    }
}
