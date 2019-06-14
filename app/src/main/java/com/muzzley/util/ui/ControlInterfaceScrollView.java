package com.muzzley.util.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.HorizontalScrollView;

/**
 * Created by ruigoncalo on 19/11/14.
 */
public class ControlInterfaceScrollView extends HorizontalScrollView {

    public interface ScrollListener {
        void onScroll(int l, int t, int oldl, int oldt);
    }

    private ScrollListener listener;

    public void setOnScrollChangedListener(ScrollListener listener){
        this.listener = listener;
    }

    public ControlInterfaceScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ControlInterfaceScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ControlInterfaceScrollView(Context context) {
        super(context);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        if(listener != null) {
            listener.onScroll(l, t, oldl, oldt);
        }
        super.onScrollChanged(l, t, oldl, oldt);
    }

}
