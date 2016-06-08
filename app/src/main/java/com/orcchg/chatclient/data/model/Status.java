package com.orcchg.chatclient.data.model;

import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Status {
    public static final long UNKNOWN_ID = 0;

    public static final int ACTION_UNKNOWN = -1;
    public static final int ACTION_LOGIN = 0;
    public static final int ACTION_REGISTER = 1;
    public static final int ACTION_MESSAGE = 2;
    public static final int ACTION_LOGOUT = 3;
    public static final int ACTION_SWITCH_CHANNEL = 4;
    @IntDef({
        ACTION_UNKNOWN,
        ACTION_LOGIN,
        ACTION_REGISTER,
        ACTION_MESSAGE,
        ACTION_LOGOUT,
        ACTION_SWITCH_CHANNEL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {}

    @SerializedName("code") private int mCode;
    @SerializedName("action") private int mAction;
    @SerializedName("id") private long mId;
    @SerializedName("token") private String mToken;
    @SerializedName("payload") private String mPayload;

    public int getCode() {
        return mCode;
    }

    @Action
    public int getAction() {
        return mAction;
    }

    public long getId() {
        return mId;
    }

    public String getToken() {
        return mToken;
    }

    public String getPayload() {
        return mPayload;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Status fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Status.class);
    }
}
