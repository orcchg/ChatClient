package com.orcchg.chatclient;

import android.app.Application;

import com.orcchg.chatclient.data.DataManager;

import timber.log.Timber;

public class ChatClientApplication extends Application {
    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        mDataManager = new DataManager();
    }

    public DataManager getDataManager() {
        return mDataManager;
    }
}
