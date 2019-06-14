package com.muzzley.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ruigoncalo on 14/05/14.
 */
public class Subscription implements Serializable {

    private String profile;
    private List<Channel> channels;

    public Subscription(String profile, List<Channel> channels) {
        this.profile = profile;
        this.channels = channels;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

    public static class Channel {
        private String id;
        private String content;
        private String activity;

        public Channel(String id, String content, String activity) {
            this.id = id;
            this.content = content;
            this.activity = activity;
        }

        public String getId() {
            return id;
        }

        public String getContent() {
            return content;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getActivity() {
            return activity;
        }

        public void setActivity(String activity) {
            this.activity = activity;
        }
    }
}
