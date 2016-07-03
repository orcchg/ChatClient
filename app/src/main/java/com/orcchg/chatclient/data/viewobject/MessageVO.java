package com.orcchg.chatclient.data.viewobject;

public class MessageVO {
    private long mId;
    private String mLogin;
    private String mEmail;
    private String mMessage;
    private long mTimestamp;

    public MessageVO(Builder builder) {
        mId = builder.mId;
        mLogin = builder.mLogin;
        mEmail = builder.mEmail;
        mMessage = builder.mMessage;
        mTimestamp = builder.mTimestamp;
    }

    public static class Builder {
        private final long mId;
        private String mLogin;
        private String mEmail;
        private String mMessage;
        private long mTimestamp;

        public Builder(long id) {
            mId = id;
        }

        public Builder setLogin(String login) {
            mLogin = login;
            return this;
        }

        public Builder setEmail(String email) {
            mEmail = email;
            return this;
        }

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public Builder setTimestamp(long timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        public MessageVO build() {
            return new MessageVO(this);
        }
    }

    public long getId() {
        return mId;
    }

    public String getLogin() {
        return mLogin;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getMessage() {
        return mMessage;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
