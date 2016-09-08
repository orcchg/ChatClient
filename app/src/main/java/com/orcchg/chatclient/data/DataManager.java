package com.orcchg.chatclient.data;

import com.orcchg.chatclient.data.model.LoginForm;
import com.orcchg.chatclient.data.model.Message;
import com.orcchg.chatclient.data.model.RegistrationForm;
import com.orcchg.chatclient.data.remote.ServerBridge;

public class DataManager {

    private ServerBridge mServer;

    public DataManager(ServerBridge server) {
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

    public void lostDirectConnection() {
        mServer.lostDirectConnection();
    }

    public boolean isLoggingOut() {
        return mServer.isLoggingOut();
    }

    /* Authentication */
    // --------------------------------------------------------------------------------------------
    public void getLoginFormDirect() {
        StringBuilder line = new StringBuilder("GET /login HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.setLoggingOut(false);
        mServer.sendRequest(line.toString());
    }

    public void sendLoginFormDirect(LoginForm form) {
        String json = form.toJson();
        StringBuilder line = new StringBuilder("POST /login HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n")
                .append("Content-Type: application/json\r\n")
                .append("Content-Length: ").append(json.length()).append("\r\n\r\n")
                .append(json);
        mServer.setLoggingOut(false);
        mServer.sendRequest(line.toString());
    }

    public void getRegistrationFormDirect() {
        StringBuilder line = new StringBuilder("GET /register HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.setLoggingOut(false);
        mServer.sendRequest(line.toString());
    }

    public void sendRegistrationFormDirect(RegistrationForm form) {
        String json = form.toJson();
        StringBuilder line = new StringBuilder("POST /register HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n")
                .append("Content-Type: application/json\r\n")
                .append("Content-Length: ").append(json.length()).append("\r\n\r\n")
                .append(json);
        mServer.setLoggingOut(false);
        mServer.sendRequest(line.toString());
    }

    // ------------------------------------------
    public void checkAuthDirect(LoginForm form) {
        authDirect("/check_auth", form);
    }

    public void kickByAuthDirect(LoginForm form) {
        authDirect("/kick_by_auth", form);
    }

    private void authDirect(String path, LoginForm form) {
        String login = form.getLogin();
        String password = form.getPassword();
        boolean encrypted = form.isEncrypted();
        StringBuilder line = new StringBuilder("GET " + path + "?login=")
                .append(login).append("&password=").append(password).append("&encrypted=").append(encrypted ? '1' : '0')
                .append(" HTTP/1.1\r\n\"")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.setLoggingOut(false);
        mServer.sendRequest(line.toString());
    }

    /* Chat */
    // --------------------------------------------------------------------------------------------
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
    public void logoutDirect(long id) {
        StringBuilder line = new StringBuilder("DELETE /logout?id=")
                .append(id).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.setLoggingOut(true);
        mServer.sendRequest(line.toString());
    }

    public void logoutOnAllDevicesDirect(long id) {
        logoutDirect(id);
    }

    public void switchChannelDirect(long id, int channel) {
        StringBuilder line = new StringBuilder("PUT /switch_channel?id=")
                .append(id).append("&channel=").append(channel).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    /* Checking */
    // --------------------------------------------------------------------------------------------
    public void isLoggedInDirect(String name) {
        StringBuilder line = new StringBuilder("GET /is_logged_in?login=")
                .append(name).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public void isRegisteredDirect(String name) {
        StringBuilder line = new StringBuilder("GET /is_registered?login=")
                .append(name).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public void getAllPeersDirect() {
        StringBuilder line = new StringBuilder("GET /all_peers HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }

    public void getAllPeersDirect(int channel) {
        StringBuilder line = new StringBuilder("GET /all_peers?channel=")
                .append(channel).append(" HTTP/1.1\r\n")
                .append("Host: ").append(ServerBridge.IP_ADDRESS).append(':').append(ServerBridge.PORT).append("\r\n\r\n");
        mServer.sendRequest(line.toString());
    }
}
