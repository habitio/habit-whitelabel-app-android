package com.muzzley.app.shortcuts;

import android.content.Context;
import android.graphics.Color;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.muzzley.R;
import com.muzzley.model.shortcuts.Shortcut;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class ShortcutsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface OnItemActionsListener {
        void onItemEdit(ShortcutVH viewHolder);
        void onItemDelete(ShortcutVH viewHolder);
    }

    public interface OnShortcutExecuteClickListener {
        void onShortcutExecuteClick(ShortcutVH viewHolder);
    }

    private static final int HEADER_VIEW_TYPE = 0;
    private static final int CHILD_VIEW_TYPE = 1;
    private static final int ADD_BUTTON_TYPE = 2;
    private static final int SUGGESTIONS_EMPTY_TYPE = 3;

    private static final int MIN_ITEMS_COUNT = 4; //Minimum items needed for animation
    private static final int HEADER_OFFSET = 1;

    private Context mContext;
    private List<Shortcut>[] mChildren = new List[] { new ArrayList<>(), new ArrayList<>() };

    private boolean isEditMode = false;
    private int mGlobalGroupPosition = 0;

    private OnItemActionsListener mOnItemActionsListener;
    private OnShortcutExecuteClickListener mOnShortcutExecuteClickListener;
    private View.OnClickListener mOnAddClickListener;

    public ShortcutsAdapter(Context context) {
        this.mContext = context;
    }

    public void setOnItemActionsListener(OnItemActionsListener listener) {
        mOnItemActionsListener = listener;
    }

    public void setOnShortcutExecuteClickListener(OnShortcutExecuteClickListener listener) {
        mOnShortcutExecuteClickListener = listener;
    }

    public void setOnAddClickListener(View.OnClickListener listener) {
        mOnAddClickListener = listener;
    }

    public void setEditMode(boolean mode) {
        isEditMode = mode;
        notifyDataSetChanged();
    }

    public List<Shortcut>[] getChildren() {
        return mChildren;
    }

    public boolean getEditMode() {
        return isEditMode;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh = null;

        switch (viewType) {
            case HEADER_VIEW_TYPE:
                vh = new HeaderVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_header, parent, false));
                break;
            case CHILD_VIEW_TYPE:
                vh = new ShortcutVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_shortcut, parent, false), mOnShortcutExecuteClickListener, mOnItemActionsListener);
                break;
            case ADD_BUTTON_TYPE:
                vh = new AddButtonVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_shortcut, parent, false), mOnAddClickListener);
                break;

            case SUGGESTIONS_EMPTY_TYPE:
                vh = new SimpleVH(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.adapter_item_shortcut_suggestions_empty, parent, false));
                break;
        }

        return vh;
    }

    public void setChildren(List<Shortcut>[] children) {
        mChildren = children;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        int itemViewType = getItemViewType(position);
        int groupPosition = getGroupPosition(position);
        int childPosition = getChildPosition(position);

        switch (itemViewType) {
            case HEADER_VIEW_TYPE:
                String title = "";
                if(groupPosition == 0) {
                    title = mContext.getString(R.string.mobile_shortcut_my);
                } else if(groupPosition == 1) {
                    title = mContext.getString(R.string.mobile_shortcut_suggestion);
                }

                HeaderVH headerVH = (HeaderVH) holder;
                headerVH.title.setText(title);
                break;

            case CHILD_VIEW_TYPE:
                Shortcut shortcut = mChildren[groupPosition].get(childPosition);
                ShortcutVH childVH = (ShortcutVH) holder;
                childVH.setGroupPosition(groupPosition);
                childVH.setChildPosition(childPosition);

                childVH.image.setImageResource(R.drawable.circle_shape);
                childVH.title.setText(shortcut.getLabel());
                if(shortcut.getColor() != null) {
                    childVH.image.setColorFilter(Color.parseColor(shortcut.getColor()));
                }

                if(groupPosition == 0 && isEditMode && shortcut.getActions() != null && shortcut.getActions().size() > 0) {
                    childVH.buttonEdit.setVisibility(View.VISIBLE);
                    childVH.buttonRemove.setVisibility(View.VISIBLE);
                } else {
                    childVH.buttonEdit.setVisibility(View.GONE);
                    childVH.buttonRemove.setVisibility(View.INVISIBLE);
                }
                childVH.iconShowOnWatch.setVisibility(shortcut.isShowInWatch() ? View.VISIBLE : View.INVISIBLE);

                if(shortcut.isExecuting()) {
                    childVH.progressBar.setVisibility(View.VISIBLE);
                } else {
                    childVH.progressBar.setVisibility(View.INVISIBLE);
                }
                break;

            case ADD_BUTTON_TYPE:
                AddButtonVH addButtonVH = (AddButtonVH) holder;
                if(position == Math.max(mChildren[0].size(), MIN_ITEMS_COUNT) + getAddButtonCounter()) {
                    addButtonVH.divider.setVisibility(View.INVISIBLE);
                } else {
                    addButtonVH.divider.setVisibility(View.VISIBLE);
                }
                break;
        }
    }

    @Override
    public int getItemCount() {
        int count = 0;
        for (int i = 0; i < mChildren.length; i++) {
            count += mChildren[i].size();
            if(i == 0) {
                count = Math.max(count, MIN_ITEMS_COUNT);
            }
        }

        if(!hasSuggestions()) {
            count++;
        }

        return count + getGroupCount() + getAddButtonCounter();
    }

    @Override
    public int getItemViewType(int position) {
        int itemGroupPosition = getGroupPosition(position);
        int itemChildPosition = getChildPosition(position);
        if(position == getItemCount() - 1 && !hasSuggestions()) {
            return SUGGESTIONS_EMPTY_TYPE;
        } else if(position == 0 || itemGroupPosition != getGroupPosition(position - 1)) {
            return HEADER_VIEW_TYPE;
        } else if(itemGroupPosition == 0) {
            int shortcutsCount = mChildren[0].size();
            if(itemChildPosition > shortcutsCount && itemChildPosition < MIN_ITEMS_COUNT) {
                return ADD_BUTTON_TYPE;
            } else if(position == mChildren[0].size() + 1) {
                return ADD_BUTTON_TYPE;
            }
        }

        return CHILD_VIEW_TYPE;
    }

    private int getGroupCount() {
        return 2;
    }

    /**
     * @param position Current RecyclerView adapter position.
     * @return Zero based group position.
     */
    private int getGroupPosition(int position) {
        int groupPosition = 0;
        mGlobalGroupPosition = 0;
        int totalCount = getAddButtonCounter();//1 is because of add button in first section
        for (int i = 0; i < mChildren.length; i++) {
            if(i == 0) {
                totalCount += Math.max(mChildren[i].size(), MIN_ITEMS_COUNT) + HEADER_OFFSET;
            } else {
                totalCount += mChildren[i].size() + HEADER_OFFSET;
            }
            if(totalCount > position) {
                break;
            }
            mGlobalGroupPosition = totalCount; //Position is recalculated each time because child from another group could be inflated before the header
            groupPosition++;
        }

        return groupPosition;
    }

    public int getChildPosition(int position) {
        int groupPosition = getGroupPosition(position);
        int childPosition = (position - mGlobalGroupPosition);
        if(childPosition < 0) {
            int groupItemCount = mChildren[groupPosition].size();
            if(groupPosition == 0) {
                groupItemCount = Math.max(groupItemCount, MIN_ITEMS_COUNT);
                childPosition += getAddButtonCounter();//Plus 1 because of add button
            }
            childPosition += groupItemCount;
        } else {
            childPosition -= 1;
        }
        return childPosition;
    }

    /**
     * Determine if add button will exist
     * @return 0 or 1
     */
    private int getAddButtonCounter() {
        int returnValue = 1;
        if(mChildren[0].size() < MIN_ITEMS_COUNT) {
            returnValue = 0;
        }
        return returnValue;
    }

    private boolean hasSuggestions() {
        if(mChildren[1].size() == 0)
            return false;

        return true;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }
}
