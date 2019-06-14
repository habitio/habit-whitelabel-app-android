package com.muzzley.app.cards.productdetails;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.productDetails.Stores;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsItemHolder extends RecyclerView.ViewHolder {

    public TextView delivery, location, price, buyNow, companyName;
    public ImageView storeLogo, imgHighlight;
    public RelativeLayout layoutAroudn;
    public View overlay;

    private Stores store;

    public ProductDetailsItemHolder(View itemView,final ProductDetailsAdapter.OnProductClickListener listener) {
        super(itemView);

        layoutAroudn = (RelativeLayout) itemView.findViewById(R.id.product_store_layout);
        storeLogo = (ImageView) itemView.findViewById(R.id.product_store_img_logo);
        imgHighlight = (ImageView) itemView.findViewById(R.id.img_highlight);
        companyName = (TextView) itemView.findViewById(R.id.product_company_name);
        delivery = (TextView) itemView.findViewById(R.id.product_delivery_in);
        location = (TextView) itemView.findViewById(R.id.product_locations_in);
        price = (TextView) itemView.findViewById(R.id.product_price);
        buyNow = (TextView) itemView.findViewById(R.id.product_buy_now);
        overlay = itemView.findViewById(R.id.overlay);

        overlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null)
                    listener.onProductClick(ProductDetailsItemHolder.this, store);
            }
        });
    }

    public void setLinkAddress(Stores store) {
        this.store = store;
    }
}
