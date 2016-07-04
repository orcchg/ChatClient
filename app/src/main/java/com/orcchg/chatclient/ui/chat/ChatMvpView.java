package com.orcchg.chatclient.ui.chat;

import android.widget.PopupMenu;

import com.orcchg.chatclient.ui.base.MvpView;

public interface ChatMvpView extends MvpView {
    String getMessage();
    PopupMenu getPopupMenu();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);
    void showSwitchChannelDialog(int channel);

    void setTitleWithChannel(int channel, int peersOnChannel);

    void onDedicatedMessagePrepare(String title);
    void onUnauthorizedError();
}
