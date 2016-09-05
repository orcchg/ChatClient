package com.orcchg.chatclient.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import timber.log.Timber;

import static com.orcchg.chatclient.util.FrameworkUtility.RequestCode.CHAT_ACTIVITY;
import static com.orcchg.chatclient.util.FrameworkUtility.RequestCode.LOGIN_ACTIVITY;
import static com.orcchg.chatclient.util.FrameworkUtility.RequestCode.MAIN_ACTIVITY;
import static com.orcchg.chatclient.util.FrameworkUtility.RequestCode.REGISTRATION_ACTIVITY;

public class FrameworkUtility {

    public static class RequestCode {
        public static final int MAIN_ACTIVITY = 0;
        public static final int LOGIN_ACTIVITY = 1;
        public static final int REGISTRATION_ACTIVITY = 2;
        public static final int CHAT_ACTIVITY = 3;
        @IntDef({MAIN_ACTIVITY, LOGIN_ACTIVITY, REGISTRATION_ACTIVITY, CHAT_ACTIVITY})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Code {}

        public static final int ACTIVITIES_COUNT = CHAT_ACTIVITY + 1;
    }

    private static final boolean[] sActive = new boolean[RequestCode.ACTIVITIES_COUNT];
    private static final boolean[] sFinishing = new boolean[RequestCode.ACTIVITIES_COUNT];
    private static int sActiveCount = 0;

    /* Active */
    // --------------------------------------------------------------------------------------------
    public static void setActive(@RequestCode.Code int code) {
        if (!isActive(code)) {
            ++sActiveCount;
        }
        sActive[code] = true;
        Timber.v("setActive(): active %s, total %s", requestScreen(code), sActiveCount);
    }

    public static void setInactive(@RequestCode.Code int code) {
        if (isActive(code)) {
            --sActiveCount;
        }
        sActive[code] = false;
        Timber.v("setInactive(): inactive %s, total %s", requestScreen(code), sActiveCount);
    }

    public static boolean isActive(@RequestCode.Code int code) {
        return sActive[code];
    }

    public static int getActiveCount() {
        return sActiveCount;
    }

    /* Finishing */
    // --------------------------------------------------------------------------------------------
    public static void setFinishing(@RequestCode.Code int code, boolean isFinishing) {
        sFinishing[code] = isFinishing;
        Timber.v("setFinishing(): finishing %s, flag: %s", requestScreen(code), Boolean.toString(isFinishing));
    }

    public static boolean isFinishing(@RequestCode.Code int code) {
        return sFinishing[code];
    }

    public static void diagnostic() {
        for (int i = 0; i < RequestCode.ACTIVITIES_COUNT; ++i) {
            Timber.v("Screen: %s, active: %s, finishing: %s",
                    requestScreen(i), Boolean.toString(sActive[i]), Boolean.toString(sFinishing[i]));
        }
    }

    /* Internal */
    // --------------------------------------------------------------------------------------------
    private static String requestScreen(@RequestCode.Code int code) {
        switch (code) {
            case MAIN_ACTIVITY:          return "Main";
            case LOGIN_ACTIVITY:         return "Login";
            case REGISTRATION_ACTIVITY:  return "Registration";
            case CHAT_ACTIVITY:          return "Chat";
        }
        return null;
    }
}
