package com.muzzley.model.tiles;

import com.muzzley.model.Preferences;
import com.muzzley.model.channels.Channel;

import java.util.ArrayList;

/**
 * Created by caan on 01-12-2015.
 */
public class TileWidgetOptions {
    public ArrayList<Channel> channels = new ArrayList<>();
    public ArrayList<Tile> tiles = new ArrayList<>();
    public Preferences preferences;
    public ArrayList<String> capabilities = new ArrayList<>();
    public String apiVersion = "v3";
}
