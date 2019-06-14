package com.muzzley.util.retrofit;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Path;
import io.reactivex.Observable;

/**
 * Created by ruigoncalo on 18/02/15.
 */
public interface MuzzleyApiService {

    @GET(Endpoints.INTERFACE_ARCHIVE)
    Observable<Response<ResponseBody>>  getInterfaceArchive(@Path("uuid") String uuid);
}
