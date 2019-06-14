
package com.muzzley.model.cards;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Card {

    public String channel;
    @SerializedName("class")
    public String _class;
    public List<String> feedback = new ArrayList<String>();
    public String id;
    public Interaction interaction = new Interaction();
    public double relevance;
    public String title;
    public String type;
    public String user;
    public Colors colors = new Colors();
    public Date created;
    public Date updated;

    public transient boolean tracked; // hack for analytics

}
