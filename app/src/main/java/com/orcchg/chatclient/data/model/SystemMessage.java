package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class SystemMessage {
    @SerializedName("system") private String mMessage;

    public String getMessage() {
        return mMessage;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static SystemMessage fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, SystemMessage.class);
    }
}
