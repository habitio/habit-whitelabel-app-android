
package com.muzzley.model.channels;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Channel {

    public String id;
    @Deprecated public String remoteId; //used by legacy webviews
    public String content;
    public String profileId;
    public String profileName;
    public String photoUrl;
    public String backgroundPhotoUrl;
    public String tilePhotoUrl;
    public String activity;
    public boolean authorized;
    public List<String> categories = new ArrayList<String>();
    public Object place;
    public List<Component> components = new ArrayList<Component>();
    @SerializedName("properties")
    public List<Property> _properties = new ArrayList<Property>();
    @SerializedName("interface")
    public Interface _interface;
    public boolean isActionable;
    public boolean isTriggerable;
    public boolean isStateful;
    public boolean subscribed;

}
