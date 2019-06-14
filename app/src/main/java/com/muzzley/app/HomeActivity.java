package com.muzzley.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

//import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.tabs.TabLayout;
import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.Navigator;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.cards.CardFragment;
import com.muzzley.app.notifications.NotificationsInteractor;
import com.muzzley.app.tiles.TilesFragment;
import com.muzzley.app.tiles.TilesRefresh;
import com.muzzley.app.workers.WorkersFragment;
import com.muzzley.providers.BusProvider;
import com.muzzley.services.LocationInteractor;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.Utils;
import com.muzzley.util.ui.BlockingViewPager;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;


/**
 * Created by ruigoncalo on 24/08/15.
 */
public class HomeActivity extends AppCompatActivity {

    // variables for view pager
    private static final String STATE_CURRENT_PAGE = "state-current-page";
    private static final int INITIAL_PAGE = 0;
    private static final float TABS_ALPHA_UNSELECTED = 0.6f;
    public static final int SETTINGS_REQ = 191;
    public static final int PERM_REQ = 193;

    @Inject PreferencesRepository preferencesRepository;
    @Inject SignUserController signUserController;
//    @Inject ModelsStore modelsStore;
    @Inject Navigator navigator;
    @Inject AnalyticsTracker analyticsTracker;
//    @Inject HomeInteractor homeInteractor;
    @Inject HomeInteractor2 homeInteractor;
    @Inject NotificationsInteractor notificationsInteractor;
    @Inject LocationInteractor locationInteractor;

    @BindView(R.id.container) View container;
    @BindView(R.id.tabs) TabLayout tabLayout;
    @BindView(R.id.viewpager) BlockingViewPager viewPager;

    private TilesFragment tilesFragment;
    private WorkersFragment workersFragment;
    private PageAdapter pageAdapter;

    private int tabSelected = -1;

    private static final String TAG = "HomeActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        Timber.d("OOO OnCreate");


