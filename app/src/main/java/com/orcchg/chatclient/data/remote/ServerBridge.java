package com.orcchg.chatclient.data.remote;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import timber.log.Timber;

public class ServerBridge {

    public static final String IP_ADDRESS = "194.190.63.108";
    public static final int PORT = 80;
    private static final int BUFFER_SIZE = 4096;

    private WorkerThread mWorker;

    public void startConnection() {
        mWorker = new WorkerThread();
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

        @Override
        public void run() {
            try {
                mSocket = new Socket(IP_ADDRESS, PORT);
                mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                char[] buffer = new char[BUFFER_SIZE];
                while (!mIsStopped && mInput.read(buffer) >= 0) {
                    Timber.v("Raw response: " + new String(buffer));
                }
                mInput.close();
                mSocket.close();
            } catch (IOException e) {
                Timber.e(e.getMessage());
            }
        }

        private void terminate() {
            mIsStopped = true;
        }
    }
}
