package com.orcchg.chatclient.data.viewobject;

import android.os.Parcel;
import android.os.Parcelable;

import com.orcchg.chatclient.data.model.Status;

public class MessageVO implements Parcelable {
    private long mId = Status.UNKNOWN_ID;
    private long mDestId = Status.UNKNOWN_ID;
    private String mLogin;
    private String mEmail;
    private String mMessage;
    private long mTimestamp;
    private boolean mIsDedicated;  // this message is only to you

    public MessageVO(Builder builder) {
        mId = builder.mId;
        mDestId = builder.mDestId;
        mLogin = builder.mLogin;
        mEmail = builder.mEmail;
        mMessage = builder.mMessage;
        mTimestamp = builder.mTimestamp;
        mIsDedicated = builder.mIsDedicated;
    }

    public static class Builder {
        private final long mId;
        private long mDestId = Status.UNKNOWN_ID;
        private String mLogin;
        private String mEmail;
        private String mMessage;
        private long mTimestamp;
        private boolean mIsDedicated;

        public Builder(long id) {
            mId = id;
        }

        public Builder setDestId(long destId) {
            mDestId = destId;
            return this;
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

        public Builder setDedicated(boolean isDedicated) {
            mIsDedicated = isDedicated;
            return this;
        }

        public MessageVO build() {
            return new MessageVO(this);
        }
    }

    public long getId() {
        return mId;
    }

    public long getDestId() {
        return mDestId;
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

    public boolean isDedicated() {
        return mIsDedicated;
    }

    public void setDedicated(boolean isDedicated) {
        mIsDedicated = isDedicated;
    }

    /* Parcelable */
    // --------------------------------------------------------------------------------------------
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mId);
        parcel.writeString(mLogin);
        parcel.writeString(mEmail);
        parcel.writeString(mMessage);
        parcel.writeLong(mTimestamp);
    }

    private MessageVO(Parcel parcel) {
        mId = parcel.readLong();
        mLogin = parcel.readString();
        mEmail = parcel.readString();
        mMessage = parcel.readString();
        mTimestamp = parcel.readLong();
    }

    public static final Parcelable.Creator<MessageVO> CREATOR = new Parcelable.Creator<MessageVO>() {
        @Override
        public MessageVO createFromParcel(Parcel parcel) {
            return new MessageVO(parcel);
        }

        @Override
        public MessageVO[] newArray(int size) {
            return new MessageVO[size];
        }
    };
}
