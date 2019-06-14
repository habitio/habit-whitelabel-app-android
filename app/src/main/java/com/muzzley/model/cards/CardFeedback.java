package com.muzzley.model.cards;

import java.util.List;

public class CardFeedback {

    public String id;
    public String feedback;
    public List<Field> fields ;
    public Action triggeredAction;
    public Action clickedAction;

    volatile public boolean refreshAfter;
}