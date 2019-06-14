package com.muzzley.app;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.Navigator;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.interfaces.Webview2RealtimeBridge;
import com.muzzley.app.interfaces.Webview2VideoBridge;
import com.muzzley.app.tiles.InterfacePresenter2;
import com.muzzley.app.tiles.ModelsStore;
import com.muzzley.model.channels.Address;
import com.muzzley.model.channels.Channel;
import com.muzzley.model.channels.NativeComponents;
import com.muzzley.model.tiles.Action;
import com.muzzley.model.tiles.Component;
import com.muzzley.model.tiles.Information;
import com.muzzley.model.tiles.Tile;
import com.muzzley.model.tiles.TileWidget;
import com.muzzley.providers.BusProvider;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.services.Realtime;
import com.muzzley.util.ui.ProgDialog;
import com.muzzley.util.ui.ijk.VideoFrame;
import com.muzzley.util.webview.MuzzleyWebViewChromeClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Created by ruigoncalo on 19/06/15.
 */
public class InterfaceActivity extends AppCompatActivity implements InterfaceTileAdapter.OnItemClickListener {

    @Inject public ModelsStore modelsStore;
    @Inject public PreferencesRepository preferencesRepository;

    private LinearLayoutManager linearLayoutManager;
    private Tile selectedTile;
    private List<Tile> selectedTiles;
    private int selectedPos;

    @Inject Gson gson;
    @Inject InterfacePresenter2 interfacePresenter2;
    @Inject Navigator navigator;
    @Inject AnalyticsTracker analyticsTracker;
    @Inject Realtime realtime;

    @BindView(R.id.recyclerview_channels) RecyclerView recyclerViewChannels;
    @BindView(R.id.layout_loading_devices) View layoutDevicesLoading;
    @BindView(R.id.layout_webview) ViewGroup layoutWebView;
    @Nullable @BindView(R.id.webview) WebView webView;
    @BindView(R.id.layout_error) View layoutError;
    @BindView(R.id.tiles) View tiles;
    @BindView(R.id.slidingIndicatorIcon) ImageView slidingIndicatorIcon;


