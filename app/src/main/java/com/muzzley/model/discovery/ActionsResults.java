package com.muzzley.model.discovery;

import java.util.List;

/**
 * Created by ruigoncalo on 17/07/14.
 */
public class ActionsResults {

    public final static String ACTION_RESULTS = "actionsResults";

    private List<Result> actionsResults;

    public ActionsResults(List<Result> actionsResults) {
        this.actionsResults = actionsResults;
    }

    public List<Result> getResults() {
        return actionsResults;
    }

    public void setResults(List<Result> actionsResults) {
        this.actionsResults = actionsResults;
    }
}
