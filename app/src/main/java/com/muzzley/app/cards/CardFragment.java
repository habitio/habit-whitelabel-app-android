 package com.muzzley.app.cards;

 import android.app.Activity;
 import android.content.Intent;
 import android.os.Bundle;
 import android.os.Handler;
 import android.os.Looper;
 import androidx.annotation.Nullable;
 import com.google.android.material.tabs.TabLayout;
 import androidx.fragment.app.Fragment;
 import androidx.core.app.NotificationManagerCompat;
 import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
 import androidx.recyclerview.widget.LinearLayoutManager;
 import androidx.recyclerview.widget.RecyclerView;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.ViewFlipper;

// import com.crashlytics.android.Crashlytics;
 import com.muzzley.App;
 import com.muzzley.Constants;
 import com.muzzley.R;
 import com.muzzley.app.HomeActivity;
 import com.muzzley.app.Refresh;
 import com.muzzley.app.workers.DevicePickerActivity;
 import com.muzzley.app.shortcuts.ShortcutVM;
 import com.muzzley.app.shortcuts.ShortcutsActivity2;
 import com.muzzley.app.shortcuts.ShortcutsInteractor;
 import com.muzzley.app.shortcuts.ShortcutsPresenter;
 import com.muzzley.app.tiles.Models;
 import com.muzzley.model.cards.Card;
 import com.muzzley.model.shortcuts.Shortcut;
 import com.muzzley.providers.BusProvider;
 import com.muzzley.services.LocationInteractor;
 import com.muzzley.services.PreferencesRepository;
 import com.muzzley.util.FeedbackMessages;
 import com.muzzley.util.ui.ShowcaseBuilder;
 import com.muzzley.util.ui.ViewModelAdapter;
 import com.squareup.otto.Subscribe;

 import java.util.ArrayList;
 import java.util.List;

 import javax.inject.Inject;

 import butterknife.BindView;
 import butterknife.ButterKnife;
 import io.reactivex.functions.Consumer;
 import io.reactivex.subjects.PublishSubject;
 import timber.log.Timber;

/**
 * Created by caan on 22-09-2015.
 */
public class CardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    //HACK because of ActivityCompat.startActivityForResult, this method doesn't call fragment onActivityResult
    public static boolean isShortcutChanged = false;

    public static final int REQ = 113;
    private static final int MAPREQ = 114;
    private static final int SHORTCUT_INSERT = 200;

    @Inject CardsController cardsController;
    @Inject PreferencesRepository preferencesRepository;
    @Inject ShortcutsInteractor shortcutsInteractor;
    @Inject ShortcutsPresenter shortcutsPresenter;
    @Inject LocationInteractor locationInteractor;

    @BindView(R.id.shortcuts_cell) View shortcutsCell;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.layout_swipe_refresh) SwipeRefreshLayout swipeRefresh;
    @BindView(R.id.view_flipper) ViewFlipper viewFlipper;
    @BindView(R.id.new_cards) View newCards;
    @BindView(R.id.clear) View clearBtn;
    @BindView(R.id.btn_show_more) Button showMore;

    @BindView(R.id.shortcut_list) RecyclerView shortcutsList;
    @BindView(R.id.button_blank_state) Button tryAgain;

//    private ContainerAdapter<ShortcutVM> shortcutsAdapter;
    private ViewModelAdapter<ShortcutVM> shortcutsAdapter;
    private PublishSubject<ShortcutVM> executeRx;

    enum ViewState { DATA, LOADING, BLANK,  ERROR }

    private ContainerAdapter<Card> cardsAdapter;
    private List<Card> cards;
    ArrayList<ShortcutVM> shortcutVMS;

    boolean shortcuts;
    View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Timber.d("onCreateView");
        view = inflater.inflate(R.layout.fragment_cards_shortcuts, container, false);
        ButterKnife.bind(this, view);
        shortcuts = !getResources().getBoolean(R.bool.disable_shortcuts);
//        shortcuts = true;
        if ( !shortcuts) {
            shortcutsCell.setVisibility(View.GONE);
        }
        showMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(),ShortcutsActivity2.class));

                //deal with animations later
//                Intent intent = new Intent(getActivity(), ShortcutsActivity.class);
//                ActivityOptionsCompat options = ActivityOptionsCompat
//                        .makeSceneTransitionAnimation(getActivity(), viewHolder.itemView.findViewById(R.id.container_actions), "actions");
//                ActivityCompat.startActivity(getActivity(), intent, options.toBundle());
            }
        });
        executeRx = PublishSubject.create();
        executeRx.subscribe(new Consumer<ShortcutVM>() {
            @Override
            public void accept(ShortcutVM shortcutVM) throws Exception {
                if (shortcutVM.getShortcut() == null) { // create new
                    createNewShortcut();
                } else {
                    shortcutsPresenter.executeShortcut(shortcutVM);
                }
            }
        });

        tryAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onRefresh();
            }
        });



