package com.muzzley.app.cards.productdetails;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.muzzley.R;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsTitleHolder extends RecyclerView.ViewHolder {
    public TextView title, description;

    public ProductDetailsTitleHolder(View itemView) {
        super(itemView);
        title = (TextView) itemView.findViewById(R.id.product_item_title);
        description = (TextView) itemView.findViewById(R.id.product_item_descritpion);
    }
}
