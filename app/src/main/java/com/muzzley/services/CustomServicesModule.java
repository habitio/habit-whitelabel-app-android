package com.muzzley.services;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.muzzley.BuildConfig;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.model.Preferences;
import com.muzzley.model.user.Authorization;
import com.muzzley.util.retrofit.AuthService;
import com.muzzley.util.retrofit.CdnService;
import com.muzzley.util.retrofit.ChannelService;
import com.muzzley.util.retrofit.GmtDateTypeAdapter;
import com.muzzley.util.retrofit.MuzzleyApiService;
import com.muzzley.util.retrofit.MuzzleyCoreService;
import com.muzzley.util.retrofit.UserService;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Authenticator;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import timber.log.Timber;

import static android.content.Context.MODE_PRIVATE;


@Module()
public class CustomServicesModule {

    @Provides
    @Singleton
    PreferencesRepository providePreferencesRepository(Context context,SharedPreferences sharedPreferences) {
//        return new PreferencesRepositoryImpl(context,sharedPreferences,
//                new GsonBuilder().setDateFormat(Constants.DATE_FORMAT).create());
        return new PreferencesRepositoryImpl(context,sharedPreferences);
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences(Application application) {
        return application.getSharedPreferences("application-preferences", MODE_PRIVATE);
    }

//    @Provides
//    @Singleton
//    AnalyticsService provideAnalyticsService(MixPanelService analyticsService) {
//        return analyticsService;
//    }


//    @Provides
//    @Singleton
//    AnalyticsService provideAnalyticsService2(Context context, final MixPanelService analyticsService) {
//        final FirebaseAnalyticsService firebaseAnalyticsService = new FirebaseAnalyticsService(context);
//        return (AnalyticsService) Proxy.newProxyInstance(
//                AnalyticsService.class.getClassLoader(),
//                new Class[]{AnalyticsService.class},
//                new InvocationHandler() {
//                    @Override
//                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//                        try {
//                            method.invoke(analyticsService,args);
//                            method.invoke(firebaseAnalyticsService,args);
//                        } catch (Exception e) {
//                            Timber.e(e);
//                        }
//                        return null;
//                    }
//                }
//        );
////        return analyticsService;
//    }

//    @Provides
//    @Singleton
//    AnalyticsService provideAnalyticsService3(Context context) {
//        return Analytics.INSTANCE.getAnalyticsService(
//                context,
//                context.getString(R.string.analytics_application_name),
////                BuildConfig.APPLICATION_NAME,
//                context.getString(R.string.app_mixpanel_token),
//                context.getString(R.string.neura_client_id),
//                context.getString(R.string.neura_client_secret)
//        );
//    }

//    @Provides
//    @Singleton
//    AnalyticsService provideAnalyticsService4() {
//        return Analytics.INSTANCE.getAnalyticsService();
//    }


//    @Provides
//    @Singleton
//    AnalyticsService provideAnalyticsService(Context context) {
//
//        return new FirebaseAnalyticsService(context);
//    }

    @Provides
    @Singleton
    Realtime provideRealtime(AnalyticsTracker analyticsTracker) {
        return new Mqtt2(analyticsTracker);
    }

    @Provides
    @Singleton
    UserService provideUserService(final Context context,
                                   final PreferencesRepository preferencesRepository,
                                   final Authenticator authenticator,
                                   @Named("authorization") Interceptor authorizationInterceptor) {

//        final String baseUrl = context.getString(R.string.api_base_url);
        final String baseUrl = preferencesRepository.getAuthorization().getEndpoints().getHttp() +"/v3/legacy/";
        Interceptor userInterceptor = new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {
                String oldUrl = chain.request().url().toString();
//                HttpUrl newUrl = oldUrl.newBuilder().addEncodedPathSegments("/users/" + preferencesRepository.getUser().getId()).build();
//                String newUrl = oldUrl.toString().replace(baseUrl, baseUrl + "users/" + preferencesRepository.getUser().getId()+"/");

//                String newUrl = oldUrl.equals(baseUrl)
//                        ? baseUrl + "users/" + preferencesRepository.getUser().getId()
//                        : oldUrl.replace(baseUrl, baseUrl + "users/" + preferencesRepository.getUser().getId()+"/");
                String userId = preferencesRepository.getAuthorization().getOwnerId();
                String newUrl = oldUrl.equals(baseUrl)
                        ? baseUrl + "users/" + userId
                        : oldUrl.replace("$user_id", userId);
//                Timber.d("oldUrl = " + oldUrl+", newUrl = "+newUrl);
                Request request = chain.request().newBuilder().url(newUrl).build();
                return chain.proceed(request);
            }
        };

        return buildAdapter(authenticator, userInterceptor,authorizationInterceptor)
                .baseUrl(baseUrl)
                .build()
                .create(UserService.class);
    }

    @Provides
    @Singleton
    MuzzleyApiService provideMuzzleyApiService(Context context) {
        return buildAdapter()
                .baseUrl(context.getString(R.string.site_url))
                .build()
                .create(MuzzleyApiService.class);
    }

    @Provides
    @Singleton
    CdnService provideCdnService(Context context) {
        return buildAdapter()
                .baseUrl(context.getString(R.string.cdn_url))
                .build()
                .create(CdnService.class);
    }

