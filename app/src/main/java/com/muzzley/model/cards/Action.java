
package com.muzzley.model.cards;

import com.google.gson.JsonElement;

public class Action {

    public Args args;
    public String id;
    public String label;
    public String type;
    public JsonElement value;
    public String icon;
    public String role = "secondary"; //for backward compatibility
    public Boolean notifyOnClick;
    public Boolean refreshAfter;
    public PubMqtt pubMQTT;

}
