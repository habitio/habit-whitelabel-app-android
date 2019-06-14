package com.muzzley.app.shortcuts;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.jakewharton.rxbinding2.view.RxView;
import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.workers.DevicePickerActivity;
import com.muzzley.model.workers.RuleUnitResponse;
import com.muzzley.app.workers.RulesBuilder;
import com.muzzley.app.analytics.AnalyticsEvents;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.analytics.EventStatus;
import com.muzzley.app.cards.CardFragment;
import com.muzzley.model.workers.RuleUnit;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.rx.LogObserver;
import com.muzzley.util.ui.ShowcaseBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import timber.log.Timber;

public class ShortcutsActivity extends AppCompatActivity implements ShortcutsAdapter.OnItemActionsListener,
                                    ShortcutsAdapter.OnShortcutExecuteClickListener,
                                    SwipeRefreshLayout.OnRefreshListener {

    public static final int SHORTCUT_CREATE_CODE = 1;
    private static final int SHORTCUT_EDIT_CODE = 2;

    @Inject AnalyticsTracker analyticsTracker;
    @Inject ShortcutsController controller;

    @BindView(R.id.container) ViewGroup container;
    @BindView(R.id.btn_show_less) Button btnShowLess;
    @BindView(R.id.swipe_container) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.shortcut_list) RecyclerView shortcutRecycleView;
    @BindView(android.R.id.empty) View mEmptyView;
    @BindView(R.id.animation_container) ViewGroup animationContainer;

    private ShortcutsAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
//        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
//        ChangeBounds bounds = new ChangeBounds();
//        bounds.setDuration(1000);
//        getWindow().setSharedElementEnterTransition(bounds);
        getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        setContentView(R.layout.activity_shortcuts);

        ButterKnife.bind(this);

        mAdapter = new ShortcutsAdapter(this);
