package com.orcchg.chatclient.data.model;

import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Status {
    public static final long SYSTEM_ID = -1;
    public static final long UNKNOWN_ID = 0;
    public static final int WRONG_CHANNEL = -1;
    public static final int DEFAULT_CHANNEL = 0;

    public static final int ACTION_UNKNOWN = -1;
    public static final int ACTION_LOGIN = 0;
    public static final int ACTION_REGISTER = 1;
    public static final int ACTION_MESSAGE = 2;
    public static final int ACTION_LOGOUT = 3;
    public static final int ACTION_SWITCH_CHANNEL = 4;
    public static final int ACTION_IS_LOGGED_IN = 5;
    public static final int ACTION_IS_REGISTERED = 6;
    public static final int ACTION_ALL_PEERS = 7;
    @IntDef({
        ACTION_UNKNOWN,
        ACTION_LOGIN,
        ACTION_REGISTER,
        ACTION_MESSAGE,
        ACTION_LOGOUT,
        ACTION_SWITCH_CHANNEL,
        ACTION_IS_LOGGED_IN,
        ACTION_IS_REGISTERED,
        ACTION_ALL_PEERS
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
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Status.class);
    }
}
