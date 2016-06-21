package com.orcchg.chatclient.util;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

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
    private static int sActiveCount = 0;

    public static void setActive(@RequestCode.Code int code) {
        if (!isActive(code)) {
            ++sActiveCount;
        }
        sActive[code] = true;
    }

    public static void setInactive(@RequestCode.Code int code) {
        if (isActive(code)) {
            --sActiveCount;
        }
        sActive[code] = false;
    }

    public static boolean isActive(@RequestCode.Code int code) {
        return sActive[code];
    }

    public static int getActiveCount() {
        return sActiveCount;
    }
}
