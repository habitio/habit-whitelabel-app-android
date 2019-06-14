package com.muzzley.model.cards;

import java.util.ArrayList;
import java.util.List;

public class ContentStyles {

    public List<Double> margin = new ArrayList<Double>();

    /**
     * No args constructor for use in serialization
     *
     */
    public ContentStyles() {
    }

    /**
     *
     * @param margin
     */
    public ContentStyles(List<Double> margin) {
        this.margin = margin;
    }

}