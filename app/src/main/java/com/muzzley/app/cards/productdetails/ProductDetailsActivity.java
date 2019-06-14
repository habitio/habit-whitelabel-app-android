package com.muzzley.app.cards.productdetails;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.muzzley.App;
import com.muzzley.BuildConfig;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.WebViewActivity;
import com.muzzley.model.productDetails.Location;
import com.muzzley.model.productDetails.ProductDetails;
import com.muzzley.model.productDetails.Stores;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.retrofit.ChannelService;
import com.muzzley.util.retrofit.UserService;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by bruno.marques on 17/03/2016.
 */
public class ProductDetailsActivity extends AppCompatActivity
        implements ProductDetailsAdapter.OnProductClickListener,
                View.OnClickListener {

    public static final String EXTRA_CARD_ID = BuildConfig.APPLICATION_ID + ".extra.CARD_ID";

    public static final int DETAIL_VIEW_POSITION = 0;
    public static final int ERROR_VIEW_POSITION = 1;
    public static final int LOADING_VIEW_POSITION = 2;

    @Inject ChannelService channelService;
    @Inject UserService userService;
    @Inject PreferencesRepository preferencesRepository;

    @BindView(R.id.view_flipper) ViewFlipper mViewFlipper;
    @BindView(R.id.product_detail_image) ImageView productImageView;
    @BindView(R.id.product_recycler_view) RecyclerView recycler;
    @BindView(R.id.btn_retry) Button mBtnRetry;

    private ProductDetailsAdapter adapter;
    private LinearLayoutManager layoutManager;

    List<String> saveList = new ArrayList<String>();
    private static int SPEED_OF_DRAG = 20;
    private static int NEGATIVE_VALUE = -1;

    private String mProductId;
    private String mCardId;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.product_details);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mCardId = bundle.getString(EXTRA_CARD_ID, "");
            mProductId = bundle.getString(Constants.PRODUCT_DETAIL);
        }

        configActionBar();
        prepareImageRatio();

        recycler.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);

        adapter = new ProductDetailsAdapter(this, productImageView.getLayoutParams().height);
        adapter.setOnProductClickListener(this);
        recycler.setAdapter(adapter);

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int localSpeed = 0;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                localSpeed = dy;

                if (localSpeed < SPEED_OF_DRAG && localSpeed > (NEGATIVE_VALUE * SPEED_OF_DRAG)) {
                    for (int i = layoutManager.findFirstCompletelyVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition(); i++) {
                        String id = adapter.getItemIdAux(i);
                        if (id != null && !saveList.contains(id)) {
                            saveList.add(id);
                            putStoreView(id);
                            Timber.e("onScrolled--> showing: " + id);
                        }
                    }
                }
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (recycler.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    for (int i = layoutManager.findFirstCompletelyVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition(); i++) {
                        String id = adapter.getItemIdAux(i);
                        if (id != null && !saveList.contains(id)) {
                            saveList.add(id);
                            putStoreView(id);
                            Timber.e("onScrollStateChanged SCROLL_STATE_IDLE " + id);
                        }
                    }
                }
            }
        });

        mBtnRetry.setOnClickListener(this);

        loadProduct();
    }

    private void loadProduct() {
        mViewFlipper.setDisplayedChild(LOADING_VIEW_POSITION);

        Location lastLocation = preferencesRepository.getLastKnownLocation();
        if(lastLocation == null) {
            lastLocation = new Location();
        }
        channelService.getProductDetail(mProductId, lastLocation)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<ProductDetails>() {
                    @Override
                    public void accept(final ProductDetails productDetails) {
                        setImg(productImageView, productDetails.getImage());
                        adapter.setProductDetail(productDetails);

                        mViewFlipper.setDisplayedChild(DETAIL_VIEW_POSITION);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        mViewFlipper.setDisplayedChild(ERROR_VIEW_POSITION);
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(layoutManager.findFirstVisibleItemPosition()>=0 && layoutManager.findFirstVisibleItemPosition()>=0) {
            adapter.resumeView(layoutManager.findFirstVisibleItemPosition(), layoutManager.findLastVisibleItemPosition());

            for (int i = layoutManager.findFirstVisibleItemPosition(); i < layoutManager.findLastVisibleItemPosition(); i++) {
                String id = adapter.getItemIdAux(i);
                Timber.e("visivel no ecra pos: " + id);
                if (!saveList.contains(id)) {
                    saveList.add(id);
                    putStoreView(id);
                    Timber.e("store visivel no ecra pos : " + id);
                }
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.pauseView();
        Timber.e("onPause");
    }

    public void prepareImageRatio(){
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int w = size.x;
        productImageView.getLayoutParams().height= (int)(w * 9f / 16f);
    }

    public void setImg(ImageView img, String url) {
        Picasso.get()
                .load(url)
                .into(img);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void configActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public void putStoreView(String storeId) {
        Observable<String> observablePut = userService.putStoreView(mCardId, storeId);
        observablePut.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        Timber.e("PUT FEITO");
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.e(throwable, "PUT COM ERRO");
                    }
                });
    }

    @Override
    public void onProductClick(RecyclerView.ViewHolder viewHolder, Stores store) {
        if (store.getUrl() != null) {
            userService.sendAdCardClick(
                    mCardId,
                    store.getId()).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<String>() {
                        @Override
                        public void accept(String s) {
                            Timber.d("Registered ad click");
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Timber.e(throwable, "Register ad click failed");
                        }
                    });

            Intent intent = new Intent(this, WebViewActivity.class);
            intent.putExtra(WebViewActivity.EXTRA_URL, store.getUrl());

            startActivity(intent);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_retry:
                loadProduct();
                break;
        }
    }
}
