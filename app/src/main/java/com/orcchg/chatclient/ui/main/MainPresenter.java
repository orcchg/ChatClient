package com.orcchg.chatclient.ui.main;

import android.app.Activity;
import android.content.Intent;

import com.orcchg.chatclient.ui.authorization.LoginActivity;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.chat.ChatActivity;

public class MainPresenter extends BasePresenter<MainMvpView> {

    boolean isLoggedIn() {
        return false;  // TODO: impl
    }

    /* Open activities */
    // --------------------------------------------------------------------------------------------
    void openLoginActivity() {
        Activity activity = (Activity) getMvpView();
        Intent intent = new Intent(activity, LoginActivity.class);
        activity.startActivity(intent);
        activity.finish();
    }

    void openChatActivity() {
        Activity activity = (Activity) getMvpView();
        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, id);
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, userName);
        activity.startActivity(intent);
        activity.finish();
    }
}
