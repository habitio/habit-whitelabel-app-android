package com.muzzley.util.ui;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

import timber.log.Timber;

/**
 * Created by caan on 23-11-2015.
 */
public class IconTextView extends TextView {

    static Typeface typeFace;

    public IconTextView(Context context) {
        super(context);
        init(context);
    }

    public IconTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public IconTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public IconTextView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr,defStyleRes);
        init(context);
    }
    void init(Context context) {
        try {
            if (typeFace == null) {
                typeFace = Typeface.createFromAsset(context.getAssets(), "icomoon.ttf");
            }
            setTypeface(typeFace);
        } catch (Exception e) {
            Timber.e(e,"Instant run error, probabily");
        }
    }

    @Override
    public void setText(CharSequence text, BufferType type) {

        try {
            if (text != null && text.length() >= 2) {
                char c = (char) Integer.parseInt(text.toString().substring(2), 16);
                String icon = c + "";
                super.setText(icon, type);
            } else {
                Timber.d("invalid icon for " + text);
                super.setText(text, type);
            }
        } catch (Exception e) {
            Timber.e("invalid icon for " + text);
            super.setText(text, type);
        }
    }
}
