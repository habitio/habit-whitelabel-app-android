package com.muzzley.model.channels;

import java.io.Serializable;
import java.util.List;

/**
 * Created by caan on 01-10-2015.
 */
public class Device implements Serializable {
//    public String remoteId;
    public String profileId;
    public String channelId;
    public String componentId;
    public String photoUrl;
    public String label;
    public boolean checked;
    public List<String> classes ;
    public List<String> propertyClasses ;

    public Device(){
    }
    public Device(String channelId, String profileId, String componentId, String photoUrl, String label, boolean checked) {

        this.channelId = channelId;
        this.profileId = profileId;
        this.componentId = componentId;
        this.photoUrl = photoUrl;
        this.label = label;
        this.checked = checked;
    }

    @Override
    public boolean equals(Object o) {
        Device d = (Device) o;
        return channelId.equals(d.channelId) && profileId.equals(d.profileId) && componentId.equals(d.componentId);
    }

    @Override
    public int hashCode() {
        return channelId.hashCode() * profileId.hashCode() * componentId.hashCode();
    }
}
