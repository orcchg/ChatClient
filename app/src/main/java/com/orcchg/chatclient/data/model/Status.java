package com.orcchg.chatclient.data.model;

import com.google.gson.annotations.SerializedName;

public class Status {
    @SerializedName("code") private int mCode;
    @SerializedName("id") private long mId;
    @SerializedName("token") private String mToken;

    public int getCode() {
        return mCode;
    }

    public long getId() {
        return mId;
    }

    public String getToken() {
        return mToken;
    }
}