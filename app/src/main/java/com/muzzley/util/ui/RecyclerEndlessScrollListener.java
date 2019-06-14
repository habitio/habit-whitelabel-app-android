package com.muzzley.util.ui;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Created by ruigoncalo on 21/01/15.
 */
public abstract class RecyclerEndlessScrollListener extends RecyclerView.OnScrollListener {

    private int previousTotal = 0; // The total number of items in the dataset after the last load
    private boolean loading = true; // True if we are still waiting for the last set of data to load.
    private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
    int firstVisibleItem, visibleItemCount, totalItemCount;
    private int currentPage = 1;
    private int scrollOffset = 0;
    private int scrollMargin; // we just want values between 0 and scrollMargin;

    private LinearLayoutManager mLinearLayoutManager;

    public RecyclerEndlessScrollListener(LinearLayoutManager linearLayoutManager, int scrollMargin) {
        this.mLinearLayoutManager = linearLayoutManager;
        this.scrollMargin = scrollMargin;
    }

    public void reset(int previousTotal, boolean loading){
        this.previousTotal = previousTotal;
        this.loading = loading;
        this.scrollOffset = 0;
    }

    public void setScrollOffset(int value){
        this.scrollOffset = value;
    }

    @Override
    public void onScrollStateChanged(RecyclerView recyclerView, int newState){
        super.onScrollStateChanged(recyclerView, newState);
        onStateChanged(newState);
    }


    @Override
    public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        clip();
        onScrolled(scrollOffset);
        if((scrollOffset < scrollMargin && dy > 0) || (scrollOffset > 0 && dy < 0)) {
            scrollOffset += dy;
        }

        visibleItemCount = recyclerView.getChildCount();
        totalItemCount = mLinearLayoutManager.getItemCount();
        firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

        if (loading && (totalItemCount > previousTotal)) {
            loading = false;
            previousTotal = totalItemCount;
        }

        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            // End has been reached
            // Do something
            currentPage++;
            onLoadMore(currentPage);
            loading = true;
        }

    }

    private void clip(){
        if(scrollOffset > scrollMargin){
            scrollOffset = scrollMargin;
        } else if(scrollOffset < 0){
            scrollOffset = 0;
        }
    }

    public abstract void onLoadMore(int currentPage);

    public abstract void onScrolled(int distance);

    public abstract void onStateChanged(int state);
}
