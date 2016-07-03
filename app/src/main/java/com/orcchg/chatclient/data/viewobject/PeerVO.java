package com.orcchg.chatclient.data.viewobject;

public class PeerVO {
    private long mId;
    private String mLogin;
    private int mChannel;

    public PeerVO(Builder builder) {
        mId = builder.mId;
        mLogin = builder.mLogin;
        mChannel = builder.mChannel;
    }

    public static class Builder {
        private final long mId;
        private String mLogin;
        private int mChannel;

        public Builder(long id) {
            mId = id;
        }

        public Builder setLogin(String login) {
            mLogin = login;
            return this;
        }

        public Builder setChannel(int channel) {
            mChannel = channel;
            return this;
        }
        public PeerVO build() {
            return new PeerVO(this);
        }
    }

    public long getId() {
        return mId;
    }

    public String getLogin() {
        return mLogin;
    }

    public int getChannel() {
        return mChannel;
    }
}
