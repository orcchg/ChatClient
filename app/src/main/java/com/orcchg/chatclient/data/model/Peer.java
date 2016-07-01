package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class Peer {
    @SerializedName("id") private long mId;
    @SerializedName("login") private String mLogin;
    @SerializedName("channel") private int mChannel = Status.WRONG_CHANNEL;

    public long getId() {
        return mId;
    }

    public String getLogin() {
        return mLogin;
    }

    public int getChannel() {
        return mChannel;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Peer fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Peer.class);
    }
}
