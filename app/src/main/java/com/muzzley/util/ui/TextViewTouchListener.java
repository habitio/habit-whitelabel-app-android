package com.muzzley.util.ui;

import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

/**
 * Created by ruigoncalo on 26/11/14.
 */
public class TextViewTouchListener implements View.OnTouchListener {

    private int colorOnActionDown;
    private int colorOnActionUp;

    public TextViewTouchListener(int colorOnActionDown, int colorOnActionUp) {
        this.colorOnActionDown = colorOnActionDown;
        this.colorOnActionUp = colorOnActionUp;
    }


    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                ((TextView) view).setTextColor(colorOnActionDown);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                ((TextView) view).setTextColor(colorOnActionUp);
                break;
        }
        return false;
    }
}
