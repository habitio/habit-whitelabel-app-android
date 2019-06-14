package com.muzzley.model.discovery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruigoncalo on 09/07/14.
 */
public class Action implements Parcelable{

    public static final String ID = "id";
    public static final String TYPE = "type";
    public static final String PARAMS = "params";

    public static final String TYPE_UPNP = "upnp-discovery";
    public static final String TYPE_HTTP = "http";
    public static final String TYPE_NETWORK_INFO = "network-info";
    public static final String TYPE_UDP = "udp";
    public static final String TYPE_ACTIVATION_CODE = "activation-code";

    private String id;
    private String type;
    private Param params;

    public Action(String id, String type, Param params) {
        this.id = id;
        this.type = type;
        this.params = params;
    }

    public Action(Parcel in){
        id = in.readString();
        type = in.readString();
        params = in.readParcelable(Param.class.getClassLoader());
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public Param getParams() {
        return params;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setParams(Param params) {
        this.params = params;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeString(id);
        out.writeString(type);
        out.writeParcelable(params, flags);
    }

    public static final Creator<Action> CREATOR = new Creator<Action>() {
        @Override public Action createFromParcel(Parcel source) {
            return new Action(source);
        }

        @Override public Action[] newArray(int size) {
            return new Action[size];
        }
    };

    @Override
    public String toString(){
        StringBuilder builder = new StringBuilder();
        builder.append(ID + ":").append(id == null ? "null" : id).append("\n")
                .append(TYPE + ":").append(type == null ? "null" : type).append("\n")
                .append(PARAMS + ":").append(params.toString());
        return builder.toString();
    }
}
