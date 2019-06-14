package com.muzzley.app.analytics;

import android.content.Context;

import java.util.Map;

public class TrackEvent {
    public TrackEvent(Context context) {

    }

    public TrackEvent addProperty(String prop, String obj) {
        return this;
    }
    public TrackEvent addProperty(String prop, int obj) {
        return this;
    }
    public TrackEvent addProperty(String prop, EventStatus status) {
        return this;
    }

    public Map<String, Object> getAsMap() {

        return null;
    }
}
