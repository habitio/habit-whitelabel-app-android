package com.muzzley.app.cards;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.widget.EditText;

import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;

import java.util.ArrayList;

/**
 * Created by caan on 28-09-2015.
 */
public class FieldTextContainer extends EditText implements Container<Field> {
    private Field field;

    public FieldTextContainer(Context context) {
        super(context);
    }

    public FieldTextContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldTextContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void setContainerData(final Field field) {
        this.field = field;
//        setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean hasFocus) {
//                if (!hasFocus) {
//                    field.placeholder.get(0).text = getText().toString();
//                    Toast.makeText(getContext(), "entered: "+getText().toString(), Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
        if (field.placeholder == null ) {
            field.placeholder = new ArrayList<>();
        }
        if (field.placeholder.size() == 0) {
            field.placeholder.add(new Placeholder());
        }
        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                field.placeholder.get(0).text = editable.toString();
            }
        });
    }
}
