package com.orcchg.chatclient.data.model;

import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SystemMessage {
    public static final int CHANNEL_MOVE_UNKNOWN = -1;
    public static final int CHANNEL_MOVE_ENTER = 0;
    public static final int CHANNEL_MOVE_EXIT = 1;
    @IntDef({CHANNEL_MOVE_UNKNOWN, CHANNEL_MOVE_ENTER, CHANNEL_MOVE_EXIT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ChannelMove {}

    @SerializedName("system") private String mMessage;
    @SerializedName("action") private int mAction = Status.ACTION_UNKNOWN;
    @SerializedName("id") private long mId = Status.UNKNOWN_ID;
    @SerializedName("payload") private String mPayload;

    public String getMessage() {
        return mMessage;
    }

    @Status.Action
    public int getAction() {
        return mAction;
    }

    public long getId() {
        return mId;
    }

    public String getPayload() {
        return mPayload;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SystemMessage fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, SystemMessage.class);
    }
}
