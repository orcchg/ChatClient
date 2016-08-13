package com.orcchg.chatclient.util.crypting;

import android.app.Activity;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.util.SharedUtility;

import java.util.Arrays;

import timber.log.Timber;

public class SecurityUtility {

    private static String SERVER_PUBLIC_KEY;

    public static boolean isSecurityEnabled(Activity activity) {
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        String spKey = activity.getResources().getString(R.string.shared_prefs_security_enabled);
        return sp.getBoolean(spKey, false);
    }

    public static void storeServerPublicKey(Activity activity, String pem) {
        SERVER_PUBLIC_KEY = restoreStrippedInMemoryPEM(pem);
        SharedPreferences sp = SharedUtility.getSharedPrefs(activity);
        SharedPreferences.Editor editor = sp.edit();
        String spKey = activity.getResources().getString(R.string.shared_prefs_server_pubkey);
        editor.putString(spKey, SERVER_PUBLIC_KEY);
        editor.apply();
    }

    @Nullable
    public static String getServerPublicKey() {
        return SERVER_PUBLIC_KEY;
    }

    private static String restoreStrippedInMemoryPEM(String pem) {
        char[] buffer = new char[pem.length() + 80];
        Arrays.fill(buffer, '\0');

        int i1 = pem.indexOf("RSA", 5);
        if (i1 == -1) {
            Timber.e("Input string not in PEM format!");
            return pem;
        }
        int i2 = pem.indexOf("KEY", i1 + 3) + 8;
        pem.getChars(0, i2, buffer, 0);
        buffer[i2] = '\n';
        ++i2;

        int k = 1;
        int i3 = pem.indexOf("END", i2) - 5;
        while (i2 + 64 < i3) {
            pem.getChars(i2 - k, i2 - k + 64, buffer, i2);
            i2 += 64;
            buffer[i2] = '\n';
            ++i2;
            ++k;
        }
        if (i2 != i3) {
            int rest_length = i3 - i2 + k;
            pem.getChars(i2 - k, i2 - k + rest_length, buffer, i2);
            i2 += rest_length;
        }
        buffer[i2] = '\n';
        ++i2;
        pem.getChars(i3, pem.length(), buffer, i2);

        String answer = new String(buffer);
        Timber.d("%s", answer);
        return answer;
    }
}
