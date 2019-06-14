package com.muzzley.util.retrofit;

import com.google.gson.JsonElement;

import io.reactivex.Completable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import io.reactivex.Observable;

/**
 * Created by kyryloryabin on 30/12/15.
 */
public interface MuzzleyCoreService {

    @PUT(Endpoints.CORE_PROPERTY)
    @Headers({"Content-type: application/json"})
    Completable sendProperty(@Path("channelId") String remoteChannelId,
                             @Path("componentId") String componentId,
                             @Path("propertyId") String property,
                             @Body Object payload);


    @PUT("{topic}")
    @Headers({"Content-type: application/json"})
    Completable sendProperty(@Path(value="topic", encoded = true) String topic, @Body JsonElement payload);


    @GET(Endpoints.CORE_PROPERTY)
    @Headers({"Content-type: application/json"})
    Observable<JsonElement> readProperty(@Path("channelId") String remoteChannelId,
                                     @Path("componentId") String componentId,
                                     @Path("propertyId") String property);


}
