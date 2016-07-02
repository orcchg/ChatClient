package com.orcchg.chatclient.ui.chat;

import android.widget.PopupMenu;

import com.orcchg.chatclient.ui.base.MvpView;

public interface ChatMvpView extends MvpView {
    String getMessage();
    PopupMenu getPopupMenu();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);
    void dropTitleUpdates();

    void onDedicatedMessagePrepare(String title);
    void onUnauthorizedError();
}
