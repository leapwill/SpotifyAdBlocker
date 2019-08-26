package com.cameron.spotifyadblocker;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Set;

public class MediaChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Util util = Util.getInstance(context);
        CharSequence title = intent.getCharSequenceExtra("track");
        if (title != null) {
            util.checkSpotifyNotification(title.toString());
        }
    }
}
