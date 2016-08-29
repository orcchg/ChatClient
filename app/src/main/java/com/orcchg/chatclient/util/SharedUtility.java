package com.orcchg.chatclient.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.orcchg.chatclient.R;

import java.util.HashMap;
import java.util.Map;

public class SharedUtility {

    public static SharedPreferences getSharedPrefs(Activity activity) {
        String key = activity.getResources().getString(R.string.shared_prefs_file_key);
        return activity.getSharedPreferences(key, Context.MODE_PRIVATE);
    }

    public static void storePasswordHash(Activity activity, String hash) {
        Resources resources = activity.getResources();
        SharedPreferences.Editor editor = SharedUtility.getSharedPrefs(activity).edit();
        editor.putString(resources.getString(R.string.shared_prefs_user_password_key), hash);
        editor.apply();
    }

    public static String getPasswordHash(Activity activity) {
        Resources resources = activity.getResources();
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        return sp.getString(resources.getString(R.string.shared_prefs_user_password_key), null);
    }

    public static void logIn(Activity activity, long id, String userName, String userEmail) {
        Resources resources = activity.getResources();
        SharedPreferences.Editor editor = SharedUtility.getSharedPrefs(activity).edit();
        editor.putLong(resources.getString(R.string.shared_prefs_user_id_key), id);
        editor.putString(resources.getString(R.string.shared_prefs_user_login_key), userName);
        editor.putString(resources.getString(R.string.shared_prefs_user_email_key), userEmail);
        editor.apply();
    }

    public static void logOut(Activity activity) {
//        Resources resources = activity.getResources();
//        SharedPreferences.Editor editor = SharedUtility.getSharedPrefs(activity).edit();
//        editor.putLong(resources.getString(R.string.shared_prefs_user_id_key), Status.UNKNOWN_ID);
//        editor.putString(resources.getString(R.string.shared_prefs_user_login_key), null);
//        editor.putString(resources.getString(R.string.shared_prefs_user_email_key), null);
//        editor.apply();
    }

    public static Map<String, String> splitPayload(@Nullable String payload) {
        if (TextUtils.isEmpty(payload)) {
            return new HashMap<>();
        }

        String[] tokens = payload.split("&");
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < tokens.length; ++i) {
            String[] pair = tokens[i].split("=");
            if (pair.length > 1) {
                map.put(pair[0], pair[1]);
            }
        }
        return map;
    }
}
