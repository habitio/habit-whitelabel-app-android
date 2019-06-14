package com.muzzley.model.workers;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Created by ruigoncalo on 12/08/15.
 */
public class WorkerExecuteMessage implements Parcelable {

    public static final String NAMESPACE = "iot";

    private String io;
    private String component;
    private String property;
    private String profile;
    private String channel;
    private JsonElement data;

    public WorkerExecuteMessage(Parcel parcel) {
        this.io = parcel.readString();
        this.component = parcel.readString();
        this.property = parcel.readString();
        this.profile = parcel.readString();
        this.channel = parcel.readString();
        this.data = new Gson().fromJson(parcel.readString(), JsonElement.class);
    }

    public WorkerExecuteMessage(String io, String component, String property, String profile, String channel, JsonElement data) {
        this.io = io;
        this.component = component;
        this.property = property;
        this.profile = profile;
        this.channel = channel;
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(io);
        dest.writeString(component);
        dest.writeString(property);
        dest.writeString(profile);
        dest.writeString(channel);
        dest.writeString(data.toString());
    }

    public String getIo() {
        return io;
    }

    public String getComponent() {
        return component;
    }

    public String getProperty() {
        return property;
    }

    public String getProfile() {
        return profile;
    }

    public String getChannel() {
        return channel;
    }

    public JsonElement getData() {
        return data;
    }

    public void setIo(String io) {
        this.io = io;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setData(JsonElement data) {
        this.data = data;
    }

    public static final Creator<WorkerExecuteMessage> CREATOR = new Creator<WorkerExecuteMessage>() {
        @Override
        public WorkerExecuteMessage createFromParcel(Parcel source) {
            return new WorkerExecuteMessage(source);
        }

        @Override
        public WorkerExecuteMessage[] newArray(int size) {
            return new WorkerExecuteMessage[size];
        }
    };

}
