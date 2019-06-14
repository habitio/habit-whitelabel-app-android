package com.muzzley.app.cards;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by caan on 12-10-2015.
 */
public class MapEventRequest implements Parcelable {
    public LatLng location;
    public String uuid;

    public MapEventRequest(LatLng location, String uuid) {

        this.location = location;
        this.uuid = uuid;
    }

    protected MapEventRequest(Parcel in) {
        location = in.readParcelable(LatLng.class.getClassLoader());
        uuid = in.readString();
    }

    public static final Creator<MapEventRequest> CREATOR = new Creator<MapEventRequest>() {
        @Override
        public MapEventRequest createFromParcel(Parcel in) {
            return new MapEventRequest(in);
        }

        @Override
        public MapEventRequest[] newArray(int size) {
            return new MapEventRequest[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeParcelable(location, i);
        parcel.writeString(uuid);
    }
}
