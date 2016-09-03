package com.orcchg.chatclient.ui.chat;

import android.os.Bundle;
import android.support.annotation.StringRes;

import com.orcchg.chatclient.ui.base.MvpView;
import com.orcchg.chatclient.ui.chat.util.ChatStyle;

interface ChatMvpView extends MvpView {
    String getMessage();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);
    void showSnackbar(@StringRes int resId, int duration);
    void showSwitchChannelDialog(int channel);
    void showReconnectProgress(boolean isShow);
    void decorate(@ChatStyle.Style int style);

    void setTitleWithChannel(int channel, int peersOnChannel);

    void onDedicatedMessagePrepare(Bundle args);
    void onUnauthorizedError();
    void onForbiddenMessage(String message);

    boolean isPaused();
}
