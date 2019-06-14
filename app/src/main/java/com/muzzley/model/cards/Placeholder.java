
package com.muzzley.model.cards;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class Placeholder {

    public String label;
    public String component;
    public String profileId;
    @Deprecated
    public String remoteId; //FIXME: should be renamed to channelId
    public Double latitude;
    public Double longitude;
    public String time;
//    public List<Boolean> weekDays = new ArrayList<Boolean>();
    public List<Boolean> weekDays ;
    public String text ; //FIXME: check with backend
    public List<String> classes ;
//    public String value;
    public JsonElement value;
    public Boolean selected;
    public String image;
    public String priceRange;
    public String detailUrl;
    public Boolean highlighted;

                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                    }
