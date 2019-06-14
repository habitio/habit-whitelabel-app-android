package com.muzzley.util.ui;

import android.content.Context;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.muzzley.R;

import timber.log.Timber;

public class PasswordEditText extends androidx.appcompat.widget.AppCompatEditText {

    private boolean visiblePassword;

    public PasswordEditText(Context context) {
        super(context);
        onFinishInflate();
    }

    public PasswordEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PasswordEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        Timber.d("PPP got called");
        super.onFinishInflate();
        setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_icon_view_password, 0);
        setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {

                        setVisiblePassword(!isVisiblePassword());
                        setCompoundDrawablesWithIntrinsicBounds(0, 0, isVisiblePassword() ? R.drawable.ic_icon_view_password_off : R.drawable.ic_icon_view_password, 0);
//                        (PasswordEditText.this.getText()).clear()
                        setInputType(InputType.TYPE_CLASS_TEXT | (isVisiblePassword() ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_TEXT_VARIATION_PASSWORD));
                        return true;
                    }

                }

                return false;
            }

        });
    }

    public boolean isVisiblePassword() {
        return visiblePassword;
    }

//    public boolean isVisiblePassword() {
//        return visiblePassword;
//    }

    public void setVisiblePassword(boolean visiblePassword) {
        this.visiblePassword = visiblePassword;
    }

}