//        mAdapter.setHasStableIds(true);
        mAdapter.setOnAddClickListener(mOnAddClickListener);
        mAdapter.setOnItemActionsListener(this);
        mAdapter.setOnShortcutExecuteClickListener(this);

        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);

        shortcutRecycleView.setLayoutManager(layoutManager);
        shortcutRecycleView.setAdapter(mAdapter);
        shortcutRecycleView.requestDisallowInterceptTouchEvent(true);
        shortcutRecycleView.getItemAnimator().setMoveDuration(500);


        final ItemTouchHelper itemTouchHelper = new ItemTouchHelper(mItemTouchHelper);
        itemTouchHelper.attachToRecyclerView(shortcutRecycleView);

        final GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public void onLongPress(MotionEvent e) {
                switchEditMode(true);
                final View childView = shortcutRecycleView.findChildViewUnder(e.getX(), e.getY());
                if (childView != null) {
                    RecyclerView.ViewHolder childVH = shortcutRecycleView.getChildViewHolder(childView);
                    itemTouchHelper.startDrag(childVH);
                }
                super.onLongPress(e);
            }

        });
        shortcutRecycleView.addOnItemTouchListener(new RecyclerView.SimpleOnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                if (!mAdapter.getEditMode()) {
                    gestureDetector.onTouchEvent(e);
                    return mAdapter.getEditMode();
                }
                return false;
            }
        });

        swipeRefresh.setOnRefreshListener(this);
        swipeRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.gray_lighter,
                R.color.colorPrimary,
                R.color.gray_lighter);


        getShortcuts(true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            switch (requestCode) {

                case SHORTCUT_CREATE_CODE:
                    Timber.d("successfully created");

                    break;

                case SHORTCUT_EDIT_CODE: {
                    onRefresh();

                    CardFragment.isShortcutChanged = true;

                }
                break;

                case Constants.DEVICE_PICKER_ID_INTENT:
                    if(data != null) {
                        if(Constants.SHORTCUT_CHANGE_EVENT.equals(data.getAction())) {
                            onRefresh();

                            CardFragment.isShortcutChanged = true;
                        }
                    }
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            switch (requestCode) {
                case SHORTCUT_EDIT_CODE:
                    String shortcutId = data.getStringExtra(Constants.EXTRA_AGENTS_ID);
                    analyticsTracker.trackShortcutAction(AnalyticsEvents.EDIT_SHORTCUT_CANCEL_EVENT,
                            shortcutId);

                    break;
            }
        }
    }

    private void getShortcuts(final boolean firstLoad) {
        controller.getShortcuts(new Consumer<List<Shortcut>[]>() {

            @Override
            public void accept(List<Shortcut>[] data) {
                swipeRefresh.setRefreshing(false);

                RxView.globalLayouts(shortcutRecycleView)
                        .take(1)
                        .flatMap(new Function<Object, ObservableSource<Boolean>>() {
                            @Override
                            public ObservableSource<Boolean> apply(@NonNull Object o) throws Exception {
                                return ShowcaseBuilder.showcase(ShortcutsActivity.this,
                                        getString(R.string.mobile_onboarding_shortcuts_1_title),
                                        getString(R.string.mobile_onboarding_shortcuts_1_text),
                                        getString(R.string.mobile_onboarding_shortcuts_1_close),
                                        shortcutRecycleView.findViewHolderForAdapterPosition(1).itemView.findViewById(R.id.btn_execute),
                                        R.string.on_boarding_shortcuts
                                );
                            }
                        })
                        .subscribe(new LogObserver<Boolean>("Shortcuts List10"));


                mAdapter.setChildren(data);
                mAdapter.notifyDataSetChanged();

                if (Build.VERSION.SDK_INT >= 18 ) { //Build.VERSION_CODES.JELLY_BEAN_MR2
                    if (firstLoad) {
                        prepareStartAnimation(data);

//                        shortcutRecycleView.post(new Runnable() {
//                            @Override
//                            public void run() {
//                                animateShortcutsRotation();
//                            }
//                        });
                    }
                } else {
                    shortcutRecycleView.setVisibility(View.VISIBLE);
                }

                mEmptyView.setVisibility(View.GONE);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                swipeRefresh.setRefreshing(false);
                shortcutRecycleView.setVisibility(View.GONE);
                mEmptyView.setVisibility(View.VISIBLE);
            }
        });
    }

    private void prepareStartAnimation(List<Shortcut>[] data) {
        ViewGroup vg = (ViewGroup) findViewById(R.id.container_actions);
        for (int i = 0; i < Math.min(data[0].size(), 4); i++) {
            Shortcut shortcut = data[0].get(i);
            View shortcutView = LayoutInflater.from(ShortcutsActivity.this).inflate(R.layout.shortcut_vertical, vg, false);

            TextView shortcutTitle = (TextView) shortcutView.findViewById(R.id.shortcut_title);
            shortcutTitle.setText(shortcut.getLabel());

            ImageView shortcutBg = (ImageView) shortcutView.findViewById(R.id.bg_shortcut);
            shortcutBg.setColorFilter(Color.parseColor(shortcut.getColor()));

            ImageView iconShowOnWatch = (ImageView) shortcutView.findViewById(R.id.icon_show_on_watch);
            iconShowOnWatch.setVisibility(shortcut.isShowInWatch() ? View.VISIBLE : View.GONE);

            vg.addView(shortcutView);
        }

        for (int i = data[0].size(); i < 4; i++) {
            View shortcutView = LayoutInflater.from(ShortcutsActivity.this)
                    .inflate(R.layout.shortcut_vertical, vg, false);

            ImageButton btnShortcut = (ImageButton) shortcutView.findViewById(R.id.btn_shortcut);
            btnShortcut.setImageResource(R.drawable.ic_new);

            TextView shortcutTitle = (TextView) shortcutView.findViewById(R.id.shortcut_title);
            shortcutTitle.setText(ShortcutsActivity.this.getString(R.string.mobile_shortcut_new));

            ImageView shortcutBg = (ImageView) shortcutView.findViewById(R.id.bg_shortcut);
            shortcutBg.setImageResource(R.drawable.background_shortcut_add);

            ImageView iconShowOnWatch = (ImageView) shortcutView.findViewById(R.id.icon_show_on_watch);
            iconShowOnWatch.setVisibility(View.GONE);

            View bgShadow = shortcutView.findViewById(R.id.bg_shadow);
            bgShadow.setVisibility(View.GONE);

            vg.addView(shortcutView);
        }
    }

