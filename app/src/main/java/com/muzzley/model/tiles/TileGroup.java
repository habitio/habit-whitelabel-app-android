
package com.muzzley.model.tiles;

import java.util.ArrayList;

public class TileGroup {

    public String id;
    public String label;
    public String parent;
    transient public ArrayList<Object> children = new ArrayList<>();

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return id.equals(((TileGroup) o).id);
    }
}
