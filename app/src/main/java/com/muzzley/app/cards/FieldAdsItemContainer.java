package com.muzzley.app.cards;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.cards.productdetails.ProductDetailsActivity;
import com.muzzley.model.cards.Card;
import com.muzzley.model.cards.Placeholder;
import com.muzzley.model.productDetails.ProductDetails;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by caan on 17-03-2016.
 */
public class FieldAdsItemContainer extends LinearLayout implements Container<Placeholder> {

    @BindView(R.id.image) ImageView image;
    @BindView(R.id.label) TextView label;
    @BindView(R.id.price_range) TextView priceRange;

    public FieldAdsItemContainer(Context context) {
        super(context);
    }

    public FieldAdsItemContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldAdsItemContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this,this);
    }

    @Override
    public void setContainerData(final Placeholder data) {
        Picasso.get()
                .load(data.image)
//                .fit()
                .into(image);
        label.setText(data.label);
        priceRange.setText(data.priceRange);

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final CardContainer cc = CardContainer.from(FieldAdsItemContainer.this);
                Card card = cc.card;
                getContext().startActivity(new Intent(getContext(), ProductDetailsActivity.class)
                        .putExtra(Constants.PRODUCT_DETAIL,data.detailUrl)
                        .putExtra(ProductDetailsActivity.EXTRA_CARD_ID, card.id)
                );
            }
        });
    }
}
