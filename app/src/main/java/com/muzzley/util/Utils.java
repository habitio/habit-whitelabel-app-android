package com.muzzley.util;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by ruigoncalo on 27/08/15.
 */
public class Utils {

    public static float pxToDp(Context context, int  px){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(px / (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float dpToPx(Context context, int dp){
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static int px(Context context,int dp){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp*scale + 0.5f);
    }

    public static Drawable getDrawable(Context context, int resId){
        return ContextCompat.getDrawable(context, resId);
    }

    public static float getValueWithThreshold(float value, float threshold) {
        if (value < threshold) {
            return threshold;
        } else {
            return value;
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Rect getLocationOnScreen(View mEditText) {
        Rect mRect = new Rect();
        int[] location = new int[2];

        mEditText.getLocationOnScreen(location);

        mRect.left = location[0];
        mRect.top = location[1];
        mRect.right = location[0] + mEditText.getWidth();
        mRect.bottom = location[1] + mEditText.getHeight();

        return mRect;
    }

    public static byte[] pack(Parcelable parcelable) {
        Parcel parcel = Parcel.obtain();
        parcelable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle();
        return bytes;
    }

    public static <T> T unpack(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0);
        T result = creator.createFromParcel(parcel);
        parcel.recycle();
        return result;
    }

    /**
     * @param H
     *            0-360
     * @param S
     *            0-100
     * @param V
     *            0-100
     * @return color in hex string
     */
    public static String hsvToHexa(float H, float S, float V) {

        float R, G, B;

        H /= 360.0f;
        S /= 100.0f;
        V /= 100.0f;

        if (S == 0)
        {
            R = V * 255;
            G = V * 255;
            B = V * 255;
        } else {
            float var_h = H * 6;
            if (var_h == 6)
                var_h = 0; // H must be < 1
            int var_i = (int) Math.floor((double) var_h); // Or ... var_i =
            // floor( var_h )
            float var_1 = V * (1 - S);
            float var_2 = V * (1 - S * (var_h - var_i));
            float var_3 = V * (1 - S * (1 - (var_h - var_i)));

            float var_r;
            float var_g;
            float var_b;
            if (var_i == 0) {
                var_r = V;
                var_g = var_3;
                var_b = var_1;
            } else if (var_i == 1) {
                var_r = var_2;
                var_g = V;
                var_b = var_1;
            } else if (var_i == 2) {
                var_r = var_1;
                var_g = V;
                var_b = var_3;
            } else if (var_i == 3) {
                var_r = var_1;
                var_g = var_2;
                var_b = V;
            } else if (var_i == 4) {
                var_r = var_3;
                var_g = var_1;
                var_b = V;
            } else {
                var_r = V;
                var_g = var_1;
                var_b = var_2;
            }

            R = var_r * 255; // RGB results from 0 to 255
            G = var_g * 255;
            B = var_b * 255;
        }

        String rs = Integer.toHexString((int) (R));
        String gs = Integer.toHexString((int) (G));
        String bs = Integer.toHexString((int) (B));

        if (rs.length() == 1)
            rs = "0" + rs;
        if (gs.length() == 1)
            gs = "0" + gs;
        if (bs.length() == 1)
            bs = "0" + bs;
        return "#" + rs + gs + bs;
    }

    public static void hideSoftKeyboard(Context context, View view) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }


}
