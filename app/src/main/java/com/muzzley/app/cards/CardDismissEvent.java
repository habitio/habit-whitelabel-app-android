package com.muzzley.app.cards;

/**
 * Created by caan on 05-10-2015.
 */
public class CardDismissEvent {
    public String id;
    public boolean refreshAfter;

    public CardDismissEvent(String id) {
        this.id = id;
    }
    public CardDismissEvent(String id, boolean refreshAfter) {
        this.id = id;
        this.refreshAfter = refreshAfter;
    }
}
