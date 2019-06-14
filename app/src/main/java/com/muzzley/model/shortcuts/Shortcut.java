package com.muzzley.model.shortcuts;

import android.os.Parcel;
import android.os.Parcelable;

import com.muzzley.model.workers.WorkerExecuteMessage;
import com.muzzley.model.workers.WorkerUnit;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by muzzley on 20-11-2015.
 */
public class Shortcut implements Parcelable {

    private String id;
    private String label;
    private String color;
    private boolean showInWatch;
//    private List<Rule> actions;
    private List<WorkerUnit> actions;
    private List<WorkerExecuteMessage> execute;
    private String origin;
    private transient boolean isExecuting;

    public Shortcut() {

    }

    public Shortcut(Parcel parcel) {
        this.id = parcel.readString();
        this.label = parcel.readString();
        this.color = parcel.readString();
        this.showInWatch = parcel.readByte() != 0;
        this.actions = new ArrayList<>();
        parcel.readList(this.actions, WorkerUnit.class.getClassLoader());
        this.execute = new ArrayList<>();
        parcel.readList(this.execute, WorkerExecuteMessage.class.getClassLoader());
    }


    public Shortcut(String label, boolean showInWatch, List<WorkerUnit> actions) {
        this.label = label;
        this.showInWatch = showInWatch;
        this.actions = actions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(label);
        dest.writeString(color);
        dest.writeByte((byte)(showInWatch ? 1 : 0));
        dest.writeList(actions);
        dest.writeList(execute);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public boolean isShowInWatch() {
        return showInWatch;
    }

    public void setShowInWatch(boolean showInWatch) {
        this.showInWatch = showInWatch;
    }

    public List<WorkerUnit> getActions() {
        return actions;
    }

//    public void setActions(List<Rule> actions) {
//        this.actions = actions;
//    }

    public void setExecute(List<WorkerExecuteMessage> execute) {
        this.execute = execute;
    }

    public List<WorkerExecuteMessage> getExecute() {
        return execute;
    }

    public boolean isExecuting() {
        return isExecuting;
    }

    public void setIsExecuting(boolean isExecuting) {
        this.isExecuting = isExecuting;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOrigin() {
        return origin;
    }

    public static final Creator<Shortcut> CREATOR = new Creator<Shortcut>() {

        @Override
        public Shortcut createFromParcel(Parcel source) {
            return new Shortcut(source);
        }

        @Override
        public Shortcut[] newArray(int size) {
            return new Shortcut[size];
        }
    };

}
