package com.muzzley.app.shortcuts;

import android.os.Handler;
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
 * Created by muzzley on 16-11-2015.
 */
public class ShortcutVH extends RecyclerView.ViewHolder {

    private static Handler handler = new Handler();

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

    @BindView(R.id.text_done)
    TextView executeFeedback;

    private ShortcutsAdapter.OnItemActionsListener mActionsListener;
    private ShortcutsAdapter.OnShortcutExecuteClickListener mShortcutExecuteClickListener;

    private int mGroupPosition;
    private int mChildPosition;

    public ShortcutVH(View itemView, ShortcutsAdapter.OnShortcutExecuteClickListener executeClickListener,
                      ShortcutsAdapter.OnItemActionsListener itemActionsListener) {
        super(itemView);

        ButterKnife.bind(this, itemView);

        mShortcutExecuteClickListener = executeClickListener;
        mActionsListener = itemActionsListener;

        buttonAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mShortcutExecuteClickListener != null) {
                    mShortcutExecuteClickListener.onShortcutExecuteClick(ShortcutVH.this);
                }
            }
        });

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mActionsListener != null) {
                    mActionsListener.onItemEdit(ShortcutVH.this);
                }
            }
        });

        buttonRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mActionsListener != null) {
                    mActionsListener.onItemDelete(ShortcutVH.this);
                }
            }
        });
    }

    public void setGroupPosition(int position) {
        mGroupPosition = position;
    }

    public int getGroupPosition() {
        return mGroupPosition;
    }

    public void setChildPosition(int position) {
        mChildPosition = position;
    }

    public int getChildPosition() {
        return mChildPosition;
    }

    public void showFeedback(boolean success) {
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
