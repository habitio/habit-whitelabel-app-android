package com.muzzley.app.workers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.gson.Gson;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.jakewharton.rxbinding2.widget.TextViewTextChangeEvent;
import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsEvents;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.analytics.EventStatus;
import com.muzzley.app.tiles.Models;
import com.muzzley.app.tiles.ModelsStore;
import com.muzzley.app.tiles.TilesController;
import com.muzzley.model.channels.Channel;
import com.muzzley.model.tiles.Tile;
import com.muzzley.model.tiles.TileGroup;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.Utils;
import com.muzzley.util.ui.ClearableEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;


/**
 * Created by bruno.marques on 13/11/2015.
 */
public class DevicePickerActivity extends AppCompatActivity implements DevicePickerAdapter.SmoothScrollInterface {

    public static final String ACTION_SHORTCUT_CREATE = "action.SHORTCUT_CREATE";

    @Inject PreferencesRepository preferencesRepository;
    @Inject Gson gson;

    @Inject TilesController tilesController;
    @Inject ModelsStore modelsStore;
    @Inject AnalyticsTracker analyticsTracker;
    @Inject DevicePickerMashData devicePickerMashData;

    @BindView(R.id.layout_loading_devices) ProgressBar progressBar;
    @BindView(R.id.device_picker_layout) RelativeLayout devicePickerLayout;

    @BindView(R.id.recyclerview) RecyclerView recyclerview;
    @BindView(R.id.edittext_search) ClearableEditText editTextSearch;
    @BindView(R.id.icon) ImageView editTextSearchImg;
    @BindView(R.id.group_name) EditText editGroupName;
    @BindView(R.id.worker_button_next) Button buttonNext;

    private InputMethodManager imm;
    private DevicePickerAdapter adapter;
    private boolean multipleSelection, createGroup;

    private List<DevicePickerAdapter.Item> items;
    ArrayList<String> statesTilesSelected;
    private String actionBarText, editTextHint, firstStringText, dataType;

