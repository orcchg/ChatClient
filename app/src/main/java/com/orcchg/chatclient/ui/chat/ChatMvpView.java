package com.orcchg.chatclient.ui.chat;

import android.widget.PopupMenu;

import com.orcchg.chatclient.ui.base.MvpView;

public interface ChatMvpView extends MvpView {
    String getMessage();

    void scrollListTo(int position);
    void showSnackbar(String message, int duration);
    PopupMenu getPopupMenu();

    void onUnauthorizedError();
}