    @Nullable @BindView(R.id.interfaceElement) View stub;
    @Nullable @BindView(R.id.videoSurfaceContainer) protected VideoFrame videoFrame;


//    private String targetUrl;
//    private List<NativeComponents> nativeComponentsList;
    private boolean didRequest;
    private String tileId;
    private String groupId;
    private InterfaceTileAdapter interfaceTileAdapter;
//    String audioUrl;
//    String videoUrl;
    private Webview2RealtimeBridge webview2realtimeBridge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_control_interface);
        ButterKnife.bind(this);

        linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerViewChannels.setLayoutManager(linearLayoutManager);
        recyclerViewChannels.setHasFixedSize(true);
        recyclerViewChannels.setHorizontalScrollBarEnabled(true);

        configActionBar();
        Intent intent = getIntent();
        if (intent != null && modelsStore.models != null) {
            groupId = intent.getStringExtra(Constants.GROUP_ID);
            tileId = intent.getStringExtra(Constants.TILE_ID);
        } else {
            finish(getString(R.string.mobile_error_text));
            return;
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
//        bridge.onResume(this);

        if (!didRequest) {

            if (tileId != null) {
                showTile(tileId);
                didRequest = true;
            } else if (groupId != null) {
                showGroup(groupId);
                didRequest = true;
            } else {
                finish(getString(R.string.mobile_error_text));
            }
        }
    }

    void showTile(String tileId) {
        Timber.d("New show tile");
        final Tile tile = modelsStore.models.getTile(tileId);
        selectedTile = tile;
        showDevicesLoading(false);

        tiles.setVisibility(View.GONE);
        // check if there was a label edit and update
        setTitle(tile.getLabel());
        if (hasNativeVideo()) {
            videoFrame.showLoading(getString(R.string.mobile_buffering_live));

            try {
                List<String> propAndComp = modelsStore.models.getFirstComponentWithClass(tile.getChannel(), "com.muzzley.properties.url.audio");
//            f(tile,"1","${stream-audio}")
              if (propAndComp != null)
                f(tile,propAndComp.get(1),"${"+propAndComp.get(0)+"}")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String streamUrl) {
                                Timber.d("read camera property audio "+streamUrl);
                                videoFrame.setUrl(VideoFrame.UrlType.audio,streamUrl);
//                                audioUrl = streamUrl;
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Timber.d(throwable, "error reading camera audio");
                            }
                        });

            } catch (Throwable throwable) {
                Timber.e(throwable,"Could not find audio property");
            }

            try {
                List<String> propAndComp = modelsStore.models.getFirstComponentWithClass(tile.getChannel(), "com.muzzley.properties.url.stream");
//            f(tile,"1","${stream}")
                if (propAndComp != null)
                f(tile,propAndComp.get(1),"${"+propAndComp.get(0)+"}")
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
//            Observable.just("rtsp://admin:Power181!@94.62.77.134:88/videoMain")
//            Observable.just("rtsp://admin:Power181!@77.54.123.48:88/videoMain")
//            Observable.just("rtsp://appviewer:appviewer181@fosbaby.office.muzzley.com:8096/videoMain")
//            Observable.just("rtsp://appviewer:appviewer181@fosbaby.office.muzzley.com:8096/audio")
//            Observable.just("https://v6.netatmo.net/restricted/10.255.72.178/530f55939bcfc3a9822d865eff526460/MTQ5MDY0MTIwMDpmfCvfBO89ei2mhCTuYlDXRGwU9g,,/live/index.m3u8")
//            Observable.just("http://www.androidbegin.com/tutorial/AndroidCommercial.3gp") // for testing
                        .subscribe(new Consumer<String>() {
                            @Override
                            public void accept(String streamUrl) {
                                Timber.d("read camera property stream "+streamUrl);
//                                targetUrl = videoUrl = streamUrl;
                                videoFrame.setAndPlay(VideoFrame.UrlType.live,streamUrl);
//                                videoFrame.setUrl(VideoFrame.UrlType.live,streamUrl);
//                                videoFrame.setVideoPath(targetUrl);
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                Timber.d(throwable, "error reading camera stream");
                                videoFrame.showError(throwable.getMessage());
                            }
                        });
            } catch (Throwable throwable) {
                Timber.e(throwable, "Could not find video property");
            }
        }
        loadInterface(tile);
    }

    Observable<String> f(final Tile tile, final String componentId, final String original){
        Timber.d("Got value: "+original);
        final String prop = getProp(original); //ex: ${stream}
        return prop == null ? Observable.just(original)
//                : interfacePresenter2.readPropertyAsString2(tile.remoteId,componentId, prop.substring(2,prop.length()-1))
                : interfacePresenter2.readPropertyAsString3(new Address(tile.getChannel(),componentId, prop.substring(2,prop.length()-1)))
                    .flatMap(new Function<String, Observable<String>>() {
                        @Override
                        public Observable<String> apply(String s) {
                            return f(tile,componentId,original.replace(prop, s));
                        }
                    });
    }

    String getProp(String string) {
        Matcher matcher = Pattern.compile("\\$\\{.+?\\}").matcher(string);
        return matcher.find() ? matcher.group() : null;
    }

    private void showGroup(String groupId) {
        showDevicesLoading(false);
        selectedTiles = modelsStore.models.getTileGroupSiblings(groupId);
        Tile groupControl = new Tile(
                "fake",
                getString(R.string.mobile_group_control),
                new ArrayList<String>(),
                selectedTiles.get(0).getProfile(),
                selectedTiles.get(0).getChannel(),
                null,
                getResourceUrl(R.drawable.control_all),
                null,
                null,
                new ArrayList<Component>(),
                new ArrayList<Information>(),
                new ArrayList<Action>(),
                false


        );
        List<Tile> tileGroup = new ArrayList<>();
        tileGroup.add(groupControl);
        tileGroup.addAll(selectedTiles);

        showTiles(tileGroup, 0, true);
    }

    String getResourceUrl(int resource) {
        return String.format("%s://%s/%s/%s",
                ContentResolver.SCHEME_ANDROID_RESOURCE,
                getResources().getResourcePackageName(resource),
                getResources().getResourceTypeName(resource),
                getResources().getResourceEntryName(resource));
    }


    @Override
    protected void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }



//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            toolbar.setVisibility(View.GONE);
//            applyLandscapeTransformation(displaymetrics);
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            toolbar.setVisibility(View.VISIBLE);
//            applyPortraitTransformation(displaymetrics);
//        }
//    }

