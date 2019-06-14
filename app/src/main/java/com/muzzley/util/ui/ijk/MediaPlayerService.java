package com.muzzley.util.ui.ijk;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.muzzley.R;

import timber.log.Timber;
import tv.danmaku.ijk.media.player.IMediaPlayer;

public class MediaPlayerService extends Service {
    private static IMediaPlayer sMediaPlayer;
    static Context context;

    public static Intent newIntent(Context context) {
        MediaPlayerService.context = context.getApplicationContext();
        Intent intent = new Intent(context, MediaPlayerService.class);
        return intent;
    }

    public static void intentToStart(Context context) {
        context.startService(newIntent(context));
    }

    public static void intentToStop(Context context) {
        context.stopService(newIntent(context));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Timber.d("SSS onBind");
        return null;
    }


    @Override
    public void onCreate() {
        Timber.d("SSS onCreate");
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Timber.d("SSS onStartCommand");

        if (intent != null && intent.getBooleanExtra("shutdown",false)) {
            Timber.d("stopping self");
            stopSelf(startId);
        }
        return START_NOT_STICKY;

    }

    @Override
    public void onDestroy() {
        Timber.d("SSS onDestroy called");
        if (sMediaPlayer != null) {
            if (sMediaPlayer.isPlaying()) {
                sMediaPlayer.stop();
                Timber.d("SSS was playing");
            }
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        NotificationManagerCompat.from(getApplicationContext()).cancel(1337);
        super.onDestroy();
    }

    public static void setMediaPlayer(IMediaPlayer mp) {
        Timber.d("SSS setMediaPlayer");
        if (sMediaPlayer != null && sMediaPlayer != mp) {
            if (sMediaPlayer.isPlaying())
                sMediaPlayer.stop();
            sMediaPlayer.release();
            sMediaPlayer = null;
        }
        sMediaPlayer = mp;
        if (context != null && mp != null) {
            String channelId = "media";
            Notification notification = new NotificationCompat.Builder(context,channelId)
//                    .setSmallIcon(R.drawable.ic_notification_logo)
                    .setSmallIcon(R.drawable.notifications)
                    .setContentTitle("Background audio")
                    .setOngoing(true)
                    .addAction(0,"Cancel",PendingIntent.getService(context, 33, new Intent(context, MediaPlayerService.class).putExtra("shutdown", true), PendingIntent.FLAG_ONE_SHOT))
                    .setAutoCancel(false)
                    .build();

            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= 26 ) { //Build.VERSION_CODES.O
                NotificationChannel channel = new NotificationChannel(channelId, context.getString(R.string.app_name), NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(1337, notification);
        }
    }

    public static IMediaPlayer getMediaPlayer() {
        return sMediaPlayer;
    }
}
