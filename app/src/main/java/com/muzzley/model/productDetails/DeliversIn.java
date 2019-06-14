package com.muzzley.model.productDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class DeliversIn {

    private List<Integer> timeSpan = new ArrayList<Integer>();
    private String unit;

    /**
     *
     * @return
     * The timeSpan
     */
    public List<Integer> getTimeSpan() {
        return timeSpan;
    }

    /**
     *
     * @param timeSpan
     * The timeSpan
     */
    public void setTimeSpan(List<Integer> timeSpan) {
        this.timeSpan = timeSpan;
    }

    /**
     *
     * @return
     * The unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     *
     * @param unit
     * The unit
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }
}
