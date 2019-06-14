package com.muzzley.providers;

/**
 * Created by ruigoncalo on 13/05/14.
 */
public final class BusProvider {
    private static final MainThreadBus bus = new MainThreadBus();

    public static MainThreadBus getInstance(){
        return bus;
    }

    private BusProvider() {

    }
}

