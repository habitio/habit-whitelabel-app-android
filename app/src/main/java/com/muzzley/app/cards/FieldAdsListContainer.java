package com.muzzley.app.cards;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import com.muzzley.R;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;

/**
 * Created by caan on 17-03-2016.
 */
public class FieldAdsListContainer extends LinearLayout implements Container<Field>{

    RecyclerView recyclerView;
    ContainerAdapter<Placeholder> adapter;

    public FieldAdsListContainer(Context context) {
        super(context);
    }

    public FieldAdsListContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldAdsListContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_ads);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(adapter = new ContainerAdapter<Placeholder>(getContext(), R.layout.adapter_item_field_ads_item,R.id.ads_item));
    }

    @Override
    public void setContainerData(Field data) {

        adapter.setData(data.placeholder);
    }
}
