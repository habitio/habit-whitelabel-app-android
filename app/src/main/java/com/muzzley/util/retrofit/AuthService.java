package com.muzzley.util.retrofit;

import com.google.gson.JsonObject;
import com.muzzley.model.Me;
import com.muzzley.model.login.ResetData;
import com.muzzley.model.user.Authorization;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import retrofit2.http.Url;

/**
 * Created by caan on 14-11-2017.
 */

public interface AuthService {

    @GET(Endpoints.AUTHORIZE)
    Observable<Authorization> authorize(@Query("client_id") String clientId, @Query("username") String userName, @Query("password") String password);

    @GET(Endpoints.EXCHANGE)
    Observable<Authorization> exchange(@Query("client_id") String clientId,  @Query("refresh_token") String refreshToken);

    @GET
    Observable<Map> genericGet(@Url String url, @QueryMap Map<String,String> queryParams);

    @POST(Endpoints.SIGN_IN)
    Observable<JsonObject> signin(@Body Me me);

    @POST(Endpoints.SIGN_UP)
    Observable<JsonObject> signup(@Body Me me);

    @POST(Endpoints.RESET_PASSWORD)
    Completable resetPassword(@Body ResetData resetData);


}
