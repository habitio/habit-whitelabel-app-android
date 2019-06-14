package com.muzzley.app.cards;

import com.muzzley.model.cards.Filter;
import com.muzzley.model.channels.Device;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by caan on 02-10-2015.
 */
public class DeviceEventRequest implements Serializable{

    public List<Device> devices;
    public List<Filter> filter;
    public String requestId;

    public DeviceEventRequest(List<Device> devices, List<Filter> filter, String requestId){

        this.devices = devices;
        this.filter = filter;
        this.requestId = requestId;
    }
}

