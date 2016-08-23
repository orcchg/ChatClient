package com.orcchg.chatclient.data.viewobject;

public class PeerVO implements Comparable<PeerVO> {
    private long mId;
    private String mLogin;
    private String mEmail;
    private int mChannel;
    private boolean mIsSelected;  // for view purposes

    public PeerVO(Builder builder) {
        mId = builder.mId;
        mLogin = builder.mLogin;
        mEmail = builder.mEmail;
        mChannel = builder.mChannel;
        mIsSelected = false;
    }

    public static class Builder {
        private final long mId;
        private String mLogin;
        private String mEmail;
        private int mChannel;

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

    public String getEmail() {
        return mEmail;
    }

    public int getChannel() {
        return mChannel;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    @Override
    public int compareTo(PeerVO peerVO) {
        return (int) (mId - peerVO.mId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PeerVO peerVO = (PeerVO) o;
        return mId == peerVO.mId;
    }

    @Override
    public int hashCode() {
        return (int) (mId ^ (mId >>> 32));
    }
}
