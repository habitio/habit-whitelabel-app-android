package com.muzzley.model.tiles;

import io.reactivex.subjects.BehaviorSubject;

public class Action {

    public String id;
    public String type;
    public String property;
    public String componentType;
    public String componentId;
    public Options_ options;

    transient public BehaviorSubject lastValue = BehaviorSubject.createDefault("");

}
