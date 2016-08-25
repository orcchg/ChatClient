package com.orcchg.chatclient.ui.chat;

import android.os.Bundle;

import com.orcchg.chatclient.ui.base.MvpView;

interface ChatMvpView extends MvpView {
    String getMessage();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);
    void showSwitchChannelDialog(int channel);

    void setTitleWithChannel(int channel, int peersOnChannel);

    void onDedicatedMessagePrepare(Bundle args);
    void onUnauthorizedError();
    void onForbiddenMessage(String message);

    boolean isPaused();
}
