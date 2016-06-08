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

    public void openDirectConnection() {
        mServer.openConnection();
    }

    public void closeDirectConnection() {
        mServer.closeConnection();
    }

    /* Authentication */
    // --------------------------------------------------------------------------------------------
    public Observable<LoginForm> getLoginForm() {
        return mRestAdapter.getLoginForm();
    }

    public void getLoginFormDirect() {
        StringBuilder line = new StringBuilder("GET /login HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Status> sendLoginForm(LoginForm form) {
        return mRestAdapter.sendLoginForm(form);
    }

    public void sendLoginFormDirect(LoginForm form) {
        String json = form.toJson();
        StringBuilder line = new StringBuilder("POST /login HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n")
                .append("Content-Type: application/json\r\n")
                .append("Content-Length: ").append(json.length()).append("\r\n\r\n")
                .append(json);
        mServer.sendRequest(line.toString());
    }

    public Observable<RegistrationForm> getRegistrationForm() {
        return mRestAdapter.getRegistrationForm();
    }

    public void getRegistrationFormDirect() {
        StringBuilder line = new StringBuilder("GET /register HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Status> sendRegistrationForm(RegistrationForm form) {
        return mRestAdapter.sendRegistrationForm(form);
    }

    public void sendRegistrationFormDirect(RegistrationForm form) {
        String json = form.toJson();
        StringBuilder line = new StringBuilder("POST /register HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n")
                .append("Content-Type: application/json\r\n")
                .append("Content-Length: ").append(json.length()).append("\r\n\r\n")
                .append(json);
        mServer.sendRequest(line.toString());
    }

    /* Chat */
    // --------------------------------------------------------------------------------------------
    public Observable<Status> sendMessage(Message message) {
        return mRestAdapter.sendMessage(message);
    }

    public void sendMessageDirect(Message message) {
        String json = message.toJson();
        StringBuilder line = new StringBuilder("POST /message HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n")
                .append("Content-Type: application/json\r\n")
                .append("Content-Length: ").append(json.length()).append("\r\n\r\n")
                .append(json);
        mServer.sendRequest(line.toString());
    }

    /* Access */
    // --------------------------------------------------------------------------------------------
    public Observable<Status> logout(long id, String name) {
        return mRestAdapter.logout(id, name);
    }

    public void logoutDirect(long id, String name) {
        StringBuilder line = new StringBuilder("DELETE /logout?id=")
                .append(id).append("&name=").append(name).append("HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Status> switchChannel(long id, int channel, String name) {
        return mRestAdapter.switchChannel(id, channel, name);
    }

    public void switchChannelDirect(long id, int channel, String name) {
        StringBuilder line = new StringBuilder("PUT //switch_channel?id=")
                .append(id).append("&channel=").append(channel).append("&name=").append(name).append("HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }
}
