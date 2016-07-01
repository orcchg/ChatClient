package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class SystemMessage {
    @SerializedName("system") private String mMessage;
    @SerializedName("action") private int mAction;
    @SerializedName("id") private long mId;
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
