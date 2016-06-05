package com.orcchg.chatclient.data.model;

import com.google.gson.annotations.SerializedName;

public class Status {
    public static final long UNKNOWN_ID = 0;

    @SerializedName("code") private int mCode;
    @SerializedName("id") private long mId;
    @SerializedName("token") private String mToken;
    @SerializedName("payload") private String mPayload;

    public int getCode() {
        return mCode;
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
}
