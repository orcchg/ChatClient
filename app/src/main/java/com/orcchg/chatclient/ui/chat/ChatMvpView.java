package com.orcchg.chatclient.ui.chat;

import com.orcchg.chatclient.ui.base.MvpView;

public interface ChatMvpView extends MvpView {
    String getMessage();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);

    void onUnauthorizedError();
}
