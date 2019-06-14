package com.muzzley.model.discovery;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ruigoncalo on 08/07/14.
 */
public class Init implements Parcelable{

    public static final String CONTEXT = "context";
    public static final String USER_ID = "userId";
    public static final String TITLE = "title";
    public static final String DESCRIPTION = "description";
    public static final String IMAGE = "image";
    public static final String STEPS = "steps";
    public static final String CONNECTION_TYPE = "connectionType";
    public static final String NEXT_STEP_URL = "nextStepUrl";

    public static final String CONNECTION_TYPE_WIFI = "wifi";
    public static final String CONNECTION_TYPE_CELL = "cell";

    private String context;
    private String userId;
    private String title;
    private String description;
    private String image;
    private int steps;
    private String connectionType;

    @Override
    public String toString() {
        return "Init{" +
                "context='" + context + '\'' +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", steps=" + steps +
                ", connectionType='" + connectionType + '\'' +
                ", nextStepUrl='" + nextStepUrl + '\'' +
                '}';
    }

    private String nextStepUrl;

    public Init(String context, String userId, String title, String description, String image, int steps, String connectionType, String nextStepUrl) {
        this.context = context;
        this.userId = userId;
        this.title = title;
        this.description = description;
        this.image = image;
        this.steps = steps;
        this.connectionType = connectionType;
        this.nextStepUrl = nextStepUrl;
    }

    public Init(Parcel in){
        context = in.readString();
        userId = in.readString();
        title = in.readString();
        description = in.readString();
        image = in.readString();
        steps = in.readInt();
        connectionType = in.readString();
        nextStepUrl = in.readString();
    }

    public String getContext() {
        return context;
    }

    public String getUserId() {
        return userId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public int getSteps() {
        return steps;
    }

    public String getConnectionType() {
        return connectionType;
    }

    public String getNextStepUrl() {
        return nextStepUrl;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public void setNextStepUrl(String nextStepUrl) {
        this.nextStepUrl = nextStepUrl;
    }

    public int describeContents(){
        return 0;
    }

    public void writeToParcel(Parcel out, int flags){
        out.writeString(context);
        out.writeString(userId);
        out.writeString(title);
        out.writeString(description);
        out.writeString(image);
        out.writeInt(steps);
        out.writeString(connectionType);
        out.writeString(nextStepUrl);
    }

    public static final Creator<Init> CREATOR = new Creator<Init>() {
        @Override public Init createFromParcel(Parcel source) {
            return new Init(source);
        }

        @Override public Init[] newArray(int size) {
            return new Init[size];
        }
    };
}