    @Provides
    @Singleton
    MuzzleyCoreService provideMuzzleyCoreService(PreferencesRepository preferencesRepository,
                                                 Authenticator authenticator,
                                                 @Named("authorization") Interceptor authorizationInterceptor) {
        return buildAdapter(authenticator,authorizationInterceptor)
//                .baseUrl(context.getString(R.string.core_url))
                .baseUrl(preferencesRepository.getAuthorization().getEndpoints().getHttp() +"/")
                .build()
                .create(MuzzleyCoreService.class);
    }


    @Provides
    @Singleton
    ChannelService provideChannelService(PreferencesRepository preferencesRepository,
                                         Authenticator authenticator,
                                         @Named("authorization") Interceptor authorizationInterceptor) {

        return buildAdapter(authenticator, authorizationInterceptor)
                .baseUrl(preferencesRepository.getAuthorization().getEndpoints().getHttp() +"/v3/legacy/")
                .build()
                .create(ChannelService.class);
    }

    @Provides
    @Singleton
    AuthService provideAuthService(Context context) {

        return buildAdapter()
                .baseUrl(context.getString(R.string.api_base_url)+"/v3/legacy/")
                .build()
                .create(AuthService.class);
    }

    @Provides
    @Singleton
    Authenticator provideAuthenticator(final AuthService authService, final PreferencesRepository preferencesRepository){
        return new Authenticator() {
            @Override
            public Request authenticate(Route route, Response response) throws IOException {
                try {
                    synchronized (preferencesRepository) {
                        String auth = response.request().header("Authorization");
                        if (auth.equals(getOAuthHeaderValue(preferencesRepository))) {
                            Authorization authorization = preferencesRepository.getAuthorization();
                            Authorization newAuthorization = authService.exchange(authorization.getClientId(), authorization.getRefreshToken()).blockingFirst();
                            preferencesRepository.setAuthorization(newAuthorization);
//                            try {
//                                Analytics.INSTANCE.logout(habitStatusCodes -> {
//                                    Timber.d("Error logging out SDK: "+habitStatusCodes);
//                                    return Unit.INSTANCE;
//                                });
//                            } catch (Exception e) {
//                                Timber.e(e,"Error logging out SDK");
//                            }
//                            try {
//                                Analytics.INSTANCE.setUser(UtilsKt.toJsonString(newAuthorization), habitStatusCodes -> {
//                                    Timber.d("Error logging in SDK: "+habitStatusCodes);
//                                    return Unit.INSTANCE;
//                                });
//                            } catch (Exception e) {
//                                Timber.e(e,"Error logging in SDK");
//                            }

                        } //TODO: else should we implement a max retry mechanism ?
                        Request req = response.request().newBuilder()
                                .removeHeader("Authorization")
                                .header("Authorization", getOAuthHeaderValue(preferencesRepository))
                                .build();
                        return req;
                    }
                } catch (Exception e) {
                    Timber.e(e, "Error refreshing token");
                    return null;
                }
            }
        };
    }

    @Named("io")
    @Provides
    Scheduler provideIOScheduler() {
        return Schedulers.io();
    }

    @Named("main")
    @Provides
    Scheduler provideMainScheduler() {
        return AndroidSchedulers.mainThread();
    }

    @Named("authorization")
    @Provides
    Interceptor getAuthorizationInterceptor(final PreferencesRepository preferencesRepository) {
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Headers.Builder headersBuilder = request.headers().newBuilder().add("User-agent", System.getProperty("http.agent", "NA"));
                Authorization authorization = preferencesRepository.getAuthorization();
                if (authorization != null && authorization.getAccessToken() != null) {
                    headersBuilder.add("Authorization", getOAuthHeaderValue(preferencesRepository));
                }
                Preferences preferences = preferencesRepository.getPreferences();
                if (preferences != null && preferences.getLocale() != null) {
                    headersBuilder.add("Accept-Language", preferences.getLocale().replace('_','-'));
                }
                Headers headers = headersBuilder.build();

                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            }
        };
    }

    Retrofit.Builder buildAdapter(Interceptor... interceptors){
        return buildAdapter(null, interceptors);
    }


    Retrofit.Builder buildAdapter(Authenticator authenticator, Interceptor... interceptors){

        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder()
                .followRedirects(false)
                .readTimeout(60, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS);

        if (authenticator != null) {
            okBuilder.authenticator(authenticator);
        }

        if (interceptors != null) {
            for (Interceptor interceptor : interceptors) {
                okBuilder.addInterceptor(interceptor);
            }
        }
        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(
                    new HttpLoggingInterceptor.Logger() {
                        @Override public void log(String message) {
                            Timber.tag("OkHttp").d(message);
                        }
                    }
            );
            httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            okBuilder.addInterceptor(httpLoggingInterceptor);
        }

        OkHttpClient okHttpClient = okBuilder.build();


        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new GmtDateTypeAdapter())
//                .setDateFormat(Constants.DATE_FORMAT)
//                .registerTypeAdapterFactory(NotNullTypeAdapterFactory.INSTANCE)
                .create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io()));

        return builder;
    }

    static public String getOAuthHeaderValue(PreferencesRepository preferencesRepository){
        return "OAuth2.0 " + preferencesRepository.getAuthorization().getAccessToken();
    }

}
