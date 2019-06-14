package com.muzzley.util.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.Html
import com.muzzley.R
import com.muzzley.util.fromHtml
import com.muzzley.util.rx.RxDialogs
import timber.log.Timber

class Dialogs {

    companion object {
        @JvmStatic
        fun unblockNotifications(context: Context ) {
            RxDialogs.confirm(context,null,context.getString(R.string.mobile_android_notifications_permission_html).fromHtml(),
                    context.getString(R.string.mobile_go_settings),context.getString(R.string.mobile_not_now))
                    .subscribe(
                            {
                                if (it) {
                                    if (android.os.Build.VERSION.SDK_INT >= 21 ) { //Build.VERSION_CODES.LOLLIPOP

                                        //FIXME: relies on private keys, might break in future android versions
                                        context.startActivity(Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                                                .putExtra("app_package", context.getPackageName())
                                                .putExtra("app_uid", context.getApplicationInfo().uid)
                                        );
                                    } else if (android.os.Build.VERSION.SDK_INT == 19) { //Build.VERSION_CODES.KITKAT
                                        context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                                .addCategory(Intent.CATEGORY_DEFAULT)
                                                .setData(Uri.parse("package:" + context.getPackageName()))
                                        );
                                    }
                                }
                            },
                            { Timber.e(it,"Error enabling notifications")}
                    )

        }

    }

}