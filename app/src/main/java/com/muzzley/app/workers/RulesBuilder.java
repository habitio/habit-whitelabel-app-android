package com.muzzley.app.workers;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

//import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.muzzley.App;
import com.muzzley.BuildConfig;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsEvents;
import com.muzzley.app.analytics.AnalyticsProperties;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.analytics.EventStatus;
import com.muzzley.app.tiles.ModelsStore;
import com.muzzley.app.tiles.TilesController;
import com.muzzley.model.workers.RuleUnitResponse;
import com.muzzley.model.workers.Worker;
import com.muzzley.model.workers.WorkerUnit;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.model.tiles.Tile;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.Utils;
import com.muzzley.util.picasso.CircleBorderTransform;
import com.muzzley.util.picasso.CircleTransform;
import com.muzzley.util.retrofit.UserService;
import com.muzzley.util.rx.RxComposers;
import com.muzzley.util.ui.ProgDialog;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

public class RulesBuilder extends AppCompatActivity {

    public static final String EXTRA_TYPE = BuildConfig.APPLICATION_ID + ".extra.type";
    public static final String EXTRA_SHOW_IN_WATCH = BuildConfig.APPLICATION_ID + ".extra.show_in_watch";

    public static final int TYPE_AGENT = 1;
    public static final int TYPE_SHORTCUT = 2;

    @Inject Gson gson;
    @Inject public PreferencesRepository preferencesRepository;
    @Inject public RulesBuilderController controller;
    WorkerInteractor workerInteractor = new WorkerInteractor();
    @Inject public TilesController tilesController;
    @Inject public UserService userService;
    @Inject ModelsStore modelsStore;
    @Inject AnalyticsTracker analyticsTracker;

    @BindView(R.id.edittext_worker_label) EditText workerLabel;
    @BindView(R.id.clean_text_agent_name) ImageView cleanName;

    @BindView(R.id.dinamic_agents_layout_trigger) LinearLayout dynamicLayoutTrigger;
    @BindView(R.id.dinamic_agents_layout_action) LinearLayout dynamicLayoutActions;
    @BindView(R.id.dinamic_agents_layout_state) LinearLayout dynamicLayoutStates;

    @BindView(R.id.linearLayout) LinearLayout mainHeader;

    @BindView(R.id.add_base_trigger) View bigPlusTrigger;
    @BindView(R.id.add_base_action) View bigPlusAction;
    @BindView(R.id.add_base_state) View bigPlusState;

    @BindView(R.id.bottom_white_block_cover) View bottomWhiteBlock;
    @BindView(R.id.worker_button_finish) Button finish;

    @BindView(R.id.layout_create_worker) ViewGroup container;

    @BindView(R.id.watch_options_container) View mWatchOptionsContainer;

    @BindView(R.id.chk_show_in_watch) AppCompatCheckBox chkShowInWatch;

    @BindView(R.id.top_white_block_cover) View topBlock;

    @BindView(R.id.new_agent_icon_1) ImageView icon;

    @BindView(R.id.styled_line) View styledLine;

    @BindView(R.id.scroll_container) View mScrollContainer;

    private List<RuleUnitResponse> ruleTrigger = new ArrayList<>();
    private List<RuleUnitResponse> ruleAction = new ArrayList<>();
    private List<RuleUnitResponse> ruleState = new ArrayList<>();

    private ArrayList<String> triggerTilesSelected = new ArrayList<>();
    private ArrayList<String> actionsTilesSelected = new ArrayList<>();
    private ArrayList<String> statesTilesSelected = new ArrayList<>();

    private String agentId;
    private String actionsJson;
    private String statesJson;
    private String exclude;

    private boolean isEditing;
    private boolean localDeleteAction;
    private boolean alreadyOpened;

    private boolean isComingBackFromDevicePicker;

