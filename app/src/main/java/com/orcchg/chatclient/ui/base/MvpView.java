package com.orcchg.chatclient.ui.base;

import com.orcchg.chatclient.util.NetworkUtility;

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */
public interface MvpView {

    void onSuccess();    // direct network connection has been established
    void onTerminate();  // direct network connection has been terminated
    void onLoading();    // request has been issued and progress has started
    void onComplete();   // response has been received and progress has finished
    void onError();      // error during request issuing / response receiving
    void onNetworkError(@NetworkUtility.ConnectionError String error);  // network error pair to error

    void postOnUiThread(Runnable runnable);
    void finishView();
}
