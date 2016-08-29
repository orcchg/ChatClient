package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;

public class Peer {
    @SerializedName("id") private long mId = Status.UNKNOWN_ID;
    @SerializedName("login") private String mLogin;
    @SerializedName("email") private String mEmail;
    @SerializedName("channel") private int mChannel = Status.WRONG_CHANNEL;

    public long getId() {
        return mId;
    }

    public String getLogin() {
        return mLogin;
    }

    public String getEmail() {
        return mEmail;
    }

    public int getChannel() {
        return mChannel;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    @Override
    public String toString() {
        return new StringBuilder("Peer: id=").append(mId)
                .append(", login=").append(mLogin)
                .append(", email=").append(mEmail)
                .append(", channel=").append(mChannel)
                .toString();
    }

    public static Peer fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Peer.class);
    }
}
