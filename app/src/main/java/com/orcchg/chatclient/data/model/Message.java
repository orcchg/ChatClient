package com.orcchg.chatclient.data.model;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id") private long mId;
    @SerializedName("login") private String mLogin;
    @SerializedName("channel") private int mChannel;
    @SerializedName("dest_id") private long mDestId;
    @SerializedName("timestamp") private long mTimestamp;
    @SerializedName("message") private String mMessage;

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getLogin() {
        return mLogin;
    }

    public void setLogin(String mLogin) {
        this.mLogin = mLogin;
    }

    public int getChannel() {
        return mChannel;
    }

    public void setChannel(int mChannel) {
        this.mChannel = mChannel;
    }

    public long getDestId() {
        return mDestId;
    }

    public void setDestId(long mDestId) {
        this.mDestId = mDestId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long mTimestamp) {
        this.mTimestamp = mTimestamp;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String mMessage) {
        this.mMessage = mMessage;
    }
}
