package com.muzzley.util.ui;

import android.content.Context;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.util.AttributeSet;

import com.muzzley.R;

/**
 * Created by caan on 08-02-2016.
 */
public class MuzzleySwipeRefreshLayout extends SwipeRefreshLayout {
    public MuzzleySwipeRefreshLayout(Context context) {
        super(context);
        init();
    }

    public MuzzleySwipeRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public void init() {
        setColorSchemeResources(
                R.color.colorPrimary,
                R.color.gray_lighter,
                R.color.colorPrimary,
                R.color.gray_lighter);

    }

//    @Override
//    public boolean canChildScrollUp() {
//        return true;
////        return super.canChildScrollUp();
//    }
}
