package com.muzzley.model.profiles;

import java.util.List;

/**
 * Created by caan on 05-01-2018.
 */

public class ChannelTemplates {

    public List<Element> elements;

    public static class Element {
        public ChannelTemplate template;
        public String recipe_id;
        public String overlay;
    }

    public static class ChannelTemplate {
        public String id,name,icon,image,recipe;
        public Boolean additionable;
        public DiscoveryRequiredCapability required_capability;
        public TemplateType type;
    }
}
