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

    public static String PACKAGE_NAME;

    @Override
    public void onCreate() {
        super.onCreate();
        PACKAGE_NAME = getPackageName();
        Timber.plant(new Timber.DebugTree() {
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                return PACKAGE_NAME + ":" + super.createStackElementTag(element) + ":" + element.getLineNumber();
            }
        });

        mRestAdapter = RestAdapter.Creator.create();
        mServer = new ServerBridge();
        mDataManager = new DataManager(mRestAdapter, mServer);
    }

    public DataManager getDataManager() {
        return mDataManager;
    }
}