//    @SuppressLint("NewApi")
//    private void animateShortcutsRotation() {
//        List<Animator> animations = new ArrayList<>();
//        final List<View> animatedShortcuts = new ArrayList<>();
//        ViewGroup vg = (ViewGroup) findViewById(R.id.container_actions);
//        for(int i = 0; i < Math.min(4,shortcutRecycleView.getChildCount()-1); i++) {
//            int[] location = new int[2];
//            int[] childLocation = new int[2];
////            View listShortcutText = shortcutRecycleView.getChildAt(i + 1).findViewById(R.id.shortcut_title);
//
//            View listShortcutIcon = shortcutRecycleView.getChildAt(i + 1).findViewById(R.id.bg_shortcut);
////            View listShortcutIcon = shortcutRecycleView.getLayoutManager().getChildAt(i + 1).findViewById(R.id.bg_shortcut);
//            listShortcutIcon.getLocationInWindow(location);
//            View animatedShortcut = vg.getChildAt(i).findViewById(R.id.container_shortcut);
//            animatedShortcut.findViewById(R.id.shortcut_title).setVisibility(View.GONE);
//            animatedShortcuts.add(animatedShortcut);
//            animatedShortcut.getLocationInWindow(childLocation);
//
//            animationContainer.getOverlay().add(animatedShortcut);
//
//            ObjectAnimator animationX = ObjectAnimator.ofFloat(animatedShortcut, "translationX", location[0] - childLocation[0]);
//            animationX.setStartDelay(i * 30 + 100);
//            animationX.setInterpolator(new OvershootInterpolator(1.5f));
//            animations.add(animationX);
//
//            ObjectAnimator animationY = ObjectAnimator.ofFloat(animatedShortcut, "translationY", location[1] - childLocation[1]);
//            animationY.setStartDelay(i * 30 + 100);
//            animationY.setInterpolator(new OvershootInterpolator(1.5f));
//            animations.add(animationY);
//
//
//            /*ObjectAnimator textAnimation = new ObjectAnimator().ofFloat(listShortcutText, "alpha", 0f, 1f);
//            textAnimation.setStartDelay(700 + 100 + i * 30);
//            textAnimation.setDuration(300);
//            animations.add(textAnimation);*/
//        }
//
//        AnimatorSet animatorSet = new AnimatorSet();
//        animatorSet.playTogether(animations);
//        animatorSet.setDuration(700);//700
//
//        animatorSet.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                shortcutRecycleView.setVisibility(View.VISIBLE);
//                shortcutRecycleView.setAlpha(0f);//400
//                shortcutRecycleView.animate().alpha(1f).setDuration(400).setListener(new Animator.AnimatorListener() {
//                    @Override
//                    public void onAnimationStart(Animator animation) {
//                        shortcutRecycleView.setFocusable(false);
//                    }
//
//                    @Override
//                    public void onAnimationEnd(Animator animation) {
//                        for (int i = 0; i < animatedShortcuts.size(); i++) {
//                            animationContainer.getOverlay().remove(animatedShortcuts.get(i));
//                        }
//                        shortcutRecycleView.setFocusable(true);
//                    }
//
//                    @Override
//                    public void onAnimationCancel(Animator animation) {
//                        for (int i = 0; i < animatedShortcuts.size(); i++) {
//                            animationContainer.getOverlay().remove(animatedShortcuts.get(i));
//                        }
//                        shortcutRecycleView.setFocusable(true);
//                    }
//
//                    @Override
//                    public void onAnimationRepeat(Animator animation) {
//
//                    }
//                }).start();
//
//                for (View animatedShortcut : animatedShortcuts) {
//                    animatedShortcut.findViewById(R.id.bg_shadow).animate().alpha(0f).setDuration(400).start();
//                }
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
//
//        animatorSet.start();
//    }
//
    @Override
    public void onItemEdit(ShortcutVH viewHolder) {
        Shortcut shortcut = mAdapter.getChildren()[viewHolder.getGroupPosition()].get(viewHolder.getChildPosition());

        RuleUnit ruleUnit = new RuleUnit(Constants.BRIDGE_AGENTS_ACTIONABLE, shortcut.getActions());

        Gson gson = new Gson();
        Intent intent = new Intent(ShortcutsActivity.this, RulesBuilder.class);
        intent.putExtra(Constants.EXTRA_AGENTS_IS_EDITING, true);
        intent.putExtra(Constants.EXTRA_AGENTS_ID, shortcut.getId());
        intent.putExtra(Constants.EXTRA_AGENTS_NAME, shortcut.getLabel());
        intent.putExtra(Constants.EXTRA_AGENTS_ACTIONABLE, gson.toJsonTree(new RuleUnitResponse(ruleUnit)).toString());
        intent.putExtra(RulesBuilder.EXTRA_SHOW_IN_WATCH, shortcut.isShowInWatch());
        intent.putExtra(RulesBuilder.EXTRA_TYPE, RulesBuilder.TYPE_SHORTCUT);

        startActivityForResult(intent, SHORTCUT_EDIT_CODE);

        analyticsTracker.trackShortcutAction(AnalyticsEvents.EDIT_SHORTCUT_START_EVENT, shortcut.getId());
    }

