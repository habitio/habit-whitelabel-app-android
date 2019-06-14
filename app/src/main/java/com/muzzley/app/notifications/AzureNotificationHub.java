package com.muzzley.app.notifications;

import android.content.Context;

import com.microsoft.windowsazure.messaging.NotificationHub;
import com.muzzley.R;

import timber.log.Timber;

public class AzureNotificationHub extends NotificationHub {
//    public AzureNotificationHub(Context context) {
//        super(context.getResources().getString(R.string.azure_notification_hub_path),
//                context.getResources().getString(R.string.azure_connection_string),
//                context);
//        Timber.d("hubPath: " + context.getResources().getString(R.string.azure_notification_hub_path)
//                + ", connString: " + context.getResources().getString(R.string.azure_connection_string));
//    }

    public AzureNotificationHub(String notificationHubPath, String connectionString, Context context) {
        super(notificationHubPath,connectionString, context);
        Timber.d("hubPath: " + notificationHubPath
                + ", connString: " + connectionString);
    }


}
