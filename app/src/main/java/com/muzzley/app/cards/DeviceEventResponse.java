package com.muzzley.app.cards;

import com.muzzley.model.channels.Device;

import java.io.Serializable;
import java.util.List;

/**
 * Created by caan on 02-10-2015.
 */
public class DeviceEventResponse implements Serializable {

    public List<Device> devices;
    public String requestId;

    public DeviceEventResponse(List<Device> devices,String requestId){

        this.devices = devices;
        this.requestId = requestId;
    }
}
