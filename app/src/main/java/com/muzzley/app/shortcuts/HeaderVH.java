package com.muzzley.app.shortcuts;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.muzzley.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by muzzley on 17-11-2015.
 */
public class HeaderVH extends RecyclerView.ViewHolder {

    @BindView(R.id.title)
    TextView title;

    public HeaderVH(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }
}