        if (userIsLoggedIn()) {
            signUserController.onSignIn(this);
            setupViewPager();
            setupTabLayout();
            viewPager.addOnPageChangeListener(new OnPageChangedListener());
            //viewPager.setPageTransformer(true, new DepthPageTransformer());
//            setCrashlyticsUserData();

            if (savedInstanceState == null) {
//                setCurrentItem(INITIAL_PAGE);

                setCurrentItem(getResources().getBoolean(R.bool.disable_timeline) ? 1 : INITIAL_PAGE);

                //track event
//                trackEvent();

            } else {
                setCurrentItem(savedInstanceState.getInt(STATE_CURRENT_PAGE));
            }
        } else {
            // go to "Get Started" screen
//            Intent intent = new Intent(this, GetStartedActivity.class);
//            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
            String msg = "HomeActivity but not logged in !";
            RuntimeException throwable = new RuntimeException(msg);
            //FIXME: Figure out which one of this 3 is better
            analyticsTracker.trackThrowable(throwable);
//            Crashlytics.log(msg);
//            Crashlytics.logException(throwable);
            startActivity(new Intent(this,LauncherActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            finish();
            return;
        }




        locationInteractor.requestLocation(this)
                .subscribe(isLocationUsable -> {
                    Timber.d("Location active: " + isLocationUsable);
                    BusProvider.getInstance().post(new Refresh());
                }, throwable -> Timber.e(throwable,"Error getting permission or location"));

        registerWithNotificationHubs();

        homeInteractor.initChannelUserIdIfNeeded();

//        Analytics.INSTANCE.setUser(
//                UtilsExtensionsKt.toJsonString(preferencesRepository.getAuthorization()),
//                habitStatusCodes -> {
//                    Timber.d("setUser: %s", habitStatusCodes);
//                    return null;
//                }
//        );

//        locationInteractor.scheduleWork2();
//        ContextCompat.startForegroundService(this,new Intent(this,LocationService.class));

//        new Thread(){
//            @Override
//            public void run() {
//                super.run();
//                try {
//                    Thread.sleep(20_000);
//                    Bundle bundle = new Bundle();
//                    bundle.putString("title","test title");
//                    bundle.putString("msg","test msg");
//                    new AzureNotificationsHandler().onReceive(HomeActivity.this, bundle);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }.start();
    }

    private boolean userIsLoggedIn() {
        return preferencesRepository.getAuthorization() != null && preferencesRepository.getUser() != null;
    }

//    private void setCrashlyticsUserData(){
//        ((App) getApplication()).setCrashlyticsUserData(
//                preferencesRepository.getUser().getId(),
//                preferencesRepository.getUser().getEmail(),
//                String.format("%s/%s (Android %s ; %s %s)", BuildConfig.APPLICATION_ID, BuildConfig.VERSION_NAME, Build.VERSION.RELEASE, Build.MANUFACTURER, Build.MODEL));
//    }

    @Override
    protected void onStart() {
        super.onStart();
        Timber.d("OOO Onstart");

        Intent intent = getIntent();
        if (intent != null) {
            navigateTo(intent);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Timber.d("OOO onSaveInstanceState");
        outState.putInt(STATE_CURRENT_PAGE, viewPager.getCurrentItem());
        Timber.d("Saving " + viewPager.getCurrentItem());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Timber.d("OOO onNewIntent");

        if (intent != null) {
            navigateTo(intent);
        }
    }

    public void navigateTo(Intent intent) {
        Constants.Frag navigateToFragment = (Constants.Frag) intent.getSerializableExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS);
        boolean needToUpdateChannels = intent.getBooleanExtra(Constants.EXTRA_UPDATE_CHANNELS, false);
        boolean needToUpdateWorkers = intent.getBooleanExtra(Constants.EXTRA_UPDATE_WORKERS, false);
        String errorMessage = intent.getStringExtra(Constants.EXTRA_MESSAGE);

        Timber.d("navigateTo " + navigateToFragment +
                " (Update channels=" + needToUpdateChannels +
                " | Update workers=" + needToUpdateWorkers + ")");

        if (navigateToFragment != null) {
            int idx = navigateToFragment.ordinal();
            if (getResources().getBoolean(R.bool.disable_timeline) && idx > 0) {
                idx--;
            }
            this.setCurrentItem(idx);
        }

        if (needToUpdateChannels && tilesFragment != null) {
//            tilesFragment.onRefresh();
            BusProvider.getInstance().post(new TilesRefresh()); //Fix in case activity is recovered and fragment isn't really instantiated
            //hack: tiles refresh will trigger cards and workers refresh
        }

        /*if (needToUpdateWorkers && workersFragment != null) {
            workersFragment.getItems();
        }*/

        if (errorMessage != null) {
            handleError(errorMessage);
        }

        // after navigate to fragment remove extra to avoid
        // future lands on Home Activity to navigate to the same fragment
        intent.removeExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS);
        intent.removeExtra(Constants.EXTRA_MESSAGE);
        intent.removeExtra(Constants.EXTRA_UPDATE_CHANNELS);
        intent.removeExtra(Constants.EXTRA_UPDATE_WORKERS);
    }

    public void setCurrentItem(int position) {
        tabSelected = position;
        viewPager.setCurrentItem(position, true);
        resetAlphas(viewPager.getCurrentItem());
    }

