package com.orcchg.chatclient.data.viewobject;

public class SystemMessageVO {
    private String mMessage;

    public SystemMessageVO(Builder builder) {
        mMessage = builder.mMessage;
    }

    public static class Builder {
        private String mMessage;

        public Builder setMessage(String message) {
            mMessage = message;
            return this;
        }

        public SystemMessageVO build() {
            return new SystemMessageVO(this);
        }
    }
}
