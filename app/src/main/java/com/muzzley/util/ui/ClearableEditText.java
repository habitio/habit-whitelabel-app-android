package com.muzzley.util.ui;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.muzzley.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by kyryloryabin on 29/12/15.
 */
public class ClearableEditText extends LinearLayout {

    @BindView(R.id.text)
    EditText text;

    @BindView(R.id.btn_clear)
    ImageButton btnClear;

    public ClearableEditText(Context context) {
        this(context, null);
    }

    public ClearableEditText(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ClearableEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        inflate(context, R.layout.clearable_edittext, this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        ButterKnife.bind(this, this);

        text.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    btnClear.setVisibility(VISIBLE);
                } else {
                    btnClear.setVisibility(INVISIBLE);
                }
            }
        });

        btnClear.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText("");
            }
        });
    }

    public EditText getEditText() {
        return text;
    }

    public Editable getText() {
        return text.getText();
    }

    public void setHint(String hint) {
        text.setHint(hint);
    }

    public void setText(String text) {
        this.text.setText(text);
    }


}