//        setupBlankState();
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        App.appComponent.inject(this);

        recyclerView.setAdapter(cardsAdapter = new ContainerAdapter<Card>(getActivity(), R.layout.adapter_item_user_card, R.id.card));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                //FIXME: this will fail if the 1st item doesn't totally fit in the screen
                final LinearLayoutManager layoutManager = ((LinearLayoutManager)recyclerView.getLayoutManager());
                int visibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition();
                swipeRefresh.setEnabled(visibleItemPosition == 0 || cards == null || cards.size() == 0);
            }
        });

        if (shortcuts) {
            shortcutsList.setAdapter(shortcutsAdapter = new ViewModelAdapter<ShortcutVM>(getActivity()));
        }

        shortcutsPresenter.setAdapter(shortcutsAdapter);
        swipeRefresh.setOnRefreshListener(this);
        showState(ViewState.LOADING);

        TabLayout tabLayout = (TabLayout) getActivity().findViewById(R.id.tabs);
        View tabView = tabLayout.getTabAt(0).getCustomView();

        ShowcaseBuilder.showcase(getActivity(),
                getString(R.string.mobile_onboarding_cards_1_title),
                getString(R.string.mobile_onboarding_cards_1_text,getString(R.string.app_name)),
                getString(R.string.mobile_onboarding_cards_1_close),
                tabView,
                R.string.on_boarding_cards
        )
//        .flatMap(new Function<Boolean, Observable<Boolean>>() {
//            @Override
//            public Observable<Boolean> call(Boolean aBoolean) {
//                ViewGroup viewGroup = (ViewGroup) getView().findViewById(R.id.container_actions);
//                return ShowcaseBuilder.showcase(getActivity(),
//                        getString(R.string.mobile_onboarding_shortcuts_1_title),
//                        getString(R.string.mobile_onboarding_shortcuts_1_text),
//                        getString(R.string.mobile_onboarding_shortcuts_1_close),
//                        new ViewTarget(viewGroup.getChildAt(0)));
//            }
//        })
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean shown) {
                        Timber.d("shown onboarding: "+shown);
                        if(shown)
                            ((HomeActivity) getActivity()).setCurrentItem(Constants.Frag.channels.ordinal());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.e(throwable, "Error showing onboarding");
                    }
                });

//        onRefresh();
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardsController.deleteAutomation(getActivity(),cards, cardsAdapter,clearBtn,recyclerView, swipeRefresh);
            }
        });
    }

    void showState(ViewState state) {
        viewFlipper.setDisplayedChild(state.ordinal());
    }


    @Subscribe
    public void onDeviceRequest(DeviceEventRequest deviceEventRequest) {
        startActivityForResult(new Intent(getActivity(), DevicePickActivityCards.class)
                .putExtra(Constants.EXTRA_DEVICE_REQUEST, deviceEventRequest)
                , REQ);
    }

    @Subscribe
    public void onMapRequest(MapEventRequest mapEventRequest) {

//        try {
//            int PLACE_PICKER_REQUEST = 1;
//            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
//            builder.setLatLngBounds(new LatLngBounds(mapEventRequest.location, mapEventRequest.location));
//            startActivityForResult(builder.build(getActivity()), PLACE_PICKER_REQUEST);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        startActivityForResult(new Intent(getActivity(), MapsActivity.class)
                .putExtra(Constants.EXTRA_MAP_REQUEST, mapEventRequest)
                , MAPREQ);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQ: BusProvider.getInstance().post(data.getSerializableExtra(Constants.EXTRA_DEVICES)); break;
                case MAPREQ: BusProvider.getInstance().post(data.getParcelableExtra(Constants.EXTRA_MAP_REQUEST)); break;
                case SHORTCUT_INSERT:
                    if(data != null) {
                        if(Constants.SHORTCUT_CHANGE_EVENT.equals(data.getAction())) {
                            getCards(); //should show spinner somewhere
                        }
                    }
                    break;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
//        if(isShortcutChanged) {
//            isShortcutChanged = false;
            onRefresh();
