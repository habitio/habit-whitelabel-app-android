package com.muzzley.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 02/03/15.
 */
public class ScreenInspector {

    private static final int STATUS_BAR_HEIGHT_DP = 24;
    private static final int APP_BAR_HEIGHT_DP = 56;

    public static void getScreenInfo(Context context){

        Configuration configuration = context.getResources().getConfiguration();
        int screenLayout = configuration.screenLayout;
        if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            Timber.d("Screen size: Large screen");
        }
        else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_NORMAL) {
            Timber.d("Screen size: Normal screen");
        }
        else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_SMALL) {
            Timber.d("Screen size: Small screen");
        }
        else if ((screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            Timber.d("Screen size: Extra Large screen");
        }
        else {
            Timber.d("Screen size: Undefined screen");
        }

        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        double density = metrics.density;
        if (density >= 4.0) {
            Timber.d("Density: xxxhdpi");
        } else if (density >= 3.0) {
            Timber.d("Density: xxhdpi");
        } else if (density >= 2.0) {
            Timber.d("Density: xhdpi");
        } else if (density >= 1.5) {
            Timber.d("Density: hdpi");
        } else if (density >= 1.0) {
            Timber.d("Density: mdpi");
        } else {
            Timber.d("Density: ldpi");
        }

        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Timber.d("Screen: " + width + "x" + height + " px |  density: " + density);
        Timber.d("Screen: " + (metrics.widthPixels/density) + "x" + (metrics.heightPixels/density) + " dp");
        Timber.d("Screen configuration: " + configuration.screenWidthDp + "x" + configuration.screenHeightDp);
        Timber.d("Smallest screen width dp = " + configuration.smallestScreenWidthDp);
    }

    public static int getScreenWidth(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    public static int getScreenHeight(Context context){
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    public static int getStatusBarHeightPx(Context context){
        return (int) Utils.dpToPx(context, STATUS_BAR_HEIGHT_DP);
    }

    public static int getAppBarHeightPx(Context context){
        return (int) Utils.dpToPx(context, APP_BAR_HEIGHT_DP);
    }

    /**
     * Get Navigation Bar (bottom) height in px
     *
     * @param context context to get resources
     * @return height if resourceId is found, 0 otherview
     */
    public static int getNavigationBarHeightPx(Context context){
        int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        return (resourceId > 0) ? context.getResources().getDimensionPixelSize(resourceId) : 0;
    }

}
