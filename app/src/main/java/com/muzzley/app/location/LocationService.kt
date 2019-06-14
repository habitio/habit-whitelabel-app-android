package com.muzzley.app.location

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.getObjectExtra
import com.muzzley.util.notificationManager
import com.muzzley.util.retrofit.MuzzleyCoreService
import com.muzzley.util.rxerr
import com.muzzley.util.toJsonString
import timber.log.Timber
import javax.inject.Inject


class LocationService : Service() {

    @Inject lateinit var muzzleyCoreService: MuzzleyCoreService
    @Inject lateinit var preferencesRepository: PreferencesRepository

    override fun onCreate() {
        super.onCreate()
        Timber.d("onCreate")
        App.appComponent.inject(this)

    }
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.d("onCreate")
        if (intent != null) {
            Timber.d("non null intent")
            val notification = NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_PLATFORM)
                    .setContentTitle(getString(R.string.location_notification_title))
//                    .setTicker("Ticker")
                    .setContentText(getString(R.string.location_notification_text))
                    .setSmallIcon(R.drawable.notifications)
//                    .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
//                    .setContentIntent(pendingIntent)
                    .setOngoing(true)
                    .setAutoCancel(false)
//                    .addAction(android.R.drawable.ic_media_previous, "Previous", ppreviousIntent)
//                    .addAction(android.R.drawable.ic_media_play, "Play", pplayIntent)
//                    .addAction(android.R.drawable.ic_media_next, "Next", pnextIntent)
                    .build()

            if (Build.VERSION.SDK_INT >= 26) { //Build.VERSION_CODES.O
                val channel = NotificationChannel(
                        Constants.NOTIFICATION_CHANNEL_PLATFORM,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                )
//                channel.setDescription("channelDescription")
                notificationManager.createNotificationChannel(channel)
            }

            startForeground(Constants.NOTIFICATION_LOCATION_ID,notification)

//            val x = intent.getObjectExtra<Map<String,Map<String,Any?>>>("payload")
//            Timber.d("reparsed: ${x.toJsonString()}")
            val y = intent.getStringExtra("payload")
            Timber.d("preparsed: $y")
//            val x = UtilsExtensions.gson.fromJson(y,Map::class.java)
            val x = intent.getObjectExtra<Map<String, Loc>>("payload")
            Timber.d("reparsed string: ${x.toString()}")
            Timber.d("reparsed json: ${x.toJsonString()}")

            val disposable = muzzleyCoreService.sendProperty(
                    preferencesRepository.userChannelId,
                    "location",
                    intent.getStringExtra("property"),
                    x)
//                    intent.getSerializableExtra("payload"))
//                    .subscribe(LogCompletableObserver("location send"))
                    .subscribe(
                            {
                                Timber.d("Location sent")
                                notificationManager.cancel(Constants.NOTIFICATION_LOCATION_ID)
                                stopSelf()
                            },
                            rxerr("Could not send location") {
                                notificationManager.cancel(Constants.NOTIFICATION_LOCATION_ID)
                                stopSelf()
                            }
//                            {
//                                Timber.e(it,"Could not send location")
//                                notificationManager.cancel(Constants.NOTIFICATION_LOCATION_ID)
//                                stopSelf()
//                            }
                    )

        }
        return START_NOT_STICKY
    }
}