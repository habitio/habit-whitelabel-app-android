package com.muzzley.util.ui.triStateToggle;

/**
 * Defines an extension for views that assume one of three states.
 *
 * Created by ruigoncalo on 11/09/15.
 */
public interface Statable {

    int STATE_UNDEF = -1;
    int STATE_OFF = 0;
    int STATE_ON = 1;

    int getState();

    void setState(int state);

    /**
     * goes from one state to another
     */
    void toggle();
}
