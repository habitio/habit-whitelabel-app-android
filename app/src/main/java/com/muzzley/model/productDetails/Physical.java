package com.muzzley.model.productDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class Physical {

    private Double nearest;
    private List<Location> locations = new ArrayList<Location>();

    /**
     *
     * @return
     * The nearest
     */
    public Double getNearest() {
        return nearest;
    }

    /**
     *
     * @param nearest
     * The nearest
     */
    public void setNearest(Double nearest) {
        this.nearest = nearest;
    }

    /**
     *
     * @return
     * The locations
     */
    public List<Location> getLocations() {
        return locations;
    }

    /**
     *
     * @param locations
     * The locations
     */
    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

}
