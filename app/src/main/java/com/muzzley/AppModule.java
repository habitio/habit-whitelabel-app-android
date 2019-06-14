package com.muzzley;

import android.app.Application;
import android.content.Context;

import com.muzzley.app.NavigatorImpl;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module()
public class AppModule {
    private final App application;

    public AppModule(App app) {
        this.application = app;
    }

    @Provides
    @Singleton Context provideContext() {
        return application;
    }

    @Provides
    @Singleton Navigator provideNavigator() {
        return new NavigatorImpl(application);
    }

    @Provides
    @Singleton Application provideApplication() {
        return application;
    }
}
