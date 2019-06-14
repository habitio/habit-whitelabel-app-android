package com.muzzley.model.productDetails;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class Stores {

    private String id;
    private String name;
    private String url;
    private String logo;
    private String price;
    private DeliversIn deliversIn;
    private Physical physical;
    private Boolean highlighted=false;

    public int idPosition;
    private DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm:ss.SSS");
    private String startTime="", finishTime="";


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The name
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @param name
     * The name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     * The url
     */
    public String getUrl() {
        return url;
    }

    /**
     *
     * @param url
     * The url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     *
     * @return
     * The logo
     */
    public String getLogo() {
        return logo;
    }

    /**
     *
     * @param logo
     * The logo
     */
    public void setLogo(String logo) {
        this.logo = logo;
    }

    /**
     *
     * @return
     * The price
     */
    public String getPrice() {
        return price;
    }

    /**
     *
     * @param price
     * The price
     */
    public void setPrice(String price) {
        this.price = price;
    }

    /**
     *
     * @return
     * The deliversIn
     */
    public DeliversIn getDeliversIn() {
        return deliversIn;
    }

    /**
     *
     * @param deliversIn
     * The deliversIn
     */
    public void setDeliversIn(DeliversIn deliversIn) {
        this.deliversIn = deliversIn;
    }

    /**
     *
     * @return
     * The physical
     */
    public Physical getPhysical() {
        return physical;
    }

    /**
     *
     * @param physical
     * The physical
     */
    public void setPhysical(Physical physical) {
        this.physical = physical;
    }

    /**
     *
     * @return
     * The highlighted
     */
    public Boolean getHighlighted() {
        return highlighted;
    }

    /**
     *
     * @param highlighted
     * The highlighted
     */
    public void setHighlighted(Boolean highlighted) {
        this.highlighted = highlighted;
    }


    public int getIdPosition() {
        return idPosition;
    }

    public void setIdPosition(int idPosition) {
        this.idPosition = idPosition;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(String finishTime) {
        this.finishTime = finishTime;
    }


    public void resetTimers(){
        startTime = "";
        finishTime = "";
    }

    public void saveStartTime(){
        startTime = df.format(Calendar.getInstance().getTime());
    }

    public void saveFinishTime(){
        finishTime = df.format(Calendar.getInstance().getTime());
    }

    public String getDurationOnScreen(){
        return "TODO";
    }

    public void printTimes(){
        Log.e("LOG_TAG", "printTimes()");
        Log.e("LOG_TAG", "idPosition: " + idPosition);
        Log.e("LOG_TAG", "store name: " + name);
        Log.e("LOG_TAG", "started: " + startTime);
        Log.e("LOG_TAG", "finished: " + finishTime);
    }

    public void printTimesFromPause(){
        Log.e("LOG_TAG", "printTimesFromPause()");
        Log.e("LOG_TAG", "idPosition: " + idPosition);
        Log.e("LOG_TAG", "store name: " + name);
        Log.e("LOG_TAG", "pause: started: " + startTime);
        Log.e("LOG_TAG", "pause: finished: " + finishTime);
    }
}
