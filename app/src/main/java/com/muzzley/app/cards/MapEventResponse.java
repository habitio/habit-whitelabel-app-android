package com.muzzley.app.cards;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by caan on 12-10-2015.
 */
public class MapEventResponse implements Parcelable{
    public LatLng target;
    public String uuid;

    public MapEventResponse(LatLng target, String uuid) {
        this.target = target;
        this.uuid = uuid;
    }

    protected MapEventResponse(Parcel in) {
        target = in.readParcelable(LatLng.class.getClassLoader());
        uuid = in.readString();
    }

    public static final Creator<MapEventResponse> CREATOR = new Creator<MapEventResponse>() {
        @Override
        public MapEventResponse createFromParcel(Parcel in) {
            return new MapEventResponse(in);
        }

        @Override
        public MapEventResponse[] newArray(int size) {
            return new MapEventResponse[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(target, i);
        parcel.writeString(uuid);
    }
}
