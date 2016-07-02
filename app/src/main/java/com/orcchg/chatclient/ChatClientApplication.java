package com.orcchg.chatclient;

import android.app.Application;

import com.orcchg.chatclient.data.DataManager;
import com.orcchg.chatclient.data.remote.RestAdapter;
import com.orcchg.chatclient.data.remote.ServerBridge;

import timber.log.Timber;

public class ChatClientApplication extends Application {

    private RestAdapter mRestAdapter;
    private ServerBridge mServer;
    private DataManager mDataManager;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());

        mRestAdapter = RestAdapter.Creator.create();
        mServer = new ServerBridge();
        mDataManager = new DataManager(mRestAdapter, mServer);
    }

    public DataManager getDataManager() {
        return mDataManager;
    }
}
