package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class LoginForm {
    @SerializedName("login") protected String mLogin;
    @SerializedName("password") protected String mPassword;

    public LoginForm(String login, String password) {
        mLogin = login;
        mPassword = password;
    }

    public String getLogin() {
        return mLogin;
    }

    public void setLogin(String mLogin) {
        this.mLogin = mLogin;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static LoginForm fromJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, LoginForm.class);
    }
}
