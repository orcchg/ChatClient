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
    public static final int ACTION_KICK = -2;
    public static final int ACTION_ADMIN = -3;
    public static final int ACTION_LOGIN = 0;
    public static final int ACTION_REGISTER = 1;
    public static final int ACTION_MESSAGE = 2;
    public static final int ACTION_LOGOUT = 3;
    public static final int ACTION_SWITCH_CHANNEL = 4;
    public static final int ACTION_PEER_ID = 5;
    public static final int ACTION_IS_LOGGED_IN = 6;
    public static final int ACTION_IS_REGISTERED = 7;
    public static final int ACTION_CHECK_AUTH = 8;
    public static final int ACTION_KICK_BY_AUTH = 9;
    public static final int ACTION_ALL_PEERS = 10;
    public static final int ACTION_PRIVATE_REQUEST = 11;
    public static final int ACTION_PRIVATE_CONFIRM = 12;
    public static final int ACTION_PRIVATE_ABORT = 13;
    public static final int ACTION_PRIVATE_PUBKEY = 14;
    public static final int ACTION_PRIVATE_PUBKEY_EXCHANGE = 15;
    @IntDef({
        ACTION_UNKNOWN,
        ACTION_KICK,
        ACTION_ADMIN,
        ACTION_LOGIN,
        ACTION_REGISTER,
        ACTION_MESSAGE,
        ACTION_LOGOUT,
        ACTION_SWITCH_CHANNEL,
        ACTION_PEER_ID,
        ACTION_IS_LOGGED_IN,
        ACTION_IS_REGISTERED,
        ACTION_CHECK_AUTH,
        ACTION_KICK_BY_AUTH,
        ACTION_ALL_PEERS,
        ACTION_PRIVATE_REQUEST,
        ACTION_PRIVATE_CONFIRM,
        ACTION_PRIVATE_ABORT,
        ACTION_PRIVATE_PUBKEY,
        ACTION_PRIVATE_PUBKEY_EXCHANGE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Action {}

    @SerializedName("code") private int mCode;
    @SerializedName("action") private int mAction = Status.ACTION_UNKNOWN;
    @SerializedName("id") private long mId = Status.UNKNOWN_ID;
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
