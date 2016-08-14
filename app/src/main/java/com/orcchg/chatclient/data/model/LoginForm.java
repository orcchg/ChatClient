package com.orcchg.chatclient.data.model;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.stream.JsonReader;
import com.orcchg.chatclient.util.crypting.RSACryptor;

import java.io.StringReader;

public class LoginForm {
    @SerializedName("login") protected String mLogin;
    @SerializedName("password") protected String mPassword;
    @SerializedName("encrypted") protected int mIsEncrypted;

    public LoginForm(String login, String password) {
        mLogin = login;
        mPassword = password;
        mIsEncrypted = 0;
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

    public boolean isEncrypted() {
        return mIsEncrypted != 0;
    }

    public void setEncrypted(boolean isEncrypted) {
        mIsEncrypted = isEncrypted ? 1 : 0;
    }

    public String toJson() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static LoginForm fromJson(String json) {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new StringReader(json));
        reader.setLenient(true);
        return gson.fromJson(reader, LoginForm.class);
    }

    public void encrypt(String publicPem) {
        boolean[] encrypted = new boolean[1];
        mPassword = RSACryptor.encryptRSA(mPassword, publicPem, encrypted);
        mIsEncrypted = encrypted[0] ? 1 : 0;
    }
}
