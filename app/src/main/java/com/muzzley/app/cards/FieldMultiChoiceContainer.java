package com.muzzley.app.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;

/**
 * Created by caan on 16-03-2016.
 */
public class FieldMultiChoiceContainer extends LinearLayout implements Container<Field>{
    public FieldMultiChoiceContainer(Context context) {
        super(context);
    }

    public FieldMultiChoiceContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldMultiChoiceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FieldMultiChoiceContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    @Override
    public void setContainerData(final Field data) {
        int id = 0;
        for (Placeholder ph : data.placeholder) {
            // radiobutton works also for multi besides single choice, but we might want to change this in the future
            LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_field_choice_item, this);
            TextView tv = (TextView) getChildAt(id);

            tv.setId(id++);
            tv.setText(ph.label);
            ((Checkable)tv).setChecked(ph.selected);

            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Placeholder ph = data.placeholder.get(v.getId());
                    ((Checkable)v).setChecked(ph.selected = !ph.selected);
                }
            });
        }

    }
}
