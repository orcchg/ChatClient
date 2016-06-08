package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id") private long mId;
    @SerializedName("login") private String mLogin;
    @SerializedName("channel") private int mChannel;
    @SerializedName("dest_id") private long mDestId;
    @SerializedName("timestamp") private long mTimestamp;
    @SerializedName("message") private String mMessage;

    public Message(Builder builder) {
        mId = builder.mId;
        mLogin = builder.mLogin;
        mChannel = builder.mChannel;
        mDestId = builder.mDestId;
        mTimestamp = builder.mTimestamp;
        mMessage = builder.mMessage;
    }

    public static class Builder {
        private final long mId;
        private final String mLogin;
        private int mChannel;
        private long mDestId;
        private long mTimestamp;
        private String mMessage;

        public Builder(long id, String login) {
            mId = id;
            mLogin = login;
        }

        public Builder setChannel(int channel) {
            mChannel = channel;
            return this;
        }

        public Builder setDestId(long destId) {
            mDestId = destId;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Message build() {
            return new Message(this);
        }
    }

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

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Message.class);
    }
}