//        }
    }

    @Override
    public void onPause() {
        BusProvider.getInstance().unregister(this);
        super.onPause();
    }


    @Subscribe
    public void onCardFeedbackError(CardFeedbackError cardFeedbackError ) {
        //FIXME: refreshing is not getting triggered when sending feedback
//        swipeRefresh.setRefreshing(false);
        FeedbackMessages.showError(recyclerView);
        Timber.d(cardFeedbackError.error, "CardFeedbackError failure");
    }

    @Subscribe
    public void onCardDismiss(CardDismissEvent cardDismissEvent) {
        Timber.d("getting dismiss");
        if (cards != null && cardsAdapter != null) {
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).id.equals(cardDismissEvent.id)) {
                    cards.remove(i);
                    cardsAdapter.notifyItemRemoved(i);
//                    empty.setVisibility(adapter.getItemCount() > 0 ? View.GONE : View.VISIBLE);
                    if (cards.isEmpty()) {
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onRefresh();
                            }
                        }, 1000);
                    } else if (cardDismissEvent.refreshAfter) {
                        newCards.setVisibility(View.VISIBLE);
                    }
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onCardUpdate(CardUpdateEvent cardUpdateEvent) {
        if (cards != null && cardsAdapter != null) {
            for (int i = 0; i < cards.size(); i++) {
                if (cards.get(i).id.equals(cardUpdateEvent.id)) {
//                    cards.remove(i);
                    cardsAdapter.notifyItemChanged(i);
//                    adapter.notifyItemChanged(adapter.isShowShortcuts() ? i+1 : i);
                    return;
                }
            }
        }
    }

    @Subscribe
    public void onRefresh(Refresh refresh) {
        onRefresh();
    }

    @Override
    public void onRefresh() {
        getCards();

        if (shortcuts) {
            getShortcuts();
        }
    }

    public void  showLoading(final boolean show) {
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(show);
            }
        });

    }

    public void getCards() {
        swipeRefresh.post(new Runnable() {
            @Override
            public void run() {
                swipeRefresh.setRefreshing(true);
                newCards.setVisibility(View.GONE);
            }
        });

        ArrayList<String> exclusions = new ArrayList<>();

        if (locationInteractor.hasLocation(getActivity())) {
            exclusions.add("no-location-permission");
        }

        if (NotificationManagerCompat.from(getContext()).areNotificationsEnabled() && preferencesRepository.getPush()) {
            exclusions.add("no-notifications-permission");
        }

        cardsController.getAllCards(exclusions).subscribe(new Consumer<List<Card>>() {
            @Override
            public void accept(List<Card> cards) {
//                swipeRefresh.setRefreshing(false);
                showLoading(false);
                CardFragment.this.cards = cards;
                clearBtn.setVisibility(cardsController.anyAutomation(cards) ? View.VISIBLE : View.GONE);
                cardsAdapter.setData(cards);
                showState(cards.isEmpty() ? ViewState.BLANK : ViewState.DATA);

            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) {
//                swipeRefresh.setRefreshing(false);
                showLoading(false);
                showState(ViewState.ERROR);
                FeedbackMessages.showError(recyclerView);
                Timber.d(throwable, "UserCards failure");
//                Crashlytics.logException(throwable);
            }
        });
    }

    private void createNewShortcut(){
        Intent i = new Intent(getActivity(), DevicePickerActivity.class);
        i.setAction(DevicePickerActivity.ACTION_SHORTCUT_CREATE);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION, true);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT, getString(R.string.mobile_device_search));
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_FIRST_STRING, getString(R.string.mobile_worker_select_trigger));
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE, Constants.AGENTS_ACTIONABLE);
        i.putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, getString(R.string.mobile_shortcut_add));
        startActivityForResult(i, SHORTCUT_INSERT);
    }


    void getShortcuts(){

        shortcutsInteractor.getShortcuts()
                .subscribe(new Consumer<Models>() {
                    @Override
                    public void accept(Models models) throws Exception {
                        if (models.anythingActionable()) {

                            shortcutVMS = new ArrayList<>();
                            for (Shortcut shortcut : models.getShortcuts()) {
                                shortcutVMS.add(new ShortcutVM(R.layout.shortcut_vertical,shortcut, executeRx));
                            }
                            while (shortcutVMS.size() < 5) {
                                shortcutVMS.add(new ShortcutVM(R.layout.shortcut_vertical,null, executeRx));
                            }

                            shortcutsAdapter.setData(shortcutVMS);
                            shortcutsPresenter.setShortcutVMS(shortcutVMS);
                        }

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Timber.e(throwable, "Error getting shortcuts "+Thread.currentThread().getName());
                        shortcutVMS = new ArrayList<>();
                        Shortcut fakeShortcut = new Shortcut();
                        fakeShortcut.setLabel(getString(R.string.mobile_shortcut_new));

                        fakeShortcut.setColor("#" + Integer.toHexString(R.color.widget_button_grey).substring(2));
                        for (int i = 0; i < 5; i++) {
                            shortcutVMS.add(new ShortcutVM(R.layout.shortcut_vertical,fakeShortcut, null));
                        }
                        shortcutsAdapter.setData(shortcutVMS);
                    }
                });

    }


}
