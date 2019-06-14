package com.muzzley.util.ui;

import android.view.MotionEvent;
import android.view.View;

/**
 * Created by ruigoncalo on 29/12/14.
 */
public class ViewTouchListener implements View.OnTouchListener {

    public ViewTouchListener() {
    }


    public boolean onTouch(View view, MotionEvent motionEvent) {

        switch (motionEvent.getAction()) {

            case MotionEvent.ACTION_DOWN:
                view.setAlpha(0.5f);
                break;

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                view.setAlpha(0);
                break;
        }
        return false;
    }
}
