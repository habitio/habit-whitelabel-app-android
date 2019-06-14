package com.muzzley.app.shortcuts;

import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.muzzley.R;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by muzzley on 20-11-2015.
 */
public class AddButtonVH extends RecyclerView.ViewHolder {

    @BindView(R.id.bg_shadow)
    View shadow;

    @BindView(R.id.bg_shortcut)
    ImageView image;

    @BindView(R.id.btn_execute)
    ImageButton buttonAction;

    @BindView(R.id.shortcut_title)
    TextView title;

    @BindView(R.id.icon_show_on_watch)
    ImageView iconShowOnWatch;

    @BindView(R.id.btn_edit)
    ImageButton buttonEdit;

    @BindView(R.id.btn_remove)
    ImageButton buttonRemove;

    @BindView(R.id.progress)
    ProgressBar progressBar;

    @BindView(R.id.divider)
    View divider;

    public AddButtonVH(View itemView, View.OnClickListener listener) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        image.setImageResource(R.drawable.background_shortcut_add);
        title.setText(R.string.mobile_shortcut_new);
        buttonAction.setImageResource(R.drawable.ic_new);
        buttonAction.setOnClickListener(listener);

        shadow.setVisibility(View.INVISIBLE);
        iconShowOnWatch.setVisibility(View.INVISIBLE);
        buttonEdit.setVisibility(View.INVISIBLE);
        buttonRemove.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.INVISIBLE);
    }

}
