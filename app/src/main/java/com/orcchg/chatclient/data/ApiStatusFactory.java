package com.orcchg.chatclient.data;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ApiStatusFactory {

    public static final int STATUS_UNKNOWN = -1;
    public static final int STATUS_SUCCESS = 0;
    public static final int STATUS_WRONG_PASSWORD = 1;
    public static final int STATUS_NOT_REGISTERED = 2;
    public static final int STATUS_ALREADY_REGISTERED = 3;
    public static final int STATUS_ALREADY_LOGGED_IN = 4;
    public static final int STATUS_INVALID_FORM = 5;
    public static final int STATUS_INVALID_QUERY = 6;
    public static final int STATUS_UNAUTHORIZED = 7;
    public static final int STATUS_WRONG_CHANNEL = 8;
    public static final int STATUS_SAME_CHANNEL = 9;
    public static final int STATUS_NO_SUCH_PEER = 10;
    public static final int STATUS_NOT_REQUESTED = 11;
    @IntDef({
        STATUS_UNKNOWN,
        STATUS_SUCCESS,
        STATUS_WRONG_PASSWORD,
        STATUS_NOT_REGISTERED,
        STATUS_ALREADY_REGISTERED,
        STATUS_ALREADY_LOGGED_IN,
        STATUS_INVALID_FORM,
        STATUS_INVALID_QUERY,
        STATUS_UNAUTHORIZED,
        STATUS_WRONG_CHANNEL,
        STATUS_SAME_CHANNEL,
        STATUS_NO_SUCH_PEER,
        STATUS_NOT_REQUESTED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Status {}

    @Status
    public static int getStatusByCode(int code) {
        return code;
    }
}
