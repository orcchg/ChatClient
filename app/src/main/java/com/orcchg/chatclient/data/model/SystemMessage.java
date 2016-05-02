package com.orcchg.chatclient.data.model;

import com.google.gson.annotations.SerializedName;

public class SystemMessage {
    @SerializedName("system") private String mMessage;

    public String getMessage() {
        return mMessage;
    }
}
