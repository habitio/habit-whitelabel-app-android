
package com.muzzley.model.cards;
import java.util.ArrayList;
import java.util.List;

public class RangeStyle {

    public boolean bold;
    public boolean italic;
    public boolean underline;
    public String color;
    public double fontSize;
    public List<Integer> range = new ArrayList<Integer>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public RangeStyle() {
    }

}
