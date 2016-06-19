package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

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
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, SystemMessage.class);
    }
}
