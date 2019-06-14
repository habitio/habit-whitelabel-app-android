package com.muzzley.app;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.tiles.Tile;
import com.muzzley.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by ruigoncalo on 01/10/15.
 */
public class InterfaceTileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<Tile> tiles;
    private OnItemClickListener listener;
    private int selectedPosition;
    private boolean isGrouped;

    public InterfaceTileAdapter(Context context, List<Tile> tiles, OnItemClickListener listener, boolean isGrouped){
        this.context = context;
        this.tiles = tiles;
        this.listener = listener;
        this.isGrouped = isGrouped;
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    public Tile getItem(int position) {
        return tiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_interface_devices, parent, false), listener);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final Tile tile = getItem(position);

        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.labelChannel.setText(tile.getLabel());
        Picasso.get()
                .load(tile.getPhotoUrl())
                .into(viewHolder.imageChannel);
        setHighlights(viewHolder, position);
    }

    public void setSelectedPosition(int position){
        selectedPosition = position;
        notifyDataSetChanged();
    }

    void setHighlights(final ViewHolder viewHolder, int position) {
        boolean selected = viewHolder.getLayoutPosition() == selectedPosition;
        viewHolder.indicator.setBackgroundResource(selected ? R.color.blue : R.color.gray_light);
        viewHolder.labelChannel.setTextColor(context.getResources().getColor(selected ? R.color.blue : R.color.tile_group_text));

        int size = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, selected ? 45 : 40, context.getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, selected ? 1 : 6, context.getResources().getDisplayMetrics());

        LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) viewHolder.imageChannel.getLayoutParams();
        if(isGrouped && position == 0) {
            viewHolder.imageChannel.setScaleType(ImageView.ScaleType.FIT_CENTER);
            layoutParams.width = (int)Utils.dpToPx(context, 60);
            layoutParams.height = (int)Utils.dpToPx(context, 60);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.topMargin = (int)Utils.dpToPx(context, 10);
            layoutParams.bottomMargin = (int)Utils.dpToPx(context, 3);
            LinearLayout.LayoutParams labelParams = (LinearLayout.LayoutParams) viewHolder.labelChannel.getLayoutParams();
            labelParams.topMargin = (int)Utils.dpToPx(context, 3);
            viewHolder.labelChannel.setLayoutParams(labelParams);
            viewHolder.itemGradient.setVisibility(View.INVISIBLE);
        } else {
            viewHolder.imageChannel.setScaleType(ImageView.ScaleType.FIT_XY);
            layoutParams.width = (int)Utils.dpToPx(context, 90);
            layoutParams.height = (int)Utils.dpToPx(context, 90);
            layoutParams.gravity = Gravity.LEFT;
            layoutParams.bottomMargin = 0;
            viewHolder.itemGradient.setVisibility(View.VISIBLE);
        }
        viewHolder.imageChannel.setLayoutParams(layoutParams);

        if (selected) {
            viewHolder.bounce();
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.image_control_interface_item) ImageView imageChannel;
        @BindView(R.id.label_control_interface_item) TextView labelChannel;
        @BindView(R.id.indicator) View indicator;
        @BindView(R.id.image_control_interface_item_gradient) View itemGradient;
        public OnItemClickListener listener;
        private final Animation animation;

        public ViewHolder(View view, OnItemClickListener listener) {
            super(view);
            ButterKnife.bind(this, view);
            this.listener = listener;
            view.setOnClickListener(this);
            animation = AnimationUtils.loadAnimation(context, R.anim.bounce);
        }

        public void bounce() {
            imageChannel.startAnimation(animation);
        }

        @Override
        public void onClick(View v) {
            setSelectedPosition(this.getLayoutPosition());
            if (listener != null) {
                listener.onTileClick(v, getItem(this.getLayoutPosition()), this.getLayoutPosition());
            }
        }
    }

    public interface OnItemClickListener {
        void onTileClick(View view, Tile tile, int position);
    }

}
