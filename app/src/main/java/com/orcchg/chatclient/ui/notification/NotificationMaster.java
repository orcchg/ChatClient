package com.orcchg.chatclient.ui.notification;

import android.app.Activity;
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

    public static void pushNotification(Activity activity, PeerVO peerVO, String message) {
        Gravatar gravatar = new Gravatar();
        String url = gravatar.getUrl(peerVO.getEmail());

        NotificationCompat.Builder builder = new NotificationCompat.Builder(activity)
                .setSmallIcon(R.drawable.ic_chat_bubble_outline_white_18dp)
                .setContentTitle(peerVO.getLogin())
                .setContentText(message);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            builder.setLargeIcon(ViewUtils.getBitmapByUrl(activity, url, false));
        } else {
            builder.setLargeIcon(ViewUtils.getBitmapByUrl(activity, url, true))
                   .setColor(activity.getResources().getColor(R.color.colorControlActivated));
        }

        Intent intent = new Intent(activity, ChatActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, (int) peerVO.getId(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify((int) peerVO.getId(), builder.build());
    }
}
