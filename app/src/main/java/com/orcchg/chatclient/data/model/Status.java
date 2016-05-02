package com.orcchg.chatclient.data.model;

import com.google.gson.annotations.SerializedName;

public class Status {
    @SerializedName("code") private int mCode;
    @SerializedName("id") private long mId;

    public int getCode() {
        return mCode;
    }

    public long getId() {
        return mId;
    }
}
