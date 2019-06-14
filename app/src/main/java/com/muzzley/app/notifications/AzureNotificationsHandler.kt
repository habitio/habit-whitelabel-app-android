package com.muzzley.app.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.HomeActivity
import com.muzzley.services.PreferencesRepository
import timber.log.Timber
import javax.inject.Inject

class AzureNotificationsHandler : BroadcastReceiver() {

     val NOTIFICATION_ID = 987123;

    @Inject lateinit var preferencesRepository: PreferencesRepository

    init {
//        if (preferencesRepository == null)
            App.appComponent.inject(this);
    }

//     override fun onReceive(p0: Context?, p1: Intent?) {
//         TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
//     }
    override
    fun onReceive(context: Context , intent: Intent) {
        Timber.d("Got azure notification")

        val bundle = intent.extras

        if (listOf("title","msg").any { bundle.getString(it).isNullOrBlank() }) {
            Timber.d("Notification payload missing field. Probably Neura event")
            return
        }

        if (preferencesRepository.push == true && preferencesRepository.user != null) {

            val notificationManager =  context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "platform"
            val builder = NotificationCompat.Builder(context,channelId)
//                    .setSmallIcon(R.drawable.ic_notification_logo)
//                    .setSmallIcon(R.drawable.ic_launcher)
                    .setSmallIcon(R.drawable.notifications)
                    .setContentTitle(bundle.getString("title"))
                    .setContentText(bundle.getString("msg"))
                    .setAutoCancel(true)
                    .setContentIntent(PendingIntent.getActivity(context, 0,
                        Intent(context, HomeActivity::class.java)
                                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                                .putExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS,Constants.Frag.timeline),
                        PendingIntent.FLAG_ONE_SHOT)
                    )

            Timber.d("built notification")
            val sound = bundle.getString("sound")
            if (sound != null) {
                builder.setSound(
                        if (sound == "default")
                            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                        else
                            Uri.parse("android.resource://" + context.getPackageName() + "/raw/"+sound)
                )
            }

            if (Build.VERSION.SDK_INT >= 26) { //Build.VERSION_CODES.O
                val channel = NotificationChannel(channelId, context.getString(R.string.app_name), NotificationManagerCompat.IMPORTANCE_DEFAULT);
//                channel.setDescription("channelDescription")
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(NOTIFICATION_ID, builder.build());
            Timber.d("sent notification")
        }
    }
}