    DevicePickerAdapter.SelectionType selectionType;
    Models models;
    String exclude;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_device_picker);
        ButterKnife.bind(this);

        Bundle bundle = getIntent().getExtras();
        actionBarText = bundle.getString(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT);
        editTextHint = bundle.getString(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT);
        firstStringText = bundle.getString(Constants.EXTRA_DEVICE_PICKER_FIRST_STRING);
        multipleSelection = bundle.getBoolean(Constants.EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION);
        createGroup = bundle.getBoolean(Constants.EXTRA_DEVICE_PICKER_CREATE_GROUP);
        dataType = bundle.getString(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE);
        exclude =  bundle.getString(Constants.EXTRA_DEVICE_PICKER_DEVICE_EXCLUDE, null);

        if (createGroup) {
            selectionType = DevicePickerAdapter.SelectionType.GROUP;
            buttonNext.setEnabled(false);
            editGroupName.setVisibility(View.VISIBLE);
            editTextSearch.setVisibility(View.GONE);
            editTextSearchImg.setVisibility(View.GONE);
        } else if (multipleSelection) {
            selectionType = DevicePickerAdapter.SelectionType.MULTIPLE;
        } else {
            selectionType = DevicePickerAdapter.SelectionType.SINGLE;
        }

        if(!createGroup && dataType!= null && dataType.equals(Constants.AGENTS_STATEFULL)){
            statesTilesSelected = bundle.getStringArrayList(Constants.EXTRA_DEVICE_PICKER_DEVICE_STATES_ALREADY);
        }

        editTextSearch.setHint(editTextHint);

        recyclerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (createGroup) {
                    createGroup();
                    return;
                }
                onNext();
            }
        });

        configActionBar();

        Observable<Models> obs;
        if (dataType != null) {
            if(exclude == null) {
                //obs = DevicePickerMashData.getItemsWithType(api, preferencesRepository.getUser().getId(), dataType);
                switch (dataType) {
                    case Constants.AGENTS_TRIGGERABLE:
                        obs = tilesController.getModelsWithType(Constants.AGENTS_TRIGGERABLE);
                        break;
                    case Constants.AGENTS_ACTIONABLE:
                        obs = tilesController.getModelsWithType(Constants.AGENTS_ACTIONABLE);
                        break;
                    case Constants.AGENTS_STATEFULL:
                        obs = modelsStore.modelsStates != null ? Observable.just(modelsStore.modelsStates) : tilesController.getModelsWithType(Constants.AGENTS_STATEFULL);
                        break;
                    default:
                        obs = modelsStore.models != null ? Observable.just(modelsStore.models) : tilesController.getModels();
                }

            } else {
                obs = devicePickerMashData.getItemsWithTypeAndExclude(dataType, exclude);
            }
        } else {
            obs = modelsStore.models != null ? Observable.just(modelsStore.models) : tilesController.getModels();
        }

        obs.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Consumer<Models>() {
                            @Override
                            public void accept(Models models) {
                                updateModelsLocalData(models);
                                updateLayoutWithData();
                            }
                        },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                progressBar.setVisibility(View.GONE);
                                Timber.e(throwable, "Device Picker Activity ERRO: " + throwable.getMessage());
                            }
                        }
                );

        if (ACTION_SHORTCUT_CREATE.equals(getIntent().getAction())) {
            analyticsTracker.trackSimpleEvent(AnalyticsEvents.CREATE_SHORTCUT_START_EVENT);
            analyticsTracker.trackSimpleEvent(AnalyticsEvents.CREATE_SHORTCUT_ADD_ACTION_START);
        } else if(createGroup) {
            analyticsTracker.trackSimpleEvent(AnalyticsEvents.CREATE_GROUP_START_EVENT);
        }
    }

    private void createGroup() {
        List<String> tileIds = adapter.getCurrentSelectedChannel();
        TileGroup tileGroup = new TileGroup();
        tileGroup.label = editGroupName.getText().toString();
        for(DevicePickerAdapter.Item item: items) {
            if (item.id.equals(tileIds.get(0))) {
                tileGroup.parent = item.parent;
                break;
            }
        }
        tilesController.createGroup(tileGroup, tileIds).subscribe(
                new Consumer<Tile>() {
                    @Override
                    public void accept(Tile tile) {
                        Timber.d("DPA: tile updated %s", tile);
                    }
                },
                new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        analyticsTracker.trackGroupAction(AnalyticsEvents.CREATE_GROUP_FINISH_EVENT,
                                EventStatus.Error,
                                throwable.getMessage());

                        Timber.d(throwable, "DPA: error creating group");
                        FeedbackMessages.showError(recyclerview);
                    }
                },
                new Action() {
                    @Override
                    public void run() {
                        analyticsTracker.trackGroupAction(AnalyticsEvents.CREATE_GROUP_FINISH_EVENT,
                                EventStatus.Success,
                                "Success");
                        //navigate  to tiles with refresh
                        navigateToHomeAndFinish();
                    }
                }
        );
    }

    private void navigateToHomeAndFinish(){
//        startActivity(new Intent(this, HomeActivity.class)
//                        .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                        .putExtra(Constants.EXTRA_UPDATE_CHANNELS, true)
//                        .putExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS, Constants.FRAG_CHANNELS)
//        );
        //startActivity(navigator.newTilesWithRefresh());

        setResult(RESULT_OK);
        finish();
    }


    public void updateLayoutWithData(){
        List<String> muzzCapa = null;
        if(preferencesRepository.getMuzzCapabilities() != null){
            muzzCapa = new ArrayList<String>(preferencesRepository.getMuzzCapabilities());
        }

        if (createGroup) {
            items = DevicePickerController.INSTANCE.createModelViewDataGroups(this.models);
        } else {
            if (dataType != null && dataType.equals(Constants.AGENTS_STATEFULL)) {
                items = DevicePickerController.INSTANCE.createModelViewData(this.models, statesTilesSelected, muzzCapa, dataType);
            } else {
                items = DevicePickerController.INSTANCE.createModelViewData(this.models, muzzCapa, dataType);
            }
        }

        progressBar.setVisibility(View.GONE);
        devicePickerLayout.setVisibility(View.VISIBLE);

        adapter = new DevicePickerAdapter(this, items, selectionType, this);
        recyclerview.setAdapter(adapter);

        if (selectionType == DevicePickerAdapter.SelectionType.GROUP) {

            Observable<TextViewTextChangeEvent> groupName = RxTextView.textChangeEvents(editGroupName);
//            Observable<OnTextChangeEvent> groupName = ViewObservable.text(editGroupName);

            Observable.combineLatest(groupName, adapter.getCurrentSelectedChannelObservable(), new BiFunction<TextViewTextChangeEvent, List<String>, Boolean>() {
                @Override
                public Boolean apply(TextViewTextChangeEvent onTextChangeEvent, List<String> selectedChannelIds) {
                    return onTextChangeEvent.text().length() > 0 && selectedChannelIds.size() >= 2;
                }
            }).subscribe(new Consumer<Boolean>() {
                @Override
                public void accept(Boolean nextOk) {
                    buttonNext.setEnabled(nextOk);
                }
            });
        } else {
            Observable devicesSelected = adapter.getCurrentSelectedChannelObservable();
            devicesSelected.subscribe(new Consumer<ArrayList>() {
                @Override
                public void accept(ArrayList selectionArray) {
                    buttonNext.setEnabled(selectionArray.size()!=0);
                }
            });

        }

        Observable<TextViewTextChangeEvent> searchTextObservable = RxTextView.textChangeEvents(editTextSearch.getEditText());

        searchTextObservable
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<TextViewTextChangeEvent>() {
                    @Override
                    public void accept(TextViewTextChangeEvent onTextChangeEvent) {
                        adapter.setFilter(onTextChangeEvent.text().toString());
//                        checkIfExist(onTextChangeEvent.text.toString());
                    }
                });


        //para fazer pesquisa caso o utilizador escreva antes de aparecer a lista
        if(!editTextSearch.getText().toString().isEmpty()){
//            checkIfExist(editTextSearch.getText().toString());
            adapter.setFilter(editTextSearch.getText().toString());
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelActivity();
                finish();
                return true;
            case R.id.ok:
                Toast.makeText(DevicePickerActivity.this, "Saving "+adapter.getCurrentSelectedChannel().size(), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy(){
//        BusProvider.getInstance().unregister(this);
        super.onDestroy();
    }

    private void advanceForWebView(List<String> currentSelectedChannelIds) {
        List<Tile> lTiles = new ArrayList<>();
        List<Channel> lChannels = new ArrayList<>();

        if(currentSelectedChannelIds != null && !currentSelectedChannelIds.isEmpty()) {
            for(String ids :currentSelectedChannelIds) {
                Tile tile = models.getTile(ids);
                Channel channel = models.getChannel(tile.getChannel());

                lTiles.add(tile);
                lChannels.add(channel);
            }

            String tiles = gson.toJson(lTiles);
            String channels = gson.toJson(lChannels);

//            Intent i = new Intent(this, AgentDeviceWebViewActivity.class);
            Intent i = new Intent(this, WorkerWebviewActivity.class);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, actionBarText);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE, correctTypeTermForWebView(dataType));
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_WEBVIEW_TILE, tiles);
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_WEBVIEW_CHANNEL, channels);

            if(getIntent() != null) {
                i.setAction(getIntent().getAction());
            }

            startActivityForResult(i, Constants.DEVICE_PICKER_ID_INTENT_WEBVIEW);
        }
    }

    private void onNext() {
        advanceForWebView(adapter.getCurrentSelectedChannel());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.DEVICE_PICKER_ID_INTENT_WEBVIEW) {
            if(data != null) {
                if(Constants.SHORTCUT_CHANGE_EVENT.equals(data.getAction())) {
                    setResult(RESULT_OK, data);
                    finish();
                    return;
                }
            }
        }
        if (requestCode == Constants.DEVICE_PICKER_ID_INTENT_WEBVIEW && resultCode == Activity.RESULT_OK && data != null) {
            String result = data.getStringExtra(Constants.DEVICE_PICKER_ID_INTENT_RESULT);
            //TODO mensagem de erro quando nao se consegue criar a rule
            finishActivity(result);
        }
    }

    private void finishActivity(String result){
        Intent output = new Intent();
        output.putExtra(Constants.DEVICE_PICKER_ID_INTENT_RESULT, result);
        setResult(RESULT_OK, output);
        finish();
    }

    private void configActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(actionBarText);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void updateModelsLocalData(Models models){
        this.models = models;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int x = (int) ev.getX();
        int y = (int) ev.getY();

        if (ev.getAction() == MotionEvent.ACTION_DOWN &&
                !Utils.getLocationOnScreen(editTextSearch).contains(x, y)) {
            InputMethodManager input = (InputMethodManager)
                    this.getSystemService(Context.INPUT_METHOD_SERVICE);
            input.hideSoftInputFromWindow(editTextSearch.getWindowToken(), 0);
        }

        return super.dispatchTouchEvent(ev);
    }

    private String correctTypeTermForWebView(String type){
        switch(type) {
            case Constants.AGENTS_TRIGGERABLE:
                return Constants.BRIDGE_AGENTS_TRIGGERABLE;
            case Constants.AGENTS_ACTIONABLE:
                return Constants.BRIDGE_AGENTS_ACTIONABLE;
            case Constants.AGENTS_STATEFULL:
                return Constants.BRIDGE_AGENTS_STATEFULL;
        }
        return null;
    }

    @Override
    public void smoothScroll(final int pos) {
        if(recyclerview!=null){
        /*
            View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    recyclerview.smoothScrollToPosition(pos);//.scrollToPosition(pos);//
                    recyclerview.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                        @Override
                        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                            //nao faz mais scrolls
                        }
                    });
                }
            };

            recyclerview.addOnLayoutChangeListener(layoutChangeListener);
        */
        }
    }

    @Override
    public void onBackPressed() {
        cancelActivity();
        super.onBackPressed();
    }

    private void cancelActivity() {
        if (ACTION_SHORTCUT_CREATE.equals(getIntent().getAction())) {
            analyticsTracker.trackSimpleEvent(AnalyticsEvents.CREATE_SHORTCUT_CANCEL_EVENT);
        } else if (createGroup) {
            analyticsTracker.trackSimpleEvent(AnalyticsEvents.CREATE_GROUP_CANCEL_EVENT);
        }
    }
}
