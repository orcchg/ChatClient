package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class RegistrationForm extends LoginForm {
    @SerializedName("email") protected String mEmail;

    public RegistrationForm(String login, String email, String password) {
        super(login, password);
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setEmail(String mEmail) {
        this.mEmail = mEmail;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static RegistrationForm fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, RegistrationForm.class);
    }
}
