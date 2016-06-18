package com.orcchg.chatclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.model.Status;

public class SharedUtility {

    public static SharedPreferences getSharedPrefs(Activity activity) {
        String key = activity.getResources().getString(R.string.shared_prefs_file_key);
        return activity.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public static void logIn(Activity activity, long id, String userName) {
        Resources resources = activity.getResources();
        SharedPreferences.Editor editor = SharedUtility.getSharedPrefs(activity).edit();
        editor.putBoolean(resources.getString(R.string.shared_prefs_is_logged_id), true);
        editor.putLong(resources.getString(R.string.shared_prefs_user_id_key), id);
        editor.putString(resources.getString(R.string.shared_prefs_user_login_key), userName);
        editor.apply();
    }

    public static void logOut(Activity activity) {
        Resources resources = activity.getResources();
        SharedPreferences.Editor editor = SharedUtility.getSharedPrefs(activity).edit();
        editor.putBoolean(resources.getString(R.string.shared_prefs_is_logged_id), false);
        editor.putLong(resources.getString(R.string.shared_prefs_user_id_key), Status.UNKNOWN_ID);
        editor.putString(resources.getString(R.string.shared_prefs_user_login_key), null);
        editor.apply();
    }
}
