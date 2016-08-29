package com.orcchg.chatclient.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

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

    public static void showSoftKeyboard(Activity activity, boolean isShow) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            if (isShow) {
                imm.toggleSoftInputFromWindow(view.getWindowToken(), InputMethodManager.SHOW_IMPLICIT, InputMethodManager.HIDE_IMPLICIT_ONLY);
            } else {
                imm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        }
    }
}
