package com.muzzley.app.cards.productdetails;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.muzzley.R;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsDescriptionHolder extends RecyclerView.ViewHolder {
    public TextView description;

    public ProductDetailsDescriptionHolder(View itemView) {
        super(itemView);
        description = (TextView) itemView.findViewById(R.id.product_description_text);
    }
}
