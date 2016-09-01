package com.orcchg.chatclient.ui.notification;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.orcchg.chatclient.R;
import com.orcchg.chatclient.data.viewobject.PeerVO;
import com.orcchg.chatclient.resources.ViewUtils;
import com.orcchg.chatclient.ui.chat.ChatActivity;
import com.orcchg.jgravatar.Gravatar;

public class NotificationMaster {
    public static final String EXTRA_OPEN_BY_NOTIFICATION = "extra_open_by_notification";

    public static void pushNotification(Activity activity, PeerVO peerVO, String message) {
        Gravatar gravatar = new Gravatar();
        String url = gravatar.getUrl(peerVO.getEmail());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity)
                .setAutoCancel(true)
                .setContentTitle(peerVO.getLogin())
                .setContentText(message)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setLights(0xffffd600, 750, 5000)
                .setSmallIcon(R.drawable.ic_chat_bubble_outline_white_18dp)
                .setWhen(System.currentTimeMillis());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(ViewUtils.getBitmapByUrl(activity, url, false));
        } else {
            builder.setLargeIcon(ViewUtils.getBitmapByUrl(activity, url, true))
                   .setColor(activity.getResources().getColor(R.color.colorControlActivated));
        }

        Intent intent = new Intent(activity, ChatActivity.class);
        intent.putExtra(EXTRA_OPEN_BY_NOTIFICATION, true);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, (int) peerVO.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) peerVO.getId(), builder.build());
    }
}
