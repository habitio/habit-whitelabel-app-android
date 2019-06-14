package com.muzzley.model;

public final class User {

    private String id;
    private String name;
    private String email;
    private String photoUrl;
    private String authToken;
//    private String authType;
//    private Boolean newUser;
//    private String clientId;
//
////    public Preferences preferences;
//
//    @Override
//    public String toString() {
//        return String.format("%s (authToken: %s) %s %s [%s] (%s)", id, authToken, name, email, photoUrl, authType);
//    }
//
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    @Deprecated
    public String getAuthToken() {
        return authToken;
    }
//
//    public String getAuthType() {
//        return authType;
//    }
//
//    public Boolean isNewUser() {
//        return newUser;
//    }
//
//    public void setId(String id) {
//        this.id = id;
//    }
//
//    public void setName(String name) {
//        this.name = name;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public void setPhotoUrl(String photoUrl) {
//        this.photoUrl = photoUrl;
//    }
//
//    public void setAuthToken(String authToken) {
//        this.authToken = authToken;
//    }
//
//    public void setAuthType(String authType) {
//        this.authType = authType;
//    }
//
//    public void setIsNewUser(boolean isNewUser) {
//        newUser = isNewUser;
//    }
//
//    public String getClientId() {
//        return clientId;
//    }
//
//    public void setClientId(String clientId) {
//        this.clientId = clientId;
//    }
}
