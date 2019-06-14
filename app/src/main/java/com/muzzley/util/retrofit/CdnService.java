package com.muzzley.util.retrofit;

import com.muzzley.model.units.UnitsTable;

import io.reactivex.Observable;
import retrofit2.http.GET;

/**
 * Created by caan on 22-06-2016.
 */

public interface CdnService {
    @GET(Endpoints.UNITS_TABLE)
    Observable<UnitsTable> getUnitsTable();

}
