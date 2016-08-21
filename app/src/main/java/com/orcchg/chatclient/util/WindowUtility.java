package com.orcchg.chatclient.util;

import android.content.Context;
import android.util.DisplayMetrics;

import com.orcchg.chatclient.R;

import timber.log.Timber;

public class WindowUtility {

    public static void logScreenParams(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Timber.i("Dots-per-Inch: %s, density: %s", metrics.densityDpi, metrics.density);
    }

    public static boolean isTablet(Context context) {
        return context.getResources().getBoolean(R.bool.is_tablet);
    }
}
