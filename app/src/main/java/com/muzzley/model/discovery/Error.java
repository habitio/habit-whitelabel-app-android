package com.muzzley.model.discovery;

/**
 * Created by ruigoncalo on 18/07/14.
 */
public class Error {

    public static final String STATUS_CODE = "statusCode";
    public static final String ERROR = "error";
    public static final String MESSAGE = "message";

    private int statusCode;
    private String error;
    private String message;

    public Error(int statusCode, String error, String message) {
        this.statusCode = statusCode;
        this.error = error;
        this.message = message;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setError(String error) {
        this.error = error;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
