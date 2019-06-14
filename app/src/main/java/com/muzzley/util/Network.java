package com.muzzley.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Network {

    public static boolean isNetworkAvailable(Context context) {
        return getCurrentNetwork(context) != null;
    }

    public static boolean equals(NetworkInfo n1, NetworkInfo n2) {
        if (n1 == null || n2 == null) return false;
        if (n1.getType() != n2.getType()) return false;
        if (n1.getSubtype() != n2.getSubtype()) return false;
        if (n1.getExtraInfo() == null && n2.getExtraInfo() != null) return false;
        if (n1.getExtraInfo() != null && n2.getExtraInfo() == null) return false;
        if (n1.getExtraInfo() != null && n2.getExtraInfo() != null && !n1.getExtraInfo().equals(n2.getExtraInfo()))
            return false;
        if (n1.getReason() != null && n2.getReason() == null) return false;
        if (n1.getReason() == null && n2.getReason() != null) return false;
        if (n1.getReason() != null && n2.getReason() != null && !n1.getReason().equals(n2.getReason()))
            return false;
        return true;
    }

    public static NetworkInfo getCurrentNetwork(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected() ? activeNetworkInfo : null;
    }

    public static boolean isConnected(Context context){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }
}