//    @Override public void onBackPressed() {
//        Timber.d("onBackPressed");
//        backPressed = true;
//        if (getResources().getConfiguration().orientation != ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        startActivity(navigator.newTilesWithRefresh());
//        super.onBackPressed();
//    }
//

    private void configActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.mobile_loading);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        if (selectedPos > 0 || tileId != null ) {
            getMenuInflater().inflate(R.menu.menu_device, menu);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
//                startActivity(navigator.newTilesWithRefresh());
                finish();
                return true;
            case R.id.menu_edit:
                onEditClick();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean hasNativeVideo() {
        Tile tile = modelsStore.models.getTile(tileId);
        List<NativeComponents> nativeComponentsList = tile.get_interface().getNative();
        if (nativeComponentsList != null) {
            for (NativeComponents nativeComponents : nativeComponentsList) {
                if ("video".equals(nativeComponents.getType())) {
                    stub.setVisibility(View.VISIBLE);
                    ButterKnife.bind(this); // to inject the new inflated views
                    videoFrame.setComponentSize(nativeComponents.getSize());
                    return true;
                }
            }
        }
        return false;
    }

    private void setTitle(String title) {
        getSupportActionBar().setTitle(title);
    }

    public void showDevicesLoading(boolean show) {
        layoutDevicesLoading.setVisibility(show ? View.VISIBLE : View.GONE);
    }


    ProgressDialog progressDialog;
    public void showWebViewLoading(boolean show) {
        if (progressDialog == null && show) {
            progressDialog = ProgDialog.show(this);
        } else if (progressDialog != null && !show) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
//    public void showWebViewLoading(boolean show) {
//        if(show) {
//            layoutWebViewLoading.setVisibility(View.VISIBLE);
//            shimmer.setDuration(1000);
//            shimmer.startShimmerAnimation();
//        } else {
//            layoutWebViewLoading.setVisibility(View.GONE);
//            shimmer.stopShimmerAnimation();
//        }
//    }

    public void showLayoutError(boolean show) {
        layoutError.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showWebView(boolean show){
        layoutWebView.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    public void showLoading() {
        showWebViewLoading(true);
        showLayoutError(false);
    }

    public void showError(){
        showWebViewLoading(false);
        showWebView(false);
        showLayoutError(true);
    }

    public void showTiles(final List<Tile> tiles, final int selectedPosition, boolean isGrouped){
        interfaceTileAdapter = new InterfaceTileAdapter(this, tiles, this, isGrouped);
        interfaceTileAdapter.setSelectedPosition(selectedPosition);
        recyclerViewChannels.setAdapter(interfaceTileAdapter);
        linearLayoutManager.scrollToPosition(selectedPosition);
        // automatically select first item
        onTileClick(null, tiles.get(0), 0);

        this.tiles.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (InterfaceActivity.this.tiles.getTranslationY() == 0) {
                    toggleGroup();
                }
            }
        },2500);
        slidingIndicatorIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleGroup();
            }
        });
    }

    void toggleGroup() {
        tiles.animate().translationY(tiles.getTranslationY() == 0 ? recyclerViewChannels.getHeight() : 0);
    }


    public void loadNewWidget2(TileWidget tileWidget) {
        webView.setWebChromeClient(new MuzzleyWebViewChromeClient());

        webView.getSettings().setSupportZoom(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        // remove long press behavior
        webView.setLongClickable(false);
        webView.setHapticFeedbackEnabled(false);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onLoadResource(android.webkit.WebView view, String url) {
                Timber.d("Load resource " + url);
                super.onLoadResource(view, url);
            }

            @Override public void onPageStarted(android.webkit.WebView view, String url, Bitmap favicon) {
                Timber.d("Start page '" + url + "' load");
//                super.onPageStarted(view, url, favicon);
//                webView.animate().alpha(0f).setDuration(200);
//                loading.setVisibility(View.VISIBLE);
            }

            @Override
            public void onPageFinished(android.webkit.WebView view, String url) {
                super.onPageFinished(view, url);
//                webView.animate().alpha(1f).setDuration(200);
//                loading.setVisibility(View.GONE);
                if ("about:blank".equals(url)) return;
                view.requestFocus();
                Timber.d("Page finished, inject Javascript bridge");
                final String bridge = getStringFromAssets("webviewjavascriptbridge.js");
                view.loadUrl("javascript:" + bridge);
                Timber.d("Javascript bridge injected");
                view.requestFocus();
            }

            private String getStringFromAssets(String location) {
                try {
                    InputStream stream = getAssets().open(location);
                    int size = stream.available();
                    byte[] buffer = new byte[size];
                    stream.read(buffer);
                    stream.close();

                    return new String(buffer);
                } catch (IOException ex) {
                    return ";";
                }
            }
        });

//        webView.addJavascriptInterface(new AndroidJavascriptInterface(gson.toJsonTree(tileWidget.options).toString()), "android");
        webview2realtimeBridge = new Webview2RealtimeBridge(webView, realtime, modelsStore, gson.toJson(tileWidget.options));
        webView.addJavascriptInterface(webview2realtimeBridge, "android");
