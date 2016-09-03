package com.orcchg.chatclient.util;

import android.support.annotation.Nullable;

import java.util.SimpleTimeZone;
import java.util.TimeZone;

import hirondelle.date4j.DateTime;

public class CommonUtility {

    public static TimeZone initTimeZone() {
        int rawOffset = -8 * 60 * 60 * 1000;
        String[] ids = TimeZone.getAvailableIDs(rawOffset);
        if (ids.length < 1) {
            return TimeZone.getDefault();
        }
        return new SimpleTimeZone(rawOffset, ids[0]);
    }

    public static DateTime convert(long millis, @Nullable TimeZone tz) {
        if (tz == null) {
            tz = initTimeZone();
        }
        return DateTime.forInstant(millis, tz);
    }

    public static String getTime(long millis, @Nullable TimeZone tz) {
        DateTime dt = convert(millis, tz);
        int hour = dt.getHour();
        int minute = dt.getMinute();
        StringBuilder builder = new StringBuilder();
        if (hour == 0) {
            builder.append('0');
        }
        builder.append(hour).append(':');
        if (minute < 10) {
            builder.append('0');
        }
        builder.append(minute);
        return builder.toString();
    }
}