//    @Override
    public void onItemDelete(final ShortcutVH viewHolder) {
        final Shortcut shortcut = mAdapter.getChildren()[viewHolder.getGroupPosition()].get(viewHolder.getChildPosition());

        new AlertDialog.Builder(this, R.style.AlertDialogStyle)
                .setMessage(getResources().getString(R.string.mobile_shortcut_delete_text))
                .setPositiveButton(R.string.mobile_delete, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteShortcut(shortcut.getId(), viewHolder);
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_CANCEL_EVENT,
                                shortcut.getId());
                    }
                }).show();

        analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_START_EVENT, shortcut.getId());
    }

    @OnClick(R.id.btn_show_less)
    void btnShowLessClick(View view) {
        if(mAdapter.getEditMode()) {
            switchEditMode(false);
        } else {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if(mAdapter.getEditMode()) {
            switchEditMode(false);
        } else {
            super.onBackPressed();
        }
    }

    private View.OnClickListener mOnAddClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(ShortcutsActivity.this, DevicePickerActivity.class);
            i.setAction(DevicePickerActivity.ACTION_SHORTCUT_CREATE);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION, true);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT, getString(R.string.mobile_device_search));
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_FIRST_STRING, getString(R.string.mobile_worker_select_trigger));
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE, Constants.AGENTS_ACTIONABLE);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, getString(R.string.mobile_shortcut_add));

            startActivityForResult(i, Constants.DEVICE_PICKER_ID_INTENT);
        }
    };

    private void showExecuteFail() {
        FeedbackMessages.showMessage(container, "Execute failed. Please check your network");
    }

    private void switchEditMode(boolean editMode) {
        if(editMode) {
            btnShowLess.setText(R.string.mobile_done);
        } else {
            btnShowLess.setText(R.string.mobile_show_less);
        }
        mAdapter.setEditMode(editMode);
    }

    @Override
    public void onRefresh() {
        swipeRefresh.setRefreshing(true);
        getShortcuts(false);
    }

    private ItemTouchHelper.SimpleCallback mItemTouchHelper = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0) {

        private boolean mIsDragging = false;
        private RecyclerView.ViewHolder mDraggedVH = null;
        private int mOldPosition = -1;

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            if(viewHolder instanceof ShortcutVH && target instanceof ShortcutVH) {
                ShortcutVH shortcutVH = (ShortcutVH)viewHolder;
                ShortcutVH targetShortcutVH = (ShortcutVH)target;

                if (viewHolder.getAdapterPosition() < target.getAdapterPosition()) {
                    for (int i = mAdapter.getChildPosition(shortcutVH.getAdapterPosition()); i < mAdapter.getChildPosition(targetShortcutVH.getAdapterPosition()); i++) {
                        Collections.swap(mAdapter.getChildren()[0], i, i + 1);
                    }
                } else {
                    for (int i = mAdapter.getChildPosition(shortcutVH.getAdapterPosition()); i > mAdapter.getChildPosition(targetShortcutVH.getAdapterPosition()); i--) {
                        Collections.swap(mAdapter.getChildren()[0], i, i - 1);
                    }
                }
                mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onSelectedChanged(final RecyclerView.ViewHolder viewHolder, int actionState) {
            super.onSelectedChanged(viewHolder, actionState);
            Timber.d("onSelectedChanged %d", actionState);
            if(mAdapter.getEditMode() && actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                mIsDragging = true;
                if(viewHolder.getLayoutPosition() > -1) {
                    mDraggedVH = viewHolder;
                    mOldPosition = mDraggedVH.getAdapterPosition();
                    mDraggedVH.itemView.setBackgroundResource(R.color.white_opacity_80);
                    controller.startDragFeedback(ShortcutsActivity.this, mDraggedVH.itemView);
                }
            } else if(mAdapter.getEditMode() &&actionState == ItemTouchHelper.ACTION_STATE_IDLE
                    && mIsDragging
                    && mDraggedVH != null) {

                mDraggedVH.itemView.setBackgroundResource(android.R.color.transparent);
                controller.endDragFeedback(ShortcutsActivity.this, mDraggedVH.itemView);

                if(mOldPosition != mDraggedVH.getAdapterPosition()) {
                    List<String> ids = new ArrayList<>();
                    for (Shortcut shortcut : mAdapter.getChildren()[0]) {
                        ids.add(shortcut.getId());
                    }
                    final int currentPosition = mDraggedVH.getAdapterPosition();
                    controller.reorderShortcuts(ids, new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            mDraggedVH = null;
                            mOldPosition = -1;
                            CardFragment.isShortcutChanged = true;
                            ShortcutWidgetProvider.sendUpdateWidgetsBroadcast(ShortcutsActivity.this);
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Collections.swap(mAdapter.getChildren()[0], currentPosition - 1, mOldPosition - 1);
                            mAdapter.notifyItemMoved(currentPosition, mOldPosition);
                            mDraggedVH = null;
                            mOldPosition = -1;
                            FeedbackMessages.showMessage(container, R.string.shortcuts_reorder_failure);
                        }
                    });
                }
            }
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }

        @Override
        public boolean isLongPressDragEnabled() {
            return mAdapter.getEditMode();
        }

        @Override
        public boolean canDropOver(RecyclerView recyclerView, RecyclerView.ViewHolder current, RecyclerView.ViewHolder target) {
            boolean canDrop = false;
            if(target instanceof ShortcutVH) {
                ShortcutVH targetShortcutVH = (ShortcutVH)target;
                if(targetShortcutVH.getGroupPosition() == 0) {
                    canDrop = true;
                }
            }

            return canDrop;
        }

        @Override
        public int getDragDirs(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            int dragDirs = 0;
            if(viewHolder instanceof ShortcutVH) {
                ShortcutVH targetShortcutVH = (ShortcutVH)viewHolder;
                if(targetShortcutVH.getGroupPosition() == 0) {
                    dragDirs = super.getDragDirs(recyclerView, viewHolder);
                }
            }

            return dragDirs;
        }
    };

    @Override
    public void onShortcutExecuteClick(final ShortcutVH viewHolder) {
        final int adapterPosition = viewHolder.getAdapterPosition();
        final Shortcut child = mAdapter.getChildren()[viewHolder.getGroupPosition()].get(viewHolder.getChildPosition());

        if (child.getId() == null)
            return;

        child.setIsExecuting(true);
        mAdapter.notifyItemChanged(adapterPosition);

        controller.executeShortcut(child.getId())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        child.setIsExecuting(false);
                        if (shortcutRecycleView.findViewHolderForAdapterPosition(adapterPosition) instanceof ShortcutVH) {
                            ShortcutVH itemVH = (ShortcutVH) shortcutRecycleView.findViewHolderForAdapterPosition(adapterPosition);
                            itemVH.progressBar.setVisibility(View.INVISIBLE);
                            itemVH.showFeedback(true);
                        }
                        analyticsTracker.trackShortcutExecute(child.getId(), "App", viewHolder.getGroupPosition() == 1 ? "Contextual" : child.getOrigin());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        child.setIsExecuting(false);
                        if (shortcutRecycleView.findViewHolderForAdapterPosition(adapterPosition) instanceof ShortcutVH) {
                            ShortcutVH itemVH = (ShortcutVH) shortcutRecycleView.findViewHolderForAdapterPosition(adapterPosition);
                            itemVH.progressBar.setVisibility(View.INVISIBLE);
                            itemVH.showFeedback(false);
                        }
                        showExecuteFail();
                    }
                });
    }

    private void deleteShortcut(final String shortcutId, final ShortcutVH viewHolder) {
        controller.deleteShortcut(shortcutId, new Consumer<Shortcut>() {

            @Override
            public void accept(Shortcut shortcut) {
                CardFragment.isShortcutChanged = true;

                mAdapter.getChildren()[viewHolder.getGroupPosition()].remove(viewHolder.getChildPosition());

                mAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                mAdapter.notifyItemRangeChanged(viewHolder.getAdapterPosition(), mAdapter.getItemCount());

                ShortcutWidgetProvider.sendUpdateWidgetsBroadcast(ShortcutsActivity.this);

                analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_FINISH_EVENT,
                        shortcutId,
                        EventStatus.Success,
                        "Success");
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
                Timber.d(throwable, "Error deleting shortcut");

                analyticsTracker.trackShortcutAction(AnalyticsEvents.REMOVE_SHORTCUT_FINISH_EVENT,
                        shortcutId,
                        EventStatus.Error,
                        throwable.getMessage());
            }
        });
    }
}
