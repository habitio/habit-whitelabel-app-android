package com.muzzley.model.productDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetails {

    private String id;
    private String title;
    private String description;
    private String image;
    private String icon;
    private String specs;
    private List<Stores> stores = new ArrayList<Stores>();

    /**
     *
     * @return
     * The id
     */
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     * The id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @return
     * The title
     */
    public String getTitle() {
        return title;
    }

    /**
     *
     * @param title
     * The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     *
     * @return
     * The description
     */
    public String getDescription() {
        return description;
    }

    /**
     *
     * @param description
     * The description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     *
     * @return
     * The image
     */
    public String getImage() {
        return image;
    }

    /**
     *
     * @param image
     * The image
     */
    public void setImage(String image) {
        this.image = image;
    }

    /**
     *
     * @return
     * The icon
     */
    public String getIcon() {
        return icon;
    }

    /**
     *
     * @param icon
     * The icon
     */
    public void setIcon(String icon) {
        this.icon = icon;
    }

    /**
     *
     * @return
     * The specs
     */
    public String getSpecs() {
        return specs;
    }

    /**
     *
     * @param specs
     * The specs
     */
    public void setSpecs(String specs) {
        this.specs = specs;
    }

    /**
     *
     * @return
     * The stores
     */
    public List<Stores> getStores() {
        return stores;
    }

    /**
     *
     * @param stores
     * The stores
     */
    public void setStores(List<Stores> stores) {
        this.stores = stores;
    }
}
