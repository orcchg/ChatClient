package com.orcchg.chatclient.data.remote;

import android.support.annotation.Nullable;
import android.util.Log;
import android.util.MalformedJsonException;

import com.orcchg.chatclient.data.parser.Response;
import com.orcchg.chatclient.util.NetworkUtility;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.text.ParseException;
import java.util.Arrays;

import timber.log.Timber;

public class ServerBridge {

    public static final String IP_ADDRESS = "194.190.63.108";
    public static final int PORT = 9000;
    private static final int BUFFER_SIZE = 1024;

    private static boolean sNeedReconnect = false;
    private static @NetworkUtility.ConnectionError String sLastNetworkError;

    private WorkerThread mWorker;

    public interface ConnectionCallback {
        void onSuccess();
        void onTerminate();
        void onNext(Response response);
        void onError(Throwable e);
        void onReconnect();
    }

    private interface InternalCallback {
        void onConnectionReset();
        void onThreadStopped();
    }

    private ConnectionCallback mCallback;
    private InternalCallback mInternalCallback;

    public ServerBridge() {
        sNeedReconnect = false;
        mInternalCallback = new InternalCallback() {
            @Override
            public void onConnectionReset() {
                Timber.w("Connection reset");
                closeConnection();
            }

            @Override
            public void onThreadStopped() {
                mWorker = null;
            }
        };
    }

    @Nullable
    @NetworkUtility.ConnectionError
    public static String getLastNetworkError() {
        return sLastNetworkError;
    }

    public void setConnectionCallback(ConnectionCallback callback) {
        if (callback != null) {
            Timber.d("setConnectionCallback");
        } else {
            Timber.d("setConnectionCallback: dropped");
        }
        mCallback = callback;
        if (mWorker != null) {
            mWorker.setConnectionCallback(mCallback);
        } else {
            Timber.w("Worker thread is null");
        }
    }

    public void openConnection() {
        Timber.d("openConnection");
        if (mWorker == null) {
            mWorker = new WorkerThread(mCallback, mInternalCallback);
            mWorker.start();
        } else {
            Timber.w("Worker thread is running, connection is alive");
            if (mCallback != null) mCallback.onSuccess();
        }
    }

    public void closeConnection() {
        Timber.d("closeConnection");
        if (mWorker != null) {
            try {
                mWorker.terminate();
            } catch (IOException e) {
                Timber.e("Error during connection termination: %s", Log.getStackTraceString(e));
                sLastNetworkError = NetworkUtility.getNetworkError(e);
                // detach thread and let if finish silently
                mWorker.interrupt();
                mWorker = null;
            }
        } else {
            Timber.w("Worker thread is null");
        }
    }

    public void lostDirectConnection() {
        Timber.d("lostDirectConnection");
        sNeedReconnect = true;
    }

    public boolean isLoggingOut() {
        if (mWorker != null) {
            return mWorker.isLoggingOut();
        } else {
            Timber.w("Worker thread is null");
        }
        return false;
    }

    public void setLoggingOut(boolean flag) {
        Timber.d("setLoggingOut: %s", Boolean.valueOf(flag).toString());
        if (mWorker != null) {
            mWorker.setLoggingOut(flag);
        } else {
            Timber.w("Worker thread is null");
        }
    }

    public void sendRequest(String request) {
        Timber.d("sendRequest: %s", request);
        if (mWorker != null) {
            mWorker.sendRequest(request);
        } else {
            Timber.w("Worker thread is null");
        }
    }

    /* Internals */
    // --------------------------------------------------------------------------------------------
    private static class WorkerThread extends Thread {
        private Socket mSocket;
        private BufferedReader mInput;
        private boolean mIsStopped;
        private boolean mIsLoggingOut;
        private ConnectionCallback mCallback;
        private InternalCallback mInternalCallback;

        WorkerThread(ConnectionCallback callback, InternalCallback internalCallback) {
            mCallback = callback;
            mInternalCallback = internalCallback;
        }

        @Override
        public void run() {
            Timber.d("Server thread has started");
            try {
                mSocket = new Socket(IP_ADDRESS, PORT);
                mInput = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                char[] buffer = new char[BUFFER_SIZE];
                Arrays.fill(buffer, (char) 0);
                Timber.i("Connection has been established !");
                if (mCallback != null) {
                    if (sNeedReconnect) {
                        sNeedReconnect = false;
                        mCallback.onReconnect();
                    } else {
                        mCallback.onSuccess();
                    }
                }
                while (!mIsStopped && !mSocket.isClosed() && mInput.read(buffer) >= 0) {
                    diagnostic();
                    try {
                        Response response = Response.parse(buffer);
                        Timber.v("Parsed response:\n\n%s", response.toString());
                        Arrays.fill(buffer, (char) 0);  // clean-up
                        if (mCallback != null) mCallback.onNext(response);
                    } catch (ParseException e) {
                        Timber.e("Parse error: %s", Log.getStackTraceString(e));
//                        if (mCallback != null) mCallback.onError(e);
//                        if (mInternalCallback != null) mInternalCallback.onConnectionReset();
                    } catch (MalformedJsonException e) {
                        Timber.e("Response has malformed json body: %s", Log.getStackTraceString(e));
//                        if (mCallback != null) mCallback.onError(e);
//                        if (mInternalCallback != null) mInternalCallback.onConnectionReset();
                    }
                }
                Timber.d("Thread is stopping");
                terminate();
            } catch (ConnectException e) {
                Timber.e("%s", e.getMessage());
                Timber.w("%s", Log.getStackTraceString(e));
                sLastNetworkError = NetworkUtility.getNetworkError(e);
                if (mCallback != null) mCallback.onError(e);
                if (mInternalCallback != null) mInternalCallback.onConnectionReset();
            } catch (IOException e) {
                Timber.e("Connection error: %s", Log.getStackTraceString(e));
                sLastNetworkError = NetworkUtility.getNetworkError(e);
                if (!mIsLoggingOut && mCallback != null) mCallback.onError(e);
                if (mInternalCallback != null) mInternalCallback.onConnectionReset();
            }

            diagnostic();
            if (mInternalCallback != null) mInternalCallback.onThreadStopped();
        }

        private void diagnostic() {
            Timber.v("Is logging out: %s", Boolean.toString(mIsLoggingOut));
            if (mCallback == null) {
                Timber.w("Callback is null !!!");
            }
        }

        private void terminate() throws IOException {
            Timber.i("Terminate call");
            if (!mIsStopped) {
                mIsStopped = true;
                if (mSocket != null) mSocket.close();
                if (mCallback != null) mCallback.onTerminate();
                Timber.d("Terminated successfully");
            } else {
                Timber.d("Already terminated");
            }
        }

        private boolean isLoggingOut() { return mIsLoggingOut; }

        private void setLoggingOut(boolean flag) { mIsLoggingOut = flag; }

        private void setConnectionCallback(ConnectionCallback callback) {
            mCallback = callback;
        }

        public void sendRequest(String request) {
            try {
                OutputStream output = mSocket.getOutputStream();
                output.write(request.getBytes());
            } catch (IOException e) {
                sLastNetworkError = NetworkUtility.getNetworkError(e);
                if (mCallback != null) mCallback.onError(e);
            } catch (NullPointerException e) {
                Timber.e("Socket is null - ignore request: %s", Log.getStackTraceString(e));
                sLastNetworkError = NetworkUtility.getNetworkError(e);
            }
        }
    }
}
