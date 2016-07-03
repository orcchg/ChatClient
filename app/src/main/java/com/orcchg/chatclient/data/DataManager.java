package com.orcchg.chatclient.data;

import com.orcchg.chatclient.data.model.Check;
import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.Peers;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.data.remote.RestAdapter;
import com.orcchg.chatclient.data.remote.ServerBridge;

import rx.Observable;

import static android.R.attr.name;

public class DataManager {

    private RestAdapter mRestAdapter;
    private ServerBridge mServer;

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
    public Observable<Status> logout(long id) {
        return mRestAdapter.logout(id);
    }

    public void logoutDirect(long id) {
        StringBuilder line = new StringBuilder("DELETE /logout?id=")
                .append(id).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Status> switchChannel(long id, int channel) {
        return mRestAdapter.switchChannel(id, channel);
    }

    public void switchChannelDirect(long id, int channel) {
        StringBuilder line = new StringBuilder("PUT /switch_channel?id=")
                .append(id).append("&channel=").append(channel).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    /* Checking */
    // --------------------------------------------------------------------------------------------
    public Observable<Check> isLoggedIn(String name) {
        return mRestAdapter.isLoggedIn(name);
    }

    public void isLoggedInDirect(String name) {
        StringBuilder line = new StringBuilder("GET /is_logged_in?login=")
                .append(name).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Check> isRegistered(String name) {
        return mRestAdapter.isRegistered(name);
    }

    public void isRegisteredDirect(String name) {
        StringBuilder line = new StringBuilder("GET /is_registered?login=")
                .append(name).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Peers> getAllPeers() {
        return mRestAdapter.getAllPeers();
    }

    public void getAllPeersDirect() {
        StringBuilder line = new StringBuilder("GET /all_peers HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public Observable<Peers> getAllPeers(int channel) {
        return mRestAdapter.getAllPeers(channel);
    }

    public void getAllPeersDirect(int channel) {
        StringBuilder line = new StringBuilder("GET /all_peers?channel=")
                .append(channel).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }
}
