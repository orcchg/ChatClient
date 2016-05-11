package com.orcchg.chatclient.data.viewobject;

public class MessageVO {
    private String mLogin;
    private String mMessage;
    private long mTimestamp;

    public MessageVO(Builder builder) {
        mLogin = builder.mLogin;
        mMessage = builder.mMessage;
        mTimestamp = builder.mTimestamp;
    }

    public static class Builder {
        private String mLogin;
        private String mMessage;
        private long mTimestamp;

        public Builder setLogin(String login) {
            mLogin = login;
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

    public String getLogin() {
        return mLogin;
    }

    public String getMessage() {
        return mMessage;
    }

    public long getTimestamp() {
        return mTimestamp;
    }
}
