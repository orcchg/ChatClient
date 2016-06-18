package com.orcchg.chatclient.ui.main;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;
import com.orcchg.chatclient.ui.authorization.LoginActivity;
import com.orcchg.chatclient.ui.base.BasePresenter;
import com.orcchg.chatclient.ui.chat.ChatActivity;
import com.orcchg.chatclient.util.SharedUtility;

public class MainPresenter extends BasePresenter<MainMvpView> {

    boolean isLoggedIn() {
        Activity activity = (Activity) getMvpView();
        Resources resources = activity.getResources();
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        boolean isLoggedIn = sp.getBoolean(resources.getString(R.string.shared_prefs_is_logged_id), false);
        return isLoggedIn;
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
        Resources resources = activity.getResources();
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        long id = sp.getLong(resources.getString(R.string.shared_prefs_user_id_key), Status.UNKNOWN_ID);
        String userName = sp.getString(resources.getString(R.string.shared_prefs_user_login_key), null);

        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(ChatActivity.EXTRA_USER_ID, id);
        intent.putExtra(ChatActivity.EXTRA_USER_NAME, userName);
        activity.startActivity(intent);
        activity.finish();
    }
}
