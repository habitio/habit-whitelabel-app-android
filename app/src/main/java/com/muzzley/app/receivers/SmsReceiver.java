package com.muzzley.app.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;

import com.google.gson.Gson;
import com.muzzley.App;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.retrofit.MuzzleyCoreService;
import com.muzzley.util.rx.LogCompletableObserver;

import java.util.HashMap;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * Created by bruno.marques on 26/12/2015.
 */
public class SmsReceiver extends BroadcastReceiver {

    @Inject protected PreferencesRepository preferencesRepository;
    @Inject MuzzleyCoreService muzzleyCoreService;

    private static final String SMS_RECEIVED = "android.provider.Telephony.SMS_RECEIVED";
    public static final String SMS_EXTRA_NAME ="pdus";

    @Override
    public void onReceive( Context context, Intent intent ) {
        App.appComponent.inject(this);

        if (preferencesRepository.getUserChannelId() != null) {
            Bundle extras = intent.getExtras();
            if ( extras != null ) {
                // Get received SMS array
                Object[] smsExtra = (Object[]) extras.get( SMS_EXTRA_NAME );

                for ( int i = 0; i < smsExtra.length; ++i ) {
                    SmsMessage sms = SmsMessage.createFromPdu((byte[]) smsExtra[i]);

                    String address = sms.getOriginatingAddress();
                    Timber.e("SmsReceiver from: " + address);
                    onReceiveSMS(context, address);

                    //String body = sms.getMessageBody().toString();
                    //Timber.e("SmsReceiver body: " + body);
                }
            }

            // WARNING!!!
            // If you uncomment the next line then received SMS will not be put to incoming.
            // Be careful!
            // this.abortBroadcast();
        }
    }


    public void onReceiveSMS(Context context, String phoneNumber) {

        HashMap<Object, Object> data = new HashMap<>();
        data.put("sender", phoneNumber);
        HashMap<Object, Object> payload = new HashMap<>();
        payload.put("data", data);
        muzzleyCoreService.sendProperty(preferencesRepository.getUserChannelId(), "phone", "incoming-sms", payload)
                .subscribe(new LogCompletableObserver("send sms info"));
    }
}
