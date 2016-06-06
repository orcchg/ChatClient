package com.orcchg.chatclient.data;

import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.remote.RestAdapter;
import com.orcchg.chatclient.data.remote.ServerBridge;

import rx.Observable;

public class DataManager {

    private RestAdapter mRestAdapter;  // TODO: inject
    private ServerBridge mServer;  // TODO: inject

    public DataManager(RestAdapter restAdapter, ServerBridge server) {
        mRestAdapter = restAdapter;
        mServer = server;
    }

    /* Direct connection bridge */
    // --------------------------------------------------------------------------------------------
    public void setConnectionCallback(ServerBridge.ConnectionCallback callback) {
        mServer.setConnectionCallback(callback);
    }

    public void connect() {
        mServer.startConnection();
    }

    public void disconnect() {
        mServer.closeConnection();
    }

    /* Authentication */
    // --------------------------------------------------------------------------------------------
    public Observable<LoginForm> getLoginForm() {
        return mRestAdapter.getLoginForm();
    }

    public Observable<Status> sendLoginForm(LoginForm form) {
        return mRestAdapter.sendLoginForm(form);
    }

    public Observable<RegistrationForm> getRegistrationForm() {
        return mRestAdapter.getRegistrationForm();
    }

    public Observable<Status> sendRegistrationForm(RegistrationForm form) {
        return mRestAdapter.sendRegistrationForm(form);
    }

    /* Chat */
    // --------------------------------------------------------------------------------------------
    public Observable<Status> sendMessage(Message message) {
        return mRestAdapter.sendMessage(message);
    }

    /* Access */
    // --------------------------------------------------------------------------------------------
    public Observable<Status> logout(long id, String name) {
        return mRestAdapter.logout(id, name);
    }
}
