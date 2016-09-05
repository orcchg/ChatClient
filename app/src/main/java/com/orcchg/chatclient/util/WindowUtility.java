package com.orcchg.chatclient.util;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.orcchg.chatclient.R;

import timber.log.Timber;

public class WindowUtility {

    public static void logScreenParams(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        Timber.i("Dots-per-Inch: %s, density: %s", metrics.densityDpi, metrics.density);
        Timber.i("Screen width: %s, height: %s", metrics.widthPixels, metrics.heightPixels);
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

    public static void tintStatusBar(@NonNull Activity activity, @ColorInt int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().setStatusBarColor(color);
        }
    }

    public static void tintStatusBarAnimated(final @NonNull Activity activity, @ColorInt int fromColor, @ColorInt int toColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ValueAnimator animator = ValueAnimator.ofArgb(fromColor, toColor);
            animator.setDuration(250);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        activity.getWindow().setStatusBarColor((int) animation.getAnimatedValue());
                    }
                }
            });
            animator.start();
        }
    }
}
