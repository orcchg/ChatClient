package com.orcchg.chatclient.data.model;

import android.support.annotation.IntDef;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;

import java.io.StringReader;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static com.orcchg.chatclient.data.model.Check.CHECK_FALSE;
import static com.orcchg.chatclient.data.model.Check.CHECK_TRUE;

public class Check {
    public static final int CHECK_FALSE = 0;
    public static final int CHECK_TRUE = 1;
    @IntDef({CHECK_FALSE, CHECK_TRUE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface CheckFlag {}

    @SerializedName("check") private @CheckFlag int mCheck = CHECK_FALSE;
    @SerializedName("action") private int mAction = Status.ACTION_UNKNOWN;
    @SerializedName("id") private long mId = Status.UNKNOWN_ID;

    @CheckFlag
    public int getCheck() {
        return mCheck;
    }

    @Status.Action
    public int getAction() {
        return mAction;
    }

    public long getId() {
        return mId;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static Check fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, Check.class);
    }
}
