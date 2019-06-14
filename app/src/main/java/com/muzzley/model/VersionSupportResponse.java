package com.muzzley.model;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by kyryloryabin on 04/02/16.
 */
public class VersionSupportResponse {

    private String minimumVersion;
    private Date goodUntil;

    public String getMinimumVersion() {
        return minimumVersion;
    }

    public void setMinimumVersion(String minimumVersion) {
        this.minimumVersion = minimumVersion;
    }

    public Date getGoodUntil() {
        return goodUntil;
    }

    public void setGoodUntil(Date goodUntil) {
        this.goodUntil = goodUntil;
    }
}