//        webView.addJavascriptInterface(new VideoJavascriptInterface(), "video");
        if (videoFrame != null) {
            webView.addJavascriptInterface(new Webview2VideoBridge(videoFrame), "video");
        }

        webView.loadUrl(tileWidget.url);
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            if (webview2realtimeBridge != null)
                webview2realtimeBridge.destroy();
            webView.removeAllViews();
            webView.destroy();
        }
        super.onDestroy();

    }

    @Override
    public void onTileClick(View v, Tile tile, int position) {

        selectedPos = position;
        selectedTile = tile;
        invalidateOptionsMenu();

        setTitle(tile.getLabel());
        loadInterface(tile);
    }

    private void loadInterface(Tile tile){
        Timber.d("Fetching interface for " + tile.get_interface().getUuid());
        if (tile.get_interface() != null && tile.get_interface().getUuid() != null&& tile.get_interface().getEtag() != null) {
            showLoading();
            interfacePresenter2.fetchInterface(tile.get_interface().getUuid(), tile.get_interface().getEtag())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Map<String, String>>() {
                        @Override
                        public void accept(Map<String, String> map) {
                            String uuid = (String) map.get("uuid");
                            String path = (String) map.get("path");
                            Timber.d("IA: uuid=" +  uuid+", path: "+path);
                            if (selectedTile.get_interface().getUuid().equals(uuid)) {
                                showWebViewLoading(false);
                                showWebView(true);
//                                loadNewWidget(buildWidget(path));
                                loadNewWidget2(buildWidget2(path));
                            } else {
                                showError();
                            }               }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) {
                            Timber.d(throwable, "Error getting interface");
                            showError();
                        }
                    });
        } else {
            showError();
        }
    }

    private TileWidget buildWidget2(String path) {
        TileWidget tileWidget = new TileWidget();
        if (selectedPos == 0 && selectedTiles != null) {
            tileWidget.options.tiles.addAll(selectedTiles);
            for (Tile tile : selectedTiles) {
                tileWidget.options.channels.add(modelsStore.models.getChannel(tile.getChannel()));
            }
        } else {
            tileWidget.options.channels.add(modelsStore.models.getChannel(selectedTile.getChannel()));
            tileWidget.options.tiles.add(selectedTile);
        }
        tileWidget.options.preferences = preferencesRepository.getPreferences();
        tileWidget.options.capabilities.addAll(preferencesRepository.getMuzzCapabilities());
//        tileWidget.options.tiles.add(selectedTile);
        tileWidget.url = "file://" + path;
        return tileWidget;
    }


    private void onEditClick() {
        Channel channel = modelsStore.models.getChannel(selectedTile.getChannel());

        startActivityForResult(new Intent(this, InterfaceSettingsActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(Constants.EXTRA_DEVICE_NAME, selectedTile.getLabel())
                        //FIXME: change this to tile_id
                .putExtra(Constants.EXTRA_DEVICE_ID, selectedTile.getId())
                .putExtra(Constants.EXTRA_PROFILE_ID, selectedTile.getProfile())
                .putExtra(Constants.EXTRA_CHANNEL_ID, selectedTile.getChannel())
                , Constants.REQUEST_CODE_INTERFACES);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("onResult " + requestCode + " " + resultCode);

        if(resultCode == RESULT_OK){
            if (data != null) {
                String newTitle = data.getStringExtra(Constants.INTERFACE_TITLE);
                if (selectedTile != null && !selectedTile.getLabel().equals(newTitle) ) {
                    selectedTile.setLabel(newTitle);
                    setTitle(newTitle);
                }
            } else {
                Timber.d("Will finish");
                finish();
            }
        }
    }

    public void finish(String message) {
        Intent intent = navigator.newHomeIntent(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra(Constants.EXTRA_MESSAGE, message);
        startActivity(intent);
        finish();
    }


//FIXME: add tracking to manual interactions
//    analyticsTracker.trackDeviceInteraction(selectedTile.profile, finalArray.get("property").toString(),
//    groupCount, isGroup ? "Group" : "Individual", "Advance interface",
//    EventStatus.Success,
//        "Success");
//    analyticsTracker.trackDeviceInteraction(selectedTile.profile, prop,
//    groupCount, isGroup ? "Group" : "Individual", "Advance interface",
//    error ? EventStatus.Error : EventStatus.Success,
//    error ? e.getMessage() : "Success");

}

