package com.muzzley.services;

import android.content.Context;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
//import com.mixpanel.android.mpmetrics.MixpanelAPI;
import com.muzzley.R;
//import com.squareup.okhttp.OkHttpClient;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;

@Module()
public class ExternalServicesModule {

    @Provides
    OkHttpClient provideOkHttpClient() {
        return new OkHttpClient.Builder().followRedirects(false).build();
    }
//    OkHttpClient provideOkHttpClient() {
//        return new OkHttpClient();
//    }

//    @Provides
//    @Singleton Picasso providePicasso(Context context) {
//        return Picasso.get();
//    }

    @Provides
    @Singleton Bus provideBus() {
        return new Bus();
    }

    @Provides
    @Singleton Gson provideGson() {
        return new GsonBuilder().serializeNulls().create();
    }

//    @Provides
//    @Singleton MixpanelAPI provideMixpanel(Context context) {
//        String mixpanelToken = context.getString(R.string.app_mixpanel_token);
//        return TextUtils.isEmpty(mixpanelToken) ? null : MixpanelAPI.getInstance(context, mixpanelToken);
//    }
}
