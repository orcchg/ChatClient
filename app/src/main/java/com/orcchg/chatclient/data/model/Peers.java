package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.util.List;

public class Peers {
    @SerializedName("peers") private List<Peer> mPeers;
    @SerializedName("channel") private int mChannel = Status.WRONG_CHANNEL;

    public List<Peer> getPeers() {
        return mPeers;
    }

    public int getChannel() {
        return mChannel;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Peers fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Peers.class);
    }
}
