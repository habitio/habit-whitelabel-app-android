package com.muzzley.model.discovery;

/**
 * Created by ruigoncalo on 15/07/14.
 */
public class Result {

    public static final String ID = "id";
    public static final String RESULT = "result";

    private String id;
    private String result;

    public Result(String id, String result) {
        this.id = id;
        this.result = result;
    }

    public String getId() {
        return id;
    }

    public String getResult() {
        return result;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
