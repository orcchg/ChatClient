package com.orcchg.chatclient.data.viewobject;

public class AuthFormVO {
    private String mLogin;
    private String mEmail;
    private String mPassword;

    public AuthFormVO(Builder builder) {
        mLogin = builder.mLogin;
        mEmail = builder.mEmail;
        mPassword = builder.mPassword;
    }

    public static class Builder {
        private String mLogin;
        private String mEmail;
        private String mPassword;

        public Builder setLogin(String login) {
            mLogin = login;
            return this;
        }

        public Builder setEmail(String email) {
            mEmail = email;
            return this;
        }

        public Builder setPassword(String password) {
            mPassword = password;
            return this;
        }

        public AuthFormVO build() {
            return new AuthFormVO(this);
        }
    }

    public String getLogin() {
        return mLogin;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getPassword() {
        return mPassword;
    }
}
