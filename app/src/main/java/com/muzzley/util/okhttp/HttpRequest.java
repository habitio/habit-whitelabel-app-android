package com.muzzley.util.okhttp;

import com.google.gson.Gson;
import com.muzzley.model.User;
import com.muzzley.services.PreferencesRepository;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import timber.log.Timber;

//import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Created by ruigoncalo on 10/07/14.
 */

@Singleton
public class HttpRequest {

    public static final String BASIC_AUTH_HEADER_FIELD = "Authorization";
//    public static final String LOCATION_HEADER_FIELD = "location";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType XML = MediaType.parse("text/xml; charset=utf-8");

    @Inject OkHttpClient okHttpClient;
    @Inject Gson gson;

    private final PreferencesRepository preferencesRepository;

    @Inject
    public HttpRequest(PreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    public Observable<Response> getResponse(final String url) {
        return Observable.defer(new Callable<ObservableSource<? extends Response>>() {
            @Override
            public ObservableSource<? extends Response> call() throws Exception {
                try {
                    Request request = new Request.Builder()
                            .url(url)
                            .header(BASIC_AUTH_HEADER_FIELD, getBasicAuthValue())
                            .build();

                    return Observable.just(okHttpClient.newCall(request).execute());
                } catch (Throwable throwable) {
                    return Observable.error(throwable);
                }
            }
        });
    }

    public <T> Observable<T> getResponse(final String url, final Class<? extends T> clazz) {
        return getResponse(url).map(new Function<Response, T>() {
            @Override
            public T apply(Response response) {
                try {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Timber.d("zzz got response body : '" + body+"'");
                        return gson.fromJson(body, clazz);
                    } else {
                        throw new RuntimeException("Http status code: " + response.code());
                    }
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

            }
        });

    }

    public Observable<String> getResponseString(final String url) {
        return getResponse(url).map(new Function<Response, String>() {
            @Override
            public String apply(Response response) {
                try {
                    if (response.isSuccessful()) {
                        String body = response.body().string();
                        Timber.d("zzz got response body : '" + body+"'");
                        return body;
                    } else {
                        throw new RuntimeException("Http status code: " + response.code());
                    }
                } catch (Throwable t) {
                    throw new RuntimeException(t);
                }

            }
        });

    }
    public Observable<Response> doPostRequest(final String url, final Object obj) throws IOException {
        return Observable.defer(new Callable<ObservableSource<? extends Response>>() {
            @Override
            public ObservableSource<? extends Response> call() throws Exception {
                try {

                    RequestBody body = RequestBody.create(JSON, gson.toJsonTree(obj).toString());
                    Request request = new Request.Builder()
                            .url(url)
//                            .header(BASIC_AUTH_HEADER_FIELD, getBasicAuthValue()) //FIXME: this was not in original code !!!
                            .post(body)
                            .build();

                    return Observable.just(okHttpClient.newCall(request).execute());
                } catch (Throwable throwable) {
                    return Observable.error(throwable);
                }
            }
        });
    }

    public Observable<String> doXmlPostRequest(final String url, final String xml, final List<Map<String,String>> headers) {
        return Observable.defer(new Callable<ObservableSource<String>>() {
            @Override
            public ObservableSource<String> call() throws Exception {
                try {
                    RequestBody body = RequestBody.create(XML, xml);
                    Request.Builder builder = new Request.Builder();
                    builder.url(url).post(RequestBody.create(XML, xml));
                    for (Map<String, String> header : headers) {
                        String key = header.keySet().iterator().next();
                        builder.header(key, header.get(key));
                    }
                    return Observable.just(okHttpClient.newCall(builder.build()).execute().body().string());

                } catch (Throwable throwable) {
                    return Observable.error(throwable);
                }
            }
        });

    }

    synchronized private String getBasicAuthValue() {
        User user = preferencesRepository.getUser();
        String value = user.getId() + ":" + user.getAuthToken();
        Timber.d("Authorization value: " + value);
        return Credentials.basic(user.getId(), user.getAuthToken());
    }


}