    private int mBuilderType;
    private boolean mFirstLoad = true;
    Disposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_rules_builder);

        ButterKnife.bind(this);

        bigPlusTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agentAddNewRuleFrom(Constants.AGENTS_TRIGGERABLE, false);
            }
        });

        finish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (workerLabel.getText().length() <= 0) {
                    workerLabel.requestFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else
                    sendData();
            }
        });

        Bundle extra = getIntent().getExtras();
        boolean showInWatch = false;
        if(extra != null) {
            mBuilderType = extra.getInt(EXTRA_TYPE, TYPE_AGENT);
            isEditing = extra.getBoolean(Constants.EXTRA_AGENTS_IS_EDITING);
            showInWatch = extra.getBoolean(EXTRA_SHOW_IN_WATCH);
            agentId = extra.getString(Constants.EXTRA_AGENTS_ID);
        }

        if(mBuilderType == TYPE_SHORTCUT) {
            workerLabel.setHint(R.string.mobile_shortcut_add_name);

            dynamicLayoutTrigger.setVisibility(View.GONE);
            dynamicLayoutStates.setVisibility(View.GONE);
            bottomWhiteBlock.setVisibility(View.GONE);

            mainHeader.setVisibility(View.GONE);

            dynamicLayoutActions.addView(addLastActionItemPlus(Constants.AGENTS_ACTIONABLE, true, false));

            mWatchOptionsContainer.setVisibility(View.VISIBLE);

            topBlock.setBackgroundColor(Color.TRANSPARENT);

            icon.setImageResource(R.drawable.ic_arrow);
            icon.setColorFilter(Color.parseColor("#99A7AA"));

            chkShowInWatch.setChecked(showInWatch);

            styledLine.setVisibility(View.GONE);
        } else if(mBuilderType == TYPE_AGENT) {
            mScrollContainer.setBackgroundColor(Color.TRANSPARENT);
            workerLabel.setHint(R.string.mobile_worker_add_name);

            if (isEditing) {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.EDIT_ROUTINE_START_EVENT, agentId);
            } else {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.CREATE_ROUTINE_START_EVENT);
            }
        }

        configActionBar();

    }

    private void configActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            int titleId = 0;
            if (isEditing) {
                if (mBuilderType == TYPE_AGENT) {
                    titleId = R.string.mobile_worker_edit;
                } else if(mBuilderType == TYPE_SHORTCUT) {
                    titleId = R.string.mobile_shortcut_edit;
                }
            } else {
                if (mBuilderType == TYPE_AGENT) {
                    titleId = R.string.mobile_worker_add;
                } else if(mBuilderType == TYPE_SHORTCUT) {
                    titleId = R.string.mobile_shortcut_add;
                }
            }

            actionBar.setTitle(titleId);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        controller.onResume(this);

        Bundle bundle = getIntent().getExtras();

        //se tiver id, nao está a editar
        if(isEditing) {
            if(!alreadyOpened && !isComingBackFromDevicePicker) {
                agentId = bundle.getString(Constants.EXTRA_AGENTS_ID);
                String name = bundle.getString(Constants.EXTRA_AGENTS_NAME);
                String triggersJson = bundle.getString(Constants.EXTRA_AGENTS_TRIGGERABLE);
                actionsJson = bundle.getString(Constants.EXTRA_AGENTS_ACTIONABLE);
                statesJson = bundle.getString(Constants.EXTRA_AGENTS_STATEFULL);

                workerLabel.setText(name);
                cleanName.setVisibility(View.VISIBLE);
                cleanName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        workerLabel.setText("");
                        cleanName.setVisibility(View.INVISIBLE);
                    }
                });

                if(mBuilderType == TYPE_SHORTCUT) {
                    setResult(RESULT_CANCELED, new Intent().putExtra(Constants.EXTRA_AGENTS_ID, agentId));

                    if(!TextUtils.isEmpty(bundle.getString(Constants.DEVICE_PICKER_ID_INTENT_RESULT, ""))) {
                        actionsJson = bundle.getString(Constants.DEVICE_PICKER_ID_INTENT_RESULT);
                    }
                    updateRulesData(gson.fromJson(actionsJson, RuleUnitResponse.class));
                } else {
                    updateRulesData(gson.fromJson(triggersJson, RuleUnitResponse.class));
                }
            }
        } else {
            if(mFirstLoad && mBuilderType == TYPE_SHORTCUT) {
                mFirstLoad = false;

                if(bundle != null && !TextUtils.isEmpty(bundle.getString(Constants.DEVICE_PICKER_ID_INTENT_RESULT, ""))) {
                    actionsJson = bundle.getString(Constants.DEVICE_PICKER_ID_INTENT_RESULT);
                }

                RuleUnitResponse ruleUnitResponse = gson.fromJson(actionsJson, RuleUnitResponse.class);
                String previousTileId = "";
                for (WorkerUnit rule : ruleUnitResponse.getRuleUnit().getRules()) {
                    Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());
                    if (tile != null) {
                        if(!previousTileId.equals(tile.getId())) {
                            trackRuleAdd(ruleUnitResponse.getRuleUnit().getType(), tile.getProfile(), tile.getLabel());
                        }
                        previousTileId = tile.getId();
                    }
                }

                updateRulesData(gson.fromJson(actionsJson, RuleUnitResponse.class));
            }
        }


        Observable<CharSequence> searchTextObservable = RxTextView.textChanges(workerLabel);
        Observable<CharSequence> searchTextObservable2 =RxTextView.textChanges(workerLabel);

        searchTextObservable
                .debounce(500, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence onTextChangeEvent) {
                        updateFinishButtonLayout();
                    }
                });

        searchTextObservable2
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<CharSequence>() {
                    @Override
                    public void accept(CharSequence onTextChangeEvent) {
                        if (onTextChangeEvent.length() > 0) {
//                            Timber.d("OnTextChangeEvent NOT EMPTY");
                            cleanName.setVisibility(View.VISIBLE);
                            cleanName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    workerLabel.setText("");
                                    cleanName.setVisibility(View.INVISIBLE);
                                }
                            });
                        } else {
//                            Timber.d("OnTextChangeEvent EMPTY");
                            cleanName.setVisibility(View.INVISIBLE);
                            cleanName.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {

                                }
                            });
                        }
                        updateFinishButtonLayout();
                    }
                });

        updateFinishButtonLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                cancelActivity();
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void agentAddNewRuleFrom(String type, boolean multipleSelection) {
        Intent i = new Intent(this, DevicePickerActivity.class);

        String actionBarText, editTextHint, firstString;

        switch(type) {
            case Constants.AGENTS_TRIGGERABLE:
                actionBarText = getResources().getString(R.string.mobile_choose_device_vc_first_step);
                editTextHint = getResources().getString(R.string.mobile_search_trigger);
                firstString = getResources().getString(R.string.mobile_worker_edit);
                break;

            case Constants.AGENTS_ACTIONABLE:
                actionBarText = getResources().getString(R.string.mobile_choose_device_vc_second_step);
                if (mBuilderType == TYPE_SHORTCUT) {
                    if (isEditing) {
                        actionBarText = getString(R.string.mobile_shortcut_edit);
                    } else {
                        actionBarText = getString(R.string.mobile_shortcut_add);
                    }
                }
                editTextHint = getResources().getString(R.string.mobile_search_action);
                firstString = getResources().getString(R.string.mobile_worker_select_action);
                break;

            case Constants.AGENTS_STATEFULL:
                actionBarText = getResources().getString(R.string.mobile_choose_device_vc_third_step);
                editTextHint = getResources().getString(R.string.mobile_search_state);
                firstString = getResources().getString(R.string.agent_text_state_first_string);
                break;

            default:
                actionBarText = "Pick a device";
                editTextHint = "Search for a device";
                firstString = "Select a device";
        }

        i.putExtra(Constants.EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION, multipleSelection);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, actionBarText);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT, editTextHint);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_FIRST_STRING, firstString);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE, type);

        if(exclude != null && type.equals(Constants.AGENTS_STATEFULL)) {
            i.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_EXCLUDE, exclude);
        }

        if(type.equals(Constants.AGENTS_STATEFULL)){
            i.putStringArrayListExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_STATES_ALREADY, statesTilesSelected);
        }

        startActivityForResult(i, Constants.DEVICE_PICKER_ID_INTENT);

        // Analytics tracking
        String eventName = getAnalyticsEventByStateAndType(type);
        if (!TextUtils.isEmpty(eventName)) {
            if (isEditing) {
                String propertyName = "";
                if (mBuilderType == TYPE_AGENT) {
                    propertyName = AnalyticsProperties.ROUTINE_ID_PROPERTY;
                } else if (mBuilderType == TYPE_SHORTCUT) {
                    propertyName = AnalyticsProperties.SHORTCUT_ID_PROPERTY;
                }
                analyticsTracker.trackRuleAddOnEdit(eventName,
                        propertyName,
                        agentId);
            } else {
                analyticsTracker.trackSimpleEvent(eventName);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.DEVICE_PICKER_ID_INTENT && resultCode == Activity.RESULT_OK && data != null) {
            isComingBackFromDevicePicker = true;
            String result = data.getStringExtra(Constants.DEVICE_PICKER_ID_INTENT_RESULT);
            RuleUnitResponse ruleUnitResponse = gson.fromJson(result, RuleUnitResponse.class);
            String previousTileId = "";
            for (WorkerUnit rule : ruleUnitResponse.getRuleUnit().getRules()) {
                Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());
                if (tile != null) {
                    if (!previousTileId.equals(tile.getId())) {
                        trackRuleAdd(ruleUnitResponse.getRuleUnit().getType(), tile.getProfile(), tile.getLabel());
                    }
                    previousTileId = tile.getId();
                }
            }

            updateRulesData(gson.fromJson(result, RuleUnitResponse.class));
        }
    }

    private void updateRulesData(RuleUnitResponse rule) {
        switch(rule.getRuleUnit().getType()) {
            case Constants.BRIDGE_AGENTS_TRIGGERABLE:
                ruleTrigger.add(rule);
                createTriggerLayout();
                break;

            case Constants.BRIDGE_AGENTS_ACTIONABLE:
                ruleAction.add(rule);
                createActionLayout();
                break;

            case Constants.BRIDGE_AGENTS_STATEFULL:
                ruleState.add(rule);
                createStateLayout();
                break;
        }
    }

    private void createTriggerLayout() {
        dynamicLayoutTrigger.removeAllViews();
        triggerTilesSelected = new ArrayList<>();

        View child = getLayoutInflater().inflate(R.layout.layout_agent_new_trigger, dynamicLayoutTrigger, false);

        String profileId = ruleTrigger.get(0).getRuleUnit().getRules().get(0).getProfile();
        String channelId = ruleTrigger.get(0).getRuleUnit().getRules().get(0).getChannel();
        String component = ruleTrigger.get(0).getRuleUnit().getRules().get(0).getComponent();

        Tile t = modelsStore.getTileAgents(profileId, channelId, component);
        String s = String.format("Tile not found for prof: %s, channel: %s, compontnet: %s", profileId, channelId, component);
        Exception throwable = new Exception(s);
//        Crashlytics.log(s);
//        Crashlytics.logException(throwable);
        analyticsTracker.trackThrowable(throwable);

        exclude = t.getId();
        triggerTilesSelected.add(t.getId());

        ((TextView) child.findViewById(R.id.children_title)).setText(t.getLabel());
        ((TextView) child.findViewById(R.id.children_description)).setText(ruleTrigger.get(0).getRuleUnit().getRules().get(0).getLabel());
        bindImage(this, t.getPhotoUrlAlt(), ((ImageView) child.findViewById(R.id.device_image)));

        final ImageView deleteTrigger = (ImageView) child.findViewById(R.id.device_delete);
        deleteTrigger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exclude = null;
                deleteTriggerAndUpdateLayout();
            }
        });

        dynamicLayoutTrigger.addView(child);

        createActionLayout();

        if(isEditing && !isComingBackFromDevicePicker){
            updateRulesData(gson.fromJson(actionsJson, RuleUnitResponse.class));
        }
        updateFinishButtonLayout();
    }

    private void deleteTriggerAndUpdateLayout() {
        if (!ruleTrigger.isEmpty() &&  !ruleTrigger.get(0).getRuleUnit().getRules().isEmpty()) {
            WorkerUnit rule = ruleTrigger.get(0).getRuleUnit().getRules().get(0);
            Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());
            trackTriggerRuleDelete(tile.getProfile(), tile.getLabel());
        }

        ruleTrigger = new ArrayList<>();
        dynamicLayoutTrigger.removeAllViews();

        if (ruleAction.isEmpty()) {
            dynamicLayoutActions.removeAllViews();
            dynamicLayoutStates.removeAllViews();

            ((TextView) bigPlusTrigger.findViewById(R.id.worker_add_new_item_text)).setText("");
            backgroundTransparentAndHideLine(bigPlusTrigger);

            dynamicLayoutTrigger.addView(bigPlusTrigger);
        } else {
            if (!ruleState.isEmpty()) {
                ruleState = new ArrayList<>();
                createStateLayout();
            }

            dynamicLayoutTrigger.addView(bigPlusTrigger);
        }
    }

    private void createActionLayout() {
        //remove the add button and the line
        dynamicLayoutActions.removeAllViews();

        if(ruleAction.isEmpty()) {
            if(ruleState.isEmpty()) {
                dynamicLayoutStates.removeAllViews();
            } else {
                if(mBuilderType != TYPE_SHORTCUT) {
                    View headerAction = getLayoutInflater().inflate(R.layout.layout_agent_new_parent_header, dynamicLayoutActions, false);
                    ((TextView) headerAction.findViewById(R.id.agent_header_text)).setText(R.string.mobile_choose_device_vc_second_step);
                    dynamicLayoutActions.addView(headerAction);
                }
            }

            if (mBuilderType == TYPE_SHORTCUT) {
                dynamicLayoutActions.addView(addLastActionItemPlus(Constants.AGENTS_ACTIONABLE, true, false,
                        getString(R.string.mobile_device_add)));
            } else {

                if (modelsStore.models.anythingActionable()) {
                    bigPlusAction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            agentAddNewRuleFrom(Constants.AGENTS_ACTIONABLE, true);
                        }
                    });
                } else {
//                    bigPlusAction.findViewById(R.id.worker_add_new_item_error_text).setVisibility(View.VISIBLE);
                    ((TextView) bigPlusAction.findViewById(R.id.worker_add_new_item_error_text)).setText(R.string.mobile_worker_no_actionable_devices);
                    ((ImageButton) bigPlusAction.findViewById(R.id.agent_add_new_item)).setEnabled(false);
                }

                bigPlusAction.setVisibility(View.VISIBLE);
                ((TextView) bigPlusAction.findViewById(R.id.worker_add_new_item_text)).setText(R.string.mobile_choose_device_vc_second_step);

                bigPlusAction.findViewById(R.id.worker_add_new_item_text).setVisibility(View.VISIBLE);
                backgroundTransparentAndHideLine(bigPlusAction);
                dynamicLayoutActions.addView(createSeparatorLine());
                dynamicLayoutActions.addView(bigPlusAction);
            }

        } else {

            if(mBuilderType != TYPE_SHORTCUT) {
                View headerAction = getLayoutInflater().inflate(R.layout.layout_agent_new_parent_header, dynamicLayoutActions, false);
                ((TextView) headerAction.findViewById(R.id.agent_header_text)).setText(R.string.mobile_choose_device_vc_second_step);
                dynamicLayoutActions.addView(headerAction);
            }

            List<String> idsAlreadyAdded = new ArrayList<>();

            for (RuleUnitResponse ruResponse : ruleAction) {
                for (int i = 0; i < ruResponse.getRuleUnit().getRules().size(); i++) {
                    String profileId = ruResponse.getRuleUnit().getRules().get(i).getProfile();
                    String channelId = ruResponse.getRuleUnit().getRules().get(i).getChannel();
                    String component = ruResponse.getRuleUnit().getRules().get(i).getComponent();
                    String label = ruResponse.getRuleUnit().getRules().get(i).getLabel().trim().toLowerCase();
                    String localActionRuleId = profileId + channelId + component + label;

                    Tile tile = modelsStore.getTileAgents(profileId, channelId, component);

                    //se e a primeira regra do tile, adiciona
                    // vai adicionando a list que ajuda a criar o layout, idsAlreadyAdded, tile ids
                    if (tile == null) {
//                        Toast.makeText(this,"invalid "+localActionRuleId,Toast.LENGTH_LONG).show();
                    } else
                    if (!idsAlreadyAdded.contains(tile.getId())) {
                        idsAlreadyAdded.add(tile.getId());

                        View actionsView = getLayoutInflater().inflate(R.layout.layout_agent_new_action, dynamicLayoutActions, false);
                        TextView actionContent = (TextView) actionsView.findViewById(R.id.children_title);
                        LinearLayout llActions = (LinearLayout) actionsView.findViewById(R.id.dinamic_actions_from_channel);

                        final String tileId = tile.getId();
                        actionsView.setTag(tileId);

                        actionContent.setText(tile.getLabel());
                        bindImage(this, tile.getPhotoUrlAlt(), (ImageView) actionsView.findViewById(R.id.device_image));

                        View actions = getLayoutInflater().inflate(R.layout.layout_agent_new_action_actions, llActions, false);
                        View smallLine = actions.findViewById(R.id.small_action_line);
                        smallLine.setVisibility(View.GONE);
                        TextView actionLabel = (TextView) actions.findViewById(R.id.img_subaction_label);
                        actionLabel.setText(ruResponse.getRuleUnit().getRules().get(i).getLabel());

                        actions.setTag(localActionRuleId);
                        actions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                localDeleteAction = true;
                                deleteIndividualAction(v.getTag().toString());
                            }
                        });
                        llActions.addView(actions);

                        final ImageView deleteTrigger = (ImageView) actionsView.findViewById(R.id.device_delete);
                        deleteTrigger.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                localDeleteAction = true;
                                deleteActionAndUpdateLayout(tileId);
                            }
                        });

                        dynamicLayoutActions.addView(actionsView);
                        dynamicLayoutActions.addView(createSeparatorLine());
                    } else {
                        View actionsView = dynamicLayoutActions.findViewWithTag(tile.getId());
                        LinearLayout llActions = (LinearLayout) actionsView.findViewById(R.id.dinamic_actions_from_channel);

                        View actions = getLayoutInflater().inflate(R.layout.layout_agent_new_action_actions, llActions, false);
                        TextView actionLabel = (TextView) actions.findViewById(R.id.img_subaction_label);
                        actionLabel.setText(ruResponse.getRuleUnit().getRules().get(i).getLabel());

                        actions.setTag(localActionRuleId);
                        actions.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                localDeleteAction = true;
                                deleteIndividualAction(v.getTag().toString());
                            }
                        });

                        llActions.addView(actions);
                    }
                }
            }

            if (mBuilderType == TYPE_SHORTCUT) {
                dynamicLayoutActions.addView(addLastActionItemPlus(Constants.AGENTS_ACTIONABLE, true, false,
                        getString(R.string.mobile_device_add)));
            } else {
                dynamicLayoutActions.addView(addLastActionItemPlus(Constants.AGENTS_ACTIONABLE, true, false));
            }

            if(mBuilderType != TYPE_SHORTCUT) {
                bigPlusState.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        agentAddNewRuleFrom(Constants.AGENTS_STATEFULL, true);
                    }
                });

                bigPlusState.setVisibility(View.VISIBLE);
                ((TextView) bigPlusState.findViewById(R.id.worker_add_new_item_text)).setText(R.string.mobile_choose_device_vc_third_step);
                backgroundToWhiteAndShowLine(bigPlusState);

                dynamicLayoutStates.removeAllViews();
                dynamicLayoutStates.addView(bigPlusState);
            }

            if (mBuilderType != TYPE_SHORTCUT && isEditing && !alreadyOpened && !localDeleteAction && !isComingBackFromDevicePicker) {
                updateRulesData(gson.fromJson(statesJson, RuleUnitResponse.class));
                alreadyOpened = true;
            }

            if(!ruleState.isEmpty() && isComingBackFromDevicePicker){
                createStateLayout();
            }

            if (localDeleteAction) {
                if (!ruleState.isEmpty() && ruleState.get(0).getRuleUnit().getRules() != null && !ruleState.get(0).getRuleUnit().getRules().isEmpty()) {
                    createStateLayout();
                }
            }
        }
        updateFinishButtonLayout();
    }

    private void deleteIndividualAction(String tagActionId) {
        for(RuleUnitResponse ruResponse : ruleAction) {

            Iterator<WorkerUnit> rulesIterator = ruResponse.getRuleUnit().getRules().iterator();
            String profileId = "";
            String deviceName = "";
            while(rulesIterator.hasNext()) {
                WorkerUnit rule = rulesIterator.next();
                String label = rule.getLabel().trim().toLowerCase();

                Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());

                if(tagActionId.equals(rule.getProfile() + rule.getChannel() + rule.getComponent() + label)){
                    rulesIterator.remove();

                    profileId = tile.getProfile();
                    deviceName = tile.getLabel();
                }
            }

            if (!TextUtils.isEmpty(profileId)) {
                trackActionSubRuleDelete(profileId, deviceName);
            }
        }

        //clean rule if empty
        Iterator<RuleUnitResponse> ruleAc = ruleAction.iterator();
        while(ruleAc.hasNext()) {
            RuleUnitResponse ruleU = ruleAc.next();

            if(ruleU.getRuleUnit().getRules().isEmpty()) {
                ruleAc.remove();
            }
        }

        createActionLayout();
    }

    private void deleteActionAndUpdateLayout(String tileIdToDelete) {
        for(RuleUnitResponse ruResponse : ruleAction) {
            Iterator<WorkerUnit> rulesIterator = ruResponse.getRuleUnit().getRules().iterator();
            String profileId = "";
            String deviceName = "";
            while(rulesIterator.hasNext()) {
                WorkerUnit rule = rulesIterator.next();

                Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());

                if(tile.getId().equals(tileIdToDelete)) {
                    profileId = tile.getProfile();
                    deviceName = tile.getLabel();

                    rulesIterator.remove();
                }
            }
            if (!TextUtils.isEmpty(profileId)) {
                trackActionRuleDelete(profileId, deviceName);
            }
        }

        //clean rule if empty
        Iterator<RuleUnitResponse> ruleAc = ruleAction.iterator();
        while(ruleAc.hasNext()) {
            RuleUnitResponse ruleU = ruleAc.next();

            if(ruleU.getRuleUnit().getRules().isEmpty()) {
                ruleAc.remove();
            }
        }

        createActionLayout();
    }

    private void createStateLayout() {
        int maxStateSize = modelsStore.modelsStates.getTilesData().tiles.size();

        for(Tile tileAux : modelsStore.modelsStates.getTilesData().tiles) {
            if(triggerTilesSelected.contains(tileAux.getId())){
                maxStateSize--;
            }
        }

        boolean isLastItem = false;

        localDeleteAction = false;
        dynamicLayoutStates.removeAllViews();

        View headerAction = getLayoutInflater().inflate(R.layout.layout_agent_new_parent_header, dynamicLayoutStates, false);
        ((TextView) headerAction.findViewById(R.id.agent_header_text)).setText(R.string.mobile_choose_device_vc_third_step);
        dynamicLayoutStates.addView(headerAction);

        statesTilesSelected = new ArrayList<>();

        for(RuleUnitResponse ruResponse : ruleState) {
            for (int i = 0; i < ruResponse.getRuleUnit().getRules().size(); i++) {
                View child = getLayoutInflater().inflate(R.layout.layout_agent_new_trigger, dynamicLayoutStates, false);

                String profileId = ruResponse.getRuleUnit().getRules().get(i).getProfile();
                String channelId = ruResponse.getRuleUnit().getRules().get(i).getChannel();
                String componentId = ruResponse.getRuleUnit().getRules().get(i).getComponent();
                String label = ruResponse.getRuleUnit().getRules().get(i).getLabel().trim().toLowerCase();
                String localActionRuleId = profileId + channelId + componentId + label;

                Tile t = modelsStore.getTileAgents(profileId, channelId, componentId);

                statesTilesSelected.add(t.getId());

                if(statesTilesSelected.size() == maxStateSize) {
                    isLastItem = true;
                }

                ((TextView) child.findViewById(R.id.children_title)).setText(t.getLabel());
                ((TextView) child.findViewById(R.id.children_description)).setText(ruResponse.getRuleUnit().getRules().get(i).getLabel());
                bindImageWithoutCircle(this, t.getPhotoUrlAlt(), ((ImageView) child.findViewById(R.id.device_image)));

                final ImageView deleteTrigger = (ImageView) child.findViewById(R.id.device_delete);
                deleteTrigger.setTag(localActionRuleId);
                deleteTrigger.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteStateAndUpdateLayout(v.getTag().toString());
                    }
                });

                if(!isLastItem) {
                    dynamicLayoutStates.addView(child);
                    dynamicLayoutStates.addView(createSeparatorLine());
                } else {
                    (child.findViewById(R.id.top_line)).setVisibility(View.VISIBLE);
                    child.findViewById(R.id.layout_agent_item_parent).setBackgroundColor(ContextCompat.getColor(this, R.color.white));
                    dynamicLayoutStates.addView(child);
                }
            }
        }

        if(ruleState.isEmpty() || (ruleState.get(ruleState.size()-1)!=null && ruleState.get(ruleState.size() - 1).getRuleUnit().getRules().isEmpty())){
            bigPlusState.setVisibility(View.VISIBLE);
            ((TextView) bigPlusState.findViewById(R.id.worker_add_new_item_text)).setText(R.string.mobile_choose_device_vc_third_step);
            backgroundToWhiteAndShowLine(bigPlusState);
            dynamicLayoutStates.addView(bigPlusState);
        } else {
            Timber.e("statesTilesSelected.size(): " + statesTilesSelected.size() + "; maxStateSize: " + maxStateSize);
            if(!isLastItem) {
                dynamicLayoutStates.addView(addLastActionItemPlus(Constants.AGENTS_STATEFULL, false, true));
            }
        }
        updateFinishButtonLayout();
    }

    private void deleteStateAndUpdateLayout(String ruleStateId) {
        for(RuleUnitResponse ruResponse : ruleState) {

            Iterator<WorkerUnit> rulesIterator = ruResponse.getRuleUnit().getRules().iterator();
            String profileId = "";
            String deviceName = "";
            while(rulesIterator.hasNext()) {
                WorkerUnit rule = rulesIterator.next();
                String label = rule.getLabel().trim().toLowerCase();
                String localActionRuleId = rule.getProfile() + rule.getChannel() + rule.getComponent() + label;

                if(ruleStateId.equals(localActionRuleId)){
                    Tile tile = modelsStore.getTileAgents(rule.getProfile(), rule.getChannel(), rule.getComponent());
                    profileId = tile.getProfile();
                    deviceName = tile.getLabel();

                    rulesIterator.remove();
                }
            }
            if (!TextUtils.isEmpty(profileId)) {
                trackButOnlyIfRuleDelete(profileId, deviceName);
            }
        }

        Iterator<RuleUnitResponse> ruleAc = ruleState.iterator();
        while(ruleAc.hasNext()) {
            RuleUnitResponse ruleU = ruleAc.next();

            if(ruleU.getRuleUnit().getRules().isEmpty()) {
                ruleAc.remove();
            }
        }

        createStateLayout();
    }

    public void updateFinishButtonLayout() {
        if(mBuilderType == TYPE_SHORTCUT) {
            if(//workerLabel.getText().length() > 0 &&
                    !ruleAction.isEmpty()
                    && !ruleAction.get(0).getRuleUnit().getRules().isEmpty()) {

                finish.setEnabled(true);
            } else {
                finish.setEnabled(false);
            }
        } else {
            if(workerLabel != null
//                    && !workerLabel.getText().toString().isEmpty()
                    && !ruleTrigger.isEmpty()
                    && !ruleTrigger.get(ruleTrigger.size() - 1).getRuleUnit().getRules().isEmpty()
                    && !ruleAction.isEmpty()
                    && !ruleAction.get(ruleAction.size() - 1).getRuleUnit().getRules().isEmpty()) {

                finish.setEnabled(true);
            } else {
                finish.setEnabled(false);
            }
        }
    }

    /**
     * add the last item in layout, a plus signal to add more actions
     * the special layout change the background color and show the line, this case is for last items
     */
    private View addLastActionItemPlus(final String actionPlusButton, final boolean multipleSelection, boolean specialLayout) {
        return addLastActionItemPlus(actionPlusButton, multipleSelection, specialLayout, null);
    }

    private View addLastActionItemPlus(final String actionPlusButton, final boolean multipleSelection,
                                       boolean specialLayout, String buttonTitle) {
        View view = getLayoutInflater().inflate(R.layout.layout_agent_new_action_plus_button, null);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agentAddNewRuleFrom(actionPlusButton, multipleSelection);
            }
        });

        if(specialLayout) {
            backgroundToWhiteAndShowLine(view);
        } else {
            backgroundTransparentAndHideLine(view);
        }

        if(!TextUtils.isEmpty(buttonTitle)) {
            ((TextView)view.findViewById(R.id.children_title)).setText(buttonTitle);
        }

        //bindImageFromResource(getActivity(), R.drawable.blue_plus, addMoreActions, 20);
        return view;
    }

    public void backgroundToWhiteAndShowLine(View view) {
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.white));
        view.findViewById(R.id.top_line).setVisibility(View.VISIBLE);
        bottomWhiteBlock.setVisibility(View.VISIBLE);
    }

    public void backgroundTransparentAndHideLine(View view) {
        view.setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        view.findViewById(R.id.top_line).setVisibility(View.INVISIBLE);
        bottomWhiteBlock.setVisibility(View.INVISIBLE);
    }

    private void bindImage(Context context, String imgUrl, ImageView iv){
        Picasso.get()
                .load(TextUtils.isEmpty(imgUrl) ? null : imgUrl)
                .fit()
                .transform(new CircleBorderTransform(context))
                .into(iv);
    }

    private void bindImageWithoutCircle(Context context, String imgUrl, ImageView iv) {
        Picasso.get()
                .load(imgUrl)
                .transform(new CircleTransform())
                .into(iv);
    }

    public View createSeparatorLine() {
        View v = new View(this);
        v.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) Utils.dpToPx(this, 1)));
        v.setBackgroundColor(Color.parseColor("#CCCDD7D9"));
        return v;
    }

    boolean isNullOrEmpty(Collection col) {
        return col == null || col.isEmpty();
    }
    boolean isValid(Worker worker) {
        if(TextUtils.isEmpty(worker.getUser()) || TextUtils.isEmpty(worker.getLabel())
            || isNullOrEmpty(worker.getTriggers()) || isNullOrEmpty(worker.getActions()) ) {
            return false;
        }

        return true;
    }


    private void sendData() {
        if(mBuilderType == TYPE_AGENT) {
            Worker worker = workerInteractor.buildWorkerToSubmit(preferencesRepository.getUser().getId(), workerLabel.getText().toString(), ruleTrigger, ruleAction, ruleState);

            finish.setEnabled(false);
//            if (newAgent.isValid()) {
            if (isValid(worker)) {
                if(!isEditing) {
                    disposable = userService.createWorker(worker)
                        .compose(RxComposers.<Worker>applyIoRefresh(ProgDialog.getLoader(this)))
//                        .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                            new Consumer<Worker>() {
                                @Override
                                public void accept(Worker worker) {
                                    successSendData(gson.toJsonTree(worker).toString());
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    Timber.e(throwable, "Retrofit Error");
                                    failSendData();
                                }
                            }
                    );
                } else {
                    worker.setId(agentId);
                    disposable = userService.editWorker(agentId,worker)
                        .andThen(userService.getWorker(agentId))
                        .compose(RxComposers.<Worker>applyIoRefresh(ProgDialog.getLoader(this)))
                        .subscribe(
                            new Consumer<Worker>() {
                                @Override
                                public void accept(Worker worker) {
                                    successSendData(gson.toJsonTree(worker).toString());
                                }
                            },
                            new Consumer<Throwable>() {
                                @Override
                                public void accept(Throwable throwable) {
                                    failSendData();
                                }
                            }
                    );
                }
            } else {
                Timber.w("INVALID AGENT! data missing");
                FeedbackMessages.showMessage(container, "Some data is missing.");
                finish.setEnabled(true);
            }
        } else if(mBuilderType == TYPE_SHORTCUT) {
            Shortcut shortcut = controller.buildShortcut(workerLabel.getText().toString(),
                    chkShowInWatch.isChecked(),
                    ruleAction);

            if(isEditing) {
                shortcut.setId(agentId);
                controller.editShortcut(shortcut);
            } else {
                controller.createShortcut(shortcut);
            }
        }
    }

    public void successSendData(String resultJson) {
        finish.setEnabled(true);
        Intent output = new Intent();
        output.putExtra(Constants.EXTRA_NEW_AGENTS_JSON, resultJson);
        output.putExtra(Constants.EXTRA_NEW_AGENTS_EDITING, isEditing);

        if (mBuilderType == TYPE_SHORTCUT) {
            Shortcut shortcut = new Gson().fromJson(resultJson, Shortcut.class);

            output.setAction(Constants.SHORTCUT_CHANGE_EVENT);
            analyticsTracker.trackShortcutAction(
                    isEditing ? AnalyticsEvents.EDIT_SHORTCUT_FINISH_EVENT : AnalyticsEvents.CREATE_SHORTCUT_FINISH_EVENT,
                    shortcut.getId(),
                    EventStatus.Success,
                    "Success");

        } else if (mBuilderType == TYPE_AGENT) {
            Worker worker = new Gson().fromJson(resultJson, Worker.class);
            analyticsTracker.trackRoutineAction(
                    isEditing ? AnalyticsEvents.EDIT_ROUTINE_FINISH_EVENT : AnalyticsEvents.CREATE_ROUTINE_FINISH_EVENT,
                    worker.getId(),
                    EventStatus.Success,
                    "Success");
        }
        
        setResult(RESULT_OK, output);
        finish();
    }

    public void failSendData() {
        int messageId = 0;
        if(isEditing) {
            if (mBuilderType == TYPE_AGENT) {
                messageId = R.string.mobile_worker_edit_text;
            } else if (mBuilderType == TYPE_SHORTCUT) {
                messageId = R.string.mobile_shortcut_edit_text;
            }
        } else {
            if (mBuilderType == TYPE_AGENT) {
                messageId = R.string.mobile_worker_edit_text;
            } else if (mBuilderType == TYPE_SHORTCUT) {
                messageId = R.string.mobile_shortcut_edit_text;
            }
        }

        FeedbackMessages.showMessage(container, messageId);
        finish.setEnabled(true);

        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.EDIT_ROUTINE_FINISH_EVENT,
                        agentId,
                        EventStatus.Error,
                        getString(messageId));
            } else {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.CREATE_ROUTINE_FINISH_EVENT,
                        EventStatus.Error,
                        getString(messageId));
            }
        } else if (mBuilderType == TYPE_SHORTCUT) {
            if (isEditing) {
                analyticsTracker.trackShortcutAction(AnalyticsEvents.EDIT_SHORTCUT_FINISH_EVENT,
                        agentId,
                        EventStatus.Error,
                        getString(messageId));
            } else {
                analyticsTracker.trackShortcutAction(AnalyticsEvents.CREATE_SHORTCUT_FINISH_EVENT,
                        chkShowInWatch.isChecked() ? "Android Wear" : "App",
                        EventStatus.Error,
                        getString(messageId));
            }
        }
    }

    @Override
    public void onBackPressed() {
        cancelActivity();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (disposable != null){
            disposable.dispose();
        }
    }

    private void cancelActivity() {
        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.EDIT_ROUTINE_CANCEL_EVENT,
                        agentId);
            } else {
                analyticsTracker.trackRoutineAction(AnalyticsEvents.CREATE_ROUTINE_CANCEL_EVENT);
            }
        }
    }

    private String getAnalyticsEventByStateAndType(String type) {
        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                switch (type) {
                    case Constants.AGENTS_TRIGGERABLE:
                        return AnalyticsEvents.EDIT_ROUTINE_ADD_TRIGGER_START;

                    case Constants.AGENTS_ACTIONABLE:
                        return AnalyticsEvents.EDIT_ROUTINE_ADD_ACTION_START;

                    case Constants.AGENTS_STATEFULL:
                        return AnalyticsEvents.EDIT_ROUTINE_ADD_BUT_ONLY_IF_START;
                }
            } else {
                switch (type) {
                    case Constants.AGENTS_TRIGGERABLE:
                        return AnalyticsEvents.CREATE_ROUTINE_ADD_TRIGGER_START;

                    case Constants.AGENTS_ACTIONABLE:
                        return AnalyticsEvents.CREATE_ROUTINE_ADD_ACTION_START;

                    case Constants.AGENTS_STATEFULL:
                        return AnalyticsEvents.CREATE_ROUTINE_ADD_BUT_ONLY_IF_START;
                }
            }
        } else if (mBuilderType == TYPE_SHORTCUT) {
            if (isEditing) {
                return AnalyticsEvents.EDIT_SHORTCUT_ADD_ACTION_START;
            } else {
                return AnalyticsEvents.CREATE_SHORTCUT_ADD_ACTION_START;
            }
        }

        return null;
    }

    private void trackTriggerRuleDelete(String profileId, String deviceName) {
        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.EDIT_ROUTINE_DELETE_TRIGGER_FINISH,
                        AnalyticsProperties.ROUTINE_ID_PROPERTY, agentId, profileId, deviceName);
            } else {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.CREATE_ROUTINE_DELETE_TRIGGER_FINISH,
                        profileId, deviceName);
            }
        }
    }

    private void trackActionRuleDelete(String profileId, String deviceName) {
        if (isEditing) {
            String eventName = "";
            String idPropertyName = "";
            if (mBuilderType == TYPE_AGENT) {
                eventName = AnalyticsEvents.EDIT_ROUTINE_DELETE_ACTION_FINISH;
                idPropertyName = AnalyticsProperties.ROUTINE_ID_PROPERTY;
            } else if (mBuilderType == TYPE_SHORTCUT) {
                eventName = AnalyticsEvents.EDIT_SHORTCUT_DELETE_ACTION_FINISH;
                idPropertyName = AnalyticsProperties.SHORTCUT_ID_PROPERTY;
            }
            analyticsTracker.trackRuleDelete(eventName, idPropertyName,
                    agentId, profileId, deviceName);
        } else {
            String eventName = "";
            if (mBuilderType == TYPE_AGENT) {
                eventName = AnalyticsEvents.CREATE_ROUTINE_DELETE_ACTION_FINISH;
            } else if (mBuilderType == TYPE_SHORTCUT) {
                eventName = AnalyticsEvents.CREATE_SHORTCUT_DELETE_ACTION_FINISH;
            }
            analyticsTracker.trackRuleDelete(eventName, profileId, deviceName);
        }
    }

    private void trackActionSubRuleDelete(String profileId, String deviceName) {
        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.EDIT_ROUTINE_DELETE_RULE_ACTION_FINISH,
                        AnalyticsProperties.ROUTINE_ID_PROPERTY, agentId, profileId, deviceName);
            } else {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.CREATE_ROUTINE_DELETE_RULE_ACTION_FINISH,
                        profileId, deviceName);
            }
        } else if(mBuilderType == TYPE_SHORTCUT) {
            if (isEditing) {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.EDIT_SHORTCUT_DELETE_RULE_ACTION_FINISH,
                        AnalyticsProperties.SHORTCUT_ID_PROPERTY, agentId, profileId, deviceName);
            } else {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.CREATE_SHORTCUT_DELETE_RULE_ACTION_FINISH,
                        profileId, deviceName);
            }
        }
    }

    private void trackButOnlyIfRuleDelete(String profileId, String deviceName) {
        if (mBuilderType == TYPE_AGENT) {
            if (isEditing) {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.EDIT_ROUTINE_DELETE_BUT_ONLY_IF_FINISH,
                        AnalyticsProperties.ROUTINE_ID_PROPERTY, agentId, profileId, deviceName);
            } else {
                analyticsTracker.trackRuleDelete(AnalyticsEvents.CREATE_ROUTINE_DELETE_BUT_ONLY_IF_FINISH,
                        profileId, deviceName);
            }
        }
    }

    private void trackRuleAdd(String type, String profileId, String deviceName) {
        String eventName = "";

        if (isEditing) {
            switch (type) {
                case Constants.BRIDGE_AGENTS_TRIGGERABLE:
                    eventName = AnalyticsEvents.EDIT_ROUTINE_ADD_TRIGGER_FINISH;
                    break;

                case Constants.BRIDGE_AGENTS_ACTIONABLE:
                    if (mBuilderType == TYPE_AGENT) {
                        eventName = AnalyticsEvents.EDIT_ROUTINE_ADD_ACTION_FINISH;
                    } else if(mBuilderType == TYPE_SHORTCUT) {
                        eventName = AnalyticsEvents.EDIT_SHORTCUT_ADD_ACTION_FINISH;
                    }
                    break;

                case Constants.BRIDGE_AGENTS_STATEFULL:
                    eventName = AnalyticsEvents.EDIT_ROUTINE_ADD_BUT_ONLY_IF_FINISH;
                    break;
            }
            analyticsTracker.trackRuleAdd(eventName, agentId, profileId, deviceName);
        } else {
            switch (type) {
                case Constants.BRIDGE_AGENTS_TRIGGERABLE:
                    eventName = AnalyticsEvents.CREATE_ROUTINE_ADD_TRIGGER_FINISH;
                    break;

                case Constants.BRIDGE_AGENTS_ACTIONABLE:
                    if (mBuilderType == TYPE_AGENT) {
                        eventName = AnalyticsEvents.CREATE_ROUTINE_ADD_ACTION_FINISH;
                    } else if(mBuilderType == TYPE_SHORTCUT) {
                        eventName = AnalyticsEvents.CREATE_SHORTCUT_ADD_ACTION_FINISH;
                    }
                    break;

                case Constants.BRIDGE_AGENTS_STATEFULL:
                    eventName = AnalyticsEvents.CREATE_ROUTINE_ADD_BUT_ONLY_IF_FINISH;
                    break;
            }
            analyticsTracker.trackRuleAdd(eventName, profileId, deviceName);
        }
    }

}
