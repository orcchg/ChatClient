package com.orcchg.chatclient.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

public class NetworkUtility {

    public static final String ERROR_SERVER_UNAVAILABLE = "ECONNREFUSED";  // server not started
    public static final String ERROR_SERVER_SHUTDOWN = "ECONNRESET";  // server's just stopped, also on client logout
    public static final String ERROR_CONNECTION_INTERRUPTED = "ETIMEDOUT";  // poor network disconnect
    public static final String ERROR_NETWORK_UNREACHABLE = "ENETUNREACH";  // no network during retry
    @StringDef({
            ERROR_SERVER_UNAVAILABLE,
            ERROR_SERVER_SHUTDOWN,
            ERROR_CONNECTION_INTERRUPTED,
            ERROR_NETWORK_UNREACHABLE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ConnectionError {}

    @Nullable
    @ConnectionError
    public static String getNetworkError(Throwable e) {
        String message = e.getMessage();
        if (message != null) {
            int index = message.indexOf("E");
            if (index >= 0) {
                int whitespace = message.indexOf(' ', index);
                String error = message.substring(index, whitespace);
                Timber.i("Error code: %s", error);
                return error;
            }
        }
        return null;
    }

    public static boolean isNetworkAccessible(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
