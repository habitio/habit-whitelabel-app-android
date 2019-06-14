
package com.muzzley.model.cards;

import java.util.List;

public class Field {

    public String id;
    public String label;
    public List<Placeholder> placeholder ;
    public List<Placeholder> value ; //Hack, because return is not the same name
    public List<Filter> filter ;
//    public List<Placeholder> placeholder = new ArrayList<Placeholder>();

//    @SerializedName("filter")
//    @Expose
//    public List<String> filter = new ArrayList<String>();
    public String type;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Field() {
    }


}
