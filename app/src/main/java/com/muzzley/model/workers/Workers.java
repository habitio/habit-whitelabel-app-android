package com.muzzley.model.workers;

import java.util.List;

/**
 * Created by ruigoncalo on 11/12/14.
 */
public class Workers {

    private List<Worker> workers;

    public Workers(List<Worker> workers) {
        this.workers = workers;
    }

    public List<Worker> getWorkers() {
        return workers;
    }

    public void setWorkers(List<Worker> workers) {
        this.workers = workers;
    }
}
