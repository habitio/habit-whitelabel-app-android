package com.muzzley.app.cards;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.muzzley.R;
import com.muzzley.app.shortcuts.ShortcutActionsListener;
import com.muzzley.app.shortcuts.ShortcutsVH;
import com.muzzley.model.cards.Card;
import com.muzzley.model.shortcuts.Shortcut;

import java.util.List;

/**
 * Created by kyryloryabin on 18/12/15.
 */
public class CardAdapter extends ContainerAdapter<Card> {

    private static final int VIEW_TYPE_SHORTCUTS = 0;

    private ShortcutActionsListener mOnShortcutActionsListener;

    private List<Shortcut> shortcuts;

    public CardAdapter(Context context, int layout, int rootContainer) {
        super(context, layout, rootContainer);
    }

    public void setOnShortcutActionsListener(ShortcutActionsListener listener) {
        mOnShortcutActionsListener = listener;
    }

//    public void setShortcuts(List<Shortcut> shortcuts) {
//        this.shortcuts = shortcuts;
////        notifyDataSetChanged();
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_SHORTCUTS:
                return new ShortcutsVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_shortcuts, parent, false));

            default:
                return super.onCreateViewHolder(parent, viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case VIEW_TYPE_SHORTCUTS:
                ((ShortcutsVH)holder).setShortcuts(shortcuts, mOnShortcutActionsListener);
                break;

            default:
                super.onBindViewHolder(holder, position - (isShowShortcuts() ? 1 : 0));
        }

    }

    public void setData(List<Shortcut> shortcuts,List<Card> data) {
        this.shortcuts = shortcuts;
        super.setData(data);
    }

    @Override
    public int getItemViewType(int position) {
        if (isShowShortcuts()) {
            if (position == 0)
                return VIEW_TYPE_SHORTCUTS;
            else
                return data.get(position - 1).id.hashCode();
        } else {
            return data.get(position).id.hashCode();
        }
        //return isShowShortcuts() ? position : position + 1;
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (isShowShortcuts() ? 1 : 0);
    }

    public boolean isShowShortcuts() {
//        return false;
        return shortcuts != null;
    }
}
