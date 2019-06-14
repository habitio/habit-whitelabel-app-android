package com.muzzley.app.shortcuts;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.shortcuts.Shortcut;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by muzzley on 23-11-2015.
 */
public class ShortcutsVH extends RecyclerView.ViewHolder {

    private Handler handler = new Handler();

    @BindView(R.id.container_actions)
    ViewGroup containerShortcuts;

    @BindView(R.id.btn_show_more)
    Button btnShowMore;

    private ShortcutActionsListener mListener;

    private List<Shortcut> mShortcuts;

    public ShortcutsVH(View itemView) {
        super(itemView);

        ButterKnife.bind(this, itemView);
    }

    public void setShortcuts(List<Shortcut> shortcuts, ShortcutActionsListener listener) {
        mListener = listener;
        mShortcuts = shortcuts;
        inflate();
    }

    public void inflateAtPosition(Shortcut shortcut, int position) {
        containerShortcuts.removeViewAt(position);
        containerShortcuts.addView(inflateShortcut(containerShortcuts.getContext(), shortcut, position), position);
    }

    private void inflate() {
        final Context context = containerShortcuts.getContext();
        containerShortcuts.removeAllViews();
        if(mShortcuts != null) {
            for (int i = 0; i < Math.min(mShortcuts.size(), 4); i++) {
                final Shortcut shortcut = mShortcuts.get(i);
                containerShortcuts.addView(inflateShortcut(context, shortcut, i));
            }

            for (int i = mShortcuts.size(); i < 4; i++) {
                containerShortcuts.addView(inflateEmptyShortcut(context));
            }

            btnShowMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mListener != null) {
                        mListener.onShowMore(ShortcutsVH.this);
                    }
                }
            });
        }
    }

    private View inflateShortcut(Context ctx, final Shortcut shortcut,
                                 final int index) {
        View shortcutView = LayoutInflater.from(ctx)
                .inflate(R.layout.shortcut_vertical, containerShortcuts, false);

        TextView shortcutTitle = (TextView) shortcutView.findViewById(R.id.shortcut_title);
        shortcutTitle.setText(shortcut.getLabel());

        ImageView shortcutBg = (ImageView) shortcutView.findViewById(R.id.bg_shortcut);
        shortcutBg.setColorFilter(Color.parseColor(shortcut.getColor()));

        ImageButton btnShortcut = (ImageButton) shortcutView.findViewById(R.id.btn_shortcut);
        btnShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onExecuteShortcut(shortcut, ShortcutsVH.this, index);
                }
            }
        });
        if(!shortcut.isShowInWatch()) {
            ImageView iconShowOnWatch = (ImageView) shortcutView.findViewById(R.id.icon_show_on_watch);
            iconShowOnWatch.setVisibility(View.GONE);
        }

        View progress = shortcutView.findViewById(R.id.progress);
        if(shortcut.isExecuting()) {
            progress.setVisibility(View.VISIBLE);
        } else {
            progress.setVisibility(View.INVISIBLE);
        }

        return shortcutView;
    }

    private View inflateEmptyShortcut(Context ctx) {
        View shortcutView = LayoutInflater.from(ctx)
                .inflate(R.layout.shortcut_vertical, containerShortcuts, false);

        ImageButton btnShortcut = (ImageButton) shortcutView.findViewById(R.id.btn_shortcut);
        btnShortcut.setImageResource(R.drawable.ic_new);
        btnShortcut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onCreateNew();
                }
            }
        });

        TextView shortcutTitle = (TextView) shortcutView.findViewById(R.id.shortcut_title);
        shortcutTitle.setText(ctx.getString(R.string.mobile_shortcut_new));

        ImageView shortcutBg = (ImageView) shortcutView.findViewById(R.id.bg_shortcut);
        shortcutBg.setImageResource(R.drawable.background_shortcut_add);

        ImageView iconShowOnWatch = (ImageView) shortcutView.findViewById(R.id.icon_show_on_watch);
        iconShowOnWatch.setVisibility(View.GONE);

        View bgShadow = shortcutView.findViewById(R.id.bg_shadow);
        bgShadow.setVisibility(View.GONE);

        return shortcutView;
    }

    public void showFeedback(int index, boolean success) {
        final ImageButton buttonAction = (ImageButton) containerShortcuts.getChildAt(index).findViewById(R.id.btn_shortcut);
        final TextView executeFeedback = (TextView) containerShortcuts.getChildAt(index).findViewById(R.id.text_done);
        if(success) {
            executeFeedback.setText(R.string.mobile_done);
        } else {
            executeFeedback.setText(R.string.shortcut_execute_failed);
        }
        buttonAction.setVisibility(View.INVISIBLE);
        executeFeedback.setVisibility(View.VISIBLE);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                executeFeedback.setVisibility(View.GONE);
                buttonAction.setVisibility(View.VISIBLE);
            }
        }, 1000);
    }
}