    private void setupViewPager() {
        pageAdapter = new PageAdapter(getSupportFragmentManager(), this);
        if (!getResources().getBoolean(R.bool.disable_timeline)) {
            int d = getString(R.string.app_namespace).contains("allianz") ? R.drawable.icon_timeline_allianz : R.drawable.icon_timeline;
            pageAdapter.addFragment(createCardsFragment(), "Timeline", d);
        }
        pageAdapter.addFragment(createAgents(), "Workers", R.drawable.icon_workers);
        pageAdapter.addFragment(createTilesFragment(), "Devices", R.drawable.icon_devices);
        // TODO: Uncomment this line when the Notifications feature is complete
        //pageAdapter.addFragment(createNotificationsFragment(), "Notifications", R.drawable.icon_notifications);
        pageAdapter.addFragment(createUserFragment(), "User", R.drawable.icon_profile);
//        viewPager.setOffscreenPageLimit(OFF_SCREEN_PAGE_LIMIT);
        viewPager.setOffscreenPageLimit(pageAdapter.getCount()-1);
        viewPager.setAdapter(pageAdapter);
    }

    private void setupTabLayout() {
        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pageAdapter.buildTabView(i));
        }
    }

    private TilesFragment createTilesFragment(){
        return tilesFragment = TilesFragment.newInstance();
//        return TilesFragment.newInstance();
    }

    private CardFragment createCardsFragment() {
        return new CardFragment();
    }

    private WorkersFragment createAgents() {
        workersFragment = WorkersFragment.newInstance();
        return workersFragment;
    }

    private UserFragment createUserFragment() {
        return UserFragment.newInstance();
    }

    private void resetAlphas(int selected) {
        Timber.d("Resetting values to " + selected);
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            pageAdapter.getTabView(i).setAlpha(i == selected ? 1f : TABS_ALPHA_UNSELECTED);
        }
    }

    private class OnPageChangedListener implements ViewPager.OnPageChangeListener {

        private boolean isDragging;
        private float temp_offset = -1;
        private boolean saveTempOffset;
        private boolean checkDirection;
        private int scrollingDirection = 0;

        @Override public void onPageScrolled(int i, float v, int i1) {
            //Timber.d("tabSelect = " + tabSelected + " | i = " + i + " | offset = " + v + " | px = " + i1);

            if (saveTempOffset) {
                temp_offset = v;
                //Timber.d("Save temp offset = " + temp_offset);
                saveTempOffset = false;
                checkDirection = true;
            }

            if (checkDirection && temp_offset != -1 && temp_offset != v) {
                if (v > temp_offset) {
                    scrollingDirection = 1;
                    //Timber.d("DIR: Going right (temp=" + temp_offset + " v=" + v);
                } else {
                    scrollingDirection = -1;
                    //Timber.d("DIR: Going left (temp=" + temp_offset + " v=" + v);
                }
                checkDirection = false;
            }

            if (v != 0) {
                switch (scrollingDirection) {
                    case 1:

                        //v: 0 -> 1
                        if (tabSelected == i && i < pageAdapter.getCount()-1) {
//                        if (tabSelected == i && i < NUM_PAGES - 1) {
                            tabSelected = i + 1;
                        }

                        pageAdapter.getTabView(i).setAlpha(Utils.getValueWithThreshold(1 - v, TABS_ALPHA_UNSELECTED));
                        pageAdapter.getTabView(tabSelected).setAlpha(Utils.getValueWithThreshold(v, TABS_ALPHA_UNSELECTED));
                        //Timber.d("[GOING RIGHT] " + tabSelected + " alpha value = " + pageAdapter.getTabView(tabSelected).getAlpha());
                        //Timber.d("[GOING RIGHT] " + i + " alpha value = " + pageAdapter.getTabView(i).getAlpha());
                        break;

                    case -1:

                        // v: 1 -> 0
                        if (tabSelected == i && i > 0) {
                            tabSelected = i - 1;
                        }

                        pageAdapter.getTabView(i).setAlpha(Utils.getValueWithThreshold(1 - v, TABS_ALPHA_UNSELECTED));
                        pageAdapter.getTabView(tabSelected).setAlpha(Utils.getValueWithThreshold(v, TABS_ALPHA_UNSELECTED));
                        //Timber.d("[GOING LEFT] " + tabSelected + " alpha value = " + pageAdapter.getTabView(tabSelected).getAlpha());
                        //Timber.d("[GOING LEFT] " + i + " alpha value = " + pageAdapter.getTabView(i).getAlpha());
                        break;

                    default:
                        //Timber.d("Scrolling dir is 0");

                }
            }
        }

        @Override public void onPageSelected(int i) {
            if (!isDragging) {
                tabSelected = i;
                resetAlphas(tabSelected);
                Timber.d("Page selected = " + tabSelected);
            }
        }

        @Override public void onPageScrollStateChanged(int i) {
            if (i == ViewPager.SCROLL_STATE_DRAGGING) {
                //Timber.d("-- START --");
                isDragging = true;
                saveTempOffset = true;
            }

            if (i == ViewPager.SCROLL_STATE_SETTLING) {
                //Timber.d("-- SETTLING -- ");
            }

            // End of scroll
            if (i == ViewPager.SCROLL_STATE_IDLE) {
                //Timber.d("-- END --");
                tabSelected = viewPager.getCurrentItem();
                //Timber.d("Now selected = " + tabSelected);
                resetAlphas(tabSelected);
                resetValues();

                String screen;
                switch (tabSelected) {
                    case 0:
                        screen = "Cards";
                        break;
                    case 1:
                        screen = "Routines";
                        break;
                    case 2:
                        screen = "Devices";
                        break;
                    case 3:
                        screen = "User Profile";
                        break;
                    default:
                        screen = "Tracking of this screen is not implemented";
                }
                analyticsTracker.trackNavigateTo(screen);
            }
        }

        private void resetValues() {
            isDragging = false;
            temp_offset = -1;
            saveTempOffset = false;
            checkDirection = false;
            scrollingDirection = 0;
        }
    }

    static class PageAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragments = new ArrayList<>();
        private final List<String> fragmentTitles = new ArrayList<>();
        private final List<Integer> fragmentIcons = new ArrayList<>();
        private final List<View> tabViews = new ArrayList<>();
        private final Context context;

        public PageAdapter(FragmentManager fm, Context context) {
            super(fm);
            this.context = context;
        }

        public void addFragment(Fragment fragment, String title, int drawable) {
            fragments.add(fragment);
            fragmentTitles.add(title);
            fragmentIcons.add(drawable);
        }

        @Override
        public Fragment getItem(int position) {
            return fragments.get(position);
        }

        @Override
        public int getCount() {
            return fragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitles.get(position);
        }

        public View getTabView(int position) {
            return tabViews.get(position);
        }

        public View buildTabView(final int position) {
            View tab = LayoutInflater.from(context).inflate(R.layout.layout_custom_tab, null);
            ImageView tabImage = (ImageView) tab.findViewById(R.id.image_tab);
            tabImage.setBackgroundResource(fragmentIcons.get(position));
            tabViews.add(position, tab);
            return tab;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Timber.d("OOO onActivityResult");
        switch (requestCode) {
            default:
                if (resultCode == RESULT_CANCELED && data != null) {
                    String error = data.getStringExtra(Constants.EXTRA_MESSAGE);
                    if (error == null) {
                        error = getResources().getString(R.string.mobile_error_text);
                    }
                    handleError(error);
                }
        }
    }

    private void handleError(String error) {
//        FeedbackMessages.showMessage(coordinatorLayout, error);
        FeedbackMessages.showMessage(container, error);
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {
                Timber.i("This device is not supported by Google Play Services.");
                ToastNotify("This device is not supported by Google Play Services.");
//                finish();
            }
            return false;
        }
        return true;
    }

    public void registerWithNotificationHubs()
    {
        if (checkPlayServices()) {
            notificationsInteractor.register();
        }
    }

    public void ToastNotify(final String notificationMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HomeActivity.this, notificationMessage, Toast.LENGTH_LONG).show();
//                TextView helloText = (TextView) findViewById(R.id.text_hello);
//                helloText.setText(notificationMessage);
            }
        });
    }

}