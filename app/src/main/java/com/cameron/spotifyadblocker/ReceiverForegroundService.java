package com.cameron.spotifyadblocker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

public class ReceiverForegroundService extends Service {

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;

    @Override
    public void onCreate() {
        // Create broadcast receiver
        broadcastReceiver = new MediaChangeReceiver();
        filter = new IntentFilter();
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        // from Spotify docs at https://developer.spotify.com/documentation/android/guides/android-media-notifications/
        filter.addAction("com.spotify.music.playbackstatechanged");
        filter.addAction("com.spotify.music.metadatachanged");

        // Create persistent notification
        Intent intent = new Intent(this, ReceiverForegroundService.class)
                .setAction(getString(R.string.INTENT_ACTION_DISABLE));
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getString(R.string.service_notification_channel))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Running in background")
                .setContentText("Tap to stop")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setOngoing(true);

        startForeground(R.integer.service_notification_id, builder.build());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && getString(R.string.INTENT_ACTION_DISABLE).equals(intent.getAction())) {
            Util.getInstance(this).disableBlocking();
            stopForeground(true);
        } else {
            this.registerReceiver(broadcastReceiver, filter);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        this.unregisterReceiver(broadcastReceiver);
    }

    @androidx.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
