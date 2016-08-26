package com.orcchg.chatclient.ui.base;

import com.orcchg.chatclient.util.NetworkUtility;

/**
 * Base interface that any class that wants to act as a View in the MVP (Model View Presenter)
 * pattern must implement. Generally this interface will be extended by a more specific interface
 * that then usually will be implemented by an Activity or Fragment.
 */
public interface MvpView {

    void onSuccess();
    void onComplete();
    void onError();
    void onLoading();
    void onNetworkError(@NetworkUtility.ConnectionError String error);

    void postOnUiThread(Runnable runnable);
}
