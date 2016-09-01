package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class Message {
    @SerializedName("id") private long mId = Status.UNKNOWN_ID;
    @SerializedName("login") private String mLogin;
    @SerializedName("email") private String mEmail;
    @SerializedName("channel") private int mChannel = Status.DEFAULT_CHANNEL;
    @SerializedName("dest_id") private long mDestId = Status.UNKNOWN_ID;
    @SerializedName("timestamp") private long mTimestamp;
    @SerializedName("size") private int mSize;
    @SerializedName("encrypted") private int mIsEncrypted;
    @SerializedName("message") private String mMessage;

    public Message(Builder builder) {
        mId = builder.mId;
        mLogin = builder.mLogin;
        mEmail = builder.mEmail;
        mChannel = builder.mChannel;
        mDestId = builder.mDestId;
        mTimestamp = builder.mTimestamp;
        mSize = builder.mSize;
        mIsEncrypted = builder.mIsEncrypted ? 1 : 0;
        mMessage = builder.mMessage;
    }

    public static class Builder {
        private final long mId;
        private final String mLogin;
        private final String mEmail;
        private int mChannel = Status.DEFAULT_CHANNEL;
        private long mDestId = Status.UNKNOWN_ID;
        private long mTimestamp;
        private int mSize;
        private boolean mIsEncrypted;
        private String mMessage;

        public Builder(long id, String login, String email) {
            mId = id;
            mLogin = login;
            mEmail = email;
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

        public Builder setEncrypted(boolean isEncrypted) {
            mIsEncrypted = isEncrypted;
            return this;
        }

        public Builder setMessage(String message) {
            mSize = message.length();
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

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public int getChannel() {
        return mChannel;
    }

    public void setChannel(int channel) {
        this.mChannel = channel;
    }

    public long getDestId() {
        return mDestId;
    }

    public void setDestId(long destId) {
        this.mDestId = destId;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public void setTimestamp(long timestamp) {
        this.mTimestamp = timestamp;
    }

    public int getSize() {
        return mSize;
    }

    public boolean isEncrypted() {
        return mIsEncrypted != 0;
    }

    public void setEncrypted(boolean isEncrypted) {
        this.mIsEncrypted = isEncrypted ? 1 : 0;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setMessage(String message) {
        this.mMessage = message;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Message fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Message.class);
    }
}
