
package com.muzzley.model.channels;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

public class Property {

    public String id;
    public String label;
    public String agentLabel;
    public JsonElement schemaExtension;
    public boolean isTriggerable;
    public boolean isActionable;
    public boolean isStateful;
    public boolean onChange;
    public int rateLimit;
    public List<WorkerParams> states;
    public List<WorkerParams> actions;
    public List<WorkerParams> triggers;
    public List<ControlInterface> controlInterfaces = new ArrayList<ControlInterface>();
    public List<String> classes = new ArrayList<String>();
    public List<String> requiredCapabilities = new ArrayList<String>();
    public List<String> components = new ArrayList<String>();
    public String io;
    public String schema;
    public UnitsOptions unitsOptions;

}
