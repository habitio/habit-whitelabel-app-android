package com.muzzley.app.cards.productdetails;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.muzzley.R;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsItemEmptyHolder extends RecyclerView.ViewHolder {

    public RelativeLayout overLayout;

    public ProductDetailsItemEmptyHolder(Context context, View itemView, int emptyHeight) {
        super(itemView);
        overLayout = (RelativeLayout) itemView.findViewById(R.id.product_invisible);

        overLayout.setBackgroundColor(context.getResources().getColor(R.color.transparent));
        overLayout.getLayoutParams().height = emptyHeight;
    }
}
