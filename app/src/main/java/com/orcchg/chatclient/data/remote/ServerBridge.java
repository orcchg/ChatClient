package com.orcchg.chatclient.data.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerBridge {

    public static final String IP_ADDRESS = "194.190.63.108";
    public static final int PORT = 80;
    private static final int BUFFER_SIZE = 4096;

    private WorkerThread mWorker;

    public interface ConnectionCallback {
        void onComplete();
        void onNext(Response response);
        void onError(Throwable e);
    }

    private ConnectionCallback mCallback;

    public void setConnectionCallback(ConnectionCallback callback) {
        mCallback = callback;
    }

    public void startConnection() {
        mWorker = new WorkerThread(mCallback);
        mWorker.start();
    }

    public void closeConnection() {
        mWorker.terminate();
    }

    /* Internals */
    // --------------------------------------------------------------------------------------------
    private static class WorkerThread extends Thread {
        private Socket mSocket;
        private BufferedReader mInput;
        private boolean mIsStopped;
        private final ConnectionCallback mCallback;

        WorkerThread(ConnectionCallback callback) {
            mCallback = callback;
        }

        @Override
        public void run() {
            try {
                mSocket = new Socket(IP_ADDRESS, PORT);
                mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                char[] buffer = new char[BUFFER_SIZE];
                while (!mIsStopped && mInput.read(buffer) >= 0) {
                    Response response = Response.obtain(buffer);
                    if (mCallback != null) mCallback.onNext(response);
                }
                mInput.close();
                mSocket.close();
                if (mCallback != null) mCallback.onComplete();
            } catch (IOException e) {
                if (mCallback != null) mCallback.onError(e);
            }
        }

        private void terminate() {
            mIsStopped = true;
        }
    }
}
