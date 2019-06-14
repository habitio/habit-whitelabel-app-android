package com.muzzley.app.shortcuts;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.muzzley.App;
import com.muzzley.Constants;
import com.muzzley.R;
import com.muzzley.app.HomeActivity;
import com.muzzley.app.workers.DevicePickerActivity;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.model.shortcuts.ShortcutSuggestions;
import com.muzzley.model.shortcuts.Shortcuts;
import com.muzzley.util.Network;
import com.muzzley.services.PreferencesRepository;
import com.muzzley.util.retrofit.UserService;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

public class ShortcutWidgetProvider extends AppWidgetProvider {

    private static final String ACTION_RETRY_REQUEST = "com.muzzley.action.RETRY_REQUEST";
    private static final String ACTION_WIDGET_BUTTON_CLICK = "com.muzzley.action.WIDGET_BUTTON_CLICK";
    private static final String ACTION_WIDGET_TAB_CLICK = "com.muzzley.action.WIDGET_TAB_CLICK";
    private static final String EXTRA_BUTTON_ID = "com.muzzley.extra.BUTTON_ID";
    private static final String EXTRA_TAB_ID = "com.muzzley.extra.TAB_ID";
    private static final String EXTRA_SHORTCUT_ID = "com.muzzley.extra.SHORTCUT_ID";
    private static final String EXTRA_SHORTCUT_ORIGIN = "com.muzzley.extra.SHORTCUT_ORIGIN";

    private static final int MAX_SHORTCUT_COUNT = 4;

    /*
     * ViewFlipper pages indexes
     */
    private static final int LOADING_PAGE = 0;
    private static final int LOGIN_PAGE = 1;
    private static final int LOADING_ERROR_PAGE = 2;
    private static final int EMPTY_SHORTCUTS_PAGE = 3;
    private static final int SHORTCUTS_LIST_PAGE = 4;

    /*
     * Widget tabs
     */
    private static final int SHORTCUTS_TAB = 1;
    private static final int SUGGESTIONS_TAB = 2;

    @Inject UserService userService;
    @Inject ShortcutsController shortcutsController;
    @Inject PreferencesRepository preferencesRepositorys;
    @Inject AnalyticsTracker analyticsTracker;

    private Handler handler = new Handler();

    public static void sendUpdateWidgetsBroadcast(Context context) {
        AppWidgetManager widgetManager = AppWidgetManager.getInstance(context);
        int[] ids = widgetManager.getAppWidgetIds(new ComponentName(context, ShortcutWidgetProvider.class));

        Intent intent = new Intent(context.getApplicationContext(), ShortcutWidgetProvider.class);
        intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            invokeUpdate(context, appWidgetId);
        }
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            WidgetPreferences.deleteAllPrefs(context, appWidgetId);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        App.appComponent.inject(this);

        if(ACTION_WIDGET_BUTTON_CLICK.equals(intent.getAction())) {
            if (Network.isConnected(context)) {
                final int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
                final int btnIndex = intent.getIntExtra(EXTRA_BUTTON_ID, 0);

                WidgetPreferences.saveExecutingShortcutPref(context, appWidgetId, btnIndex);
                invokeUpdate(context, false);
                final String shortcutId = intent.getStringExtra(EXTRA_SHORTCUT_ID);
                final String origin = intent.getStringExtra(EXTRA_SHORTCUT_ORIGIN);
                if(shortcutId == null)
                    return;

                shortcutsController.executeShortcut(shortcutId)
                        .subscribe(new Action() {
                            @Override
                            public void run() throws Exception {
                                WidgetPreferences.removeExecutingShortcutPref(context, appWidgetId, btnIndex);
                                WidgetPreferences.saveShowFeedbackShortcutPref(context, appWidgetId, btnIndex, true);
                                invokeUpdate(context, false);

                                analyticsTracker.trackShortcutExecute(shortcutId, "Android Widget",
                                        WidgetPreferences.loadCurrentTabPref(context, appWidgetId) == SHORTCUTS_TAB ? origin : "Contextual");
                            }
                        }, new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) {
                                WidgetPreferences.removeExecutingShortcutPref(context, appWidgetId, btnIndex);
                                WidgetPreferences.saveShowFeedbackShortcutPref(context, appWidgetId, btnIndex, false);
                                invokeUpdate(context, false);
                                Timber.d(throwable,"Error executing shortcut");
                            }
                        });
            } else {
                Toast.makeText(context, R.string.mobile_no_internet_title, Toast.LENGTH_SHORT).show();
            }
        } else if(ConnectivityManager.CONNECTIVITY_ACTION.equals(intent.getAction())
                && Network.isConnected(context)) {
            //connection change
            invokeUpdate(context);
        } else if(ACTION_RETRY_REQUEST.equals(intent.getAction())) {
            invokeUpdate(context);
        } else if(ACTION_WIDGET_TAB_CLICK.equals(intent.getAction())) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            int currentTab = intent.getIntExtra(EXTRA_TAB_ID, SHORTCUTS_TAB);

            if(WidgetPreferences.loadCurrentTabPref(context, appWidgetId) == currentTab)
                return;

            WidgetPreferences.saveCurrentTabPref(context, appWidgetId, currentTab);
            invokeUpdate(context, appWidgetId);
        }
    }


    public void updateAppWidget(Context context, int appWidgetId, List<Shortcut> shortcuts, boolean failed) {
        updateAppWidget(context, appWidgetId, shortcuts, failed, false, true);
    }

    /**
     * @see <a href="http://stackoverflow.com/questions/9700617/new-pendingintent-updates-current-intent">Pending Intent override problem</a>
     */
    public void updateAppWidget(Context context, int appWidgetId, List<Shortcut> shortcuts, boolean failed,
                                boolean isLoading, boolean changePage) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.shortcut_widget_provider);

        int currentTab = WidgetPreferences.loadCurrentTabPref(context, appWidgetId);

        //Config current tab
        views.setTextColor(currentTab == SHORTCUTS_TAB ? R.id.btn_tab_shortcuts : R.id.btn_tab_suggestions,
                context.getResources().getColor(R.color.blackish));

        views.setTextColor(currentTab == SHORTCUTS_TAB ? R.id.btn_tab_suggestions : R.id.btn_tab_shortcuts,
                context.getResources().getColor(R.color.widget_text_secondary));

        views.setViewVisibility(currentTab == SHORTCUTS_TAB ? R.id.tab_border_1 : R.id.tab_border_2, View.VISIBLE);
        views.setViewVisibility(currentTab == SHORTCUTS_TAB ? R.id.tab_border_2 : R.id.tab_border_1, View.GONE);

        Intent tabChangeIntent = new Intent(ACTION_WIDGET_TAB_CLICK);
        tabChangeIntent.putExtra(EXTRA_TAB_ID, SHORTCUTS_TAB);
        tabChangeIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setOnClickPendingIntent(R.id.btn_tab_shortcuts, PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), tabChangeIntent, PendingIntent.FLAG_ONE_SHOT));

        tabChangeIntent = new Intent(ACTION_WIDGET_TAB_CLICK);
        tabChangeIntent.putExtra(EXTRA_TAB_ID, SUGGESTIONS_TAB);
        tabChangeIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setOnClickPendingIntent(R.id.btn_tab_suggestions, PendingIntent.getBroadcast(context, (int) System.currentTimeMillis() + 1, tabChangeIntent, PendingIntent.FLAG_ONE_SHOT));

        //Append show more action
        PendingIntent showMorePendingIntent = PendingIntent.getActivity(context, 1, new Intent(context, ShortcutsActivity.class), 0);
        views.setOnClickPendingIntent(R.id.btn_show_more, showMorePendingIntent);
        views.setViewVisibility(R.id.btn_show_more, View.VISIBLE);

        views.removeAllViews(R.id.container_actions);

        if(isLoading && isLoggedIn()) {
            views.setInt(R.id.flipper, "setDisplayedChild", LOADING_PAGE);
        } else {
            if(failed) {
                Intent intent = new Intent(ACTION_RETRY_REQUEST);
                PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);

                views.setOnClickPendingIntent(R.id.btn_retry, pendingIntent);

                views.setInt(R.id.flipper, "setDisplayedChild", LOADING_ERROR_PAGE);
            } else {
                if(!isLoggedIn()) {
                    PendingIntent signInIntent = PendingIntent.getActivity(context, 0, new Intent(context, HomeActivity.class), 0);

                    views.setOnClickPendingIntent(R.id.btn_sign_in, signInIntent);

                    views.setInt(R.id.flipper, "setDisplayedChild", LOGIN_PAGE);

                    views.setViewVisibility(R.id.btn_show_more, View.INVISIBLE);
                } else {
                    int shortcutsCount = 0;
                    if(shortcuts != null) {
                        shortcutsCount = Math.min(shortcuts.size(), MAX_SHORTCUT_COUNT);
                    }

                    //Actions
                    for (int i = 0; i < shortcutsCount; i++) {
                        views.addView(R.id.container_actions, inflateShortcut(context, appWidgetId, shortcuts.get(i), i));
                    }

                    //Add create shortcut
                    for (int i = shortcutsCount; i < MAX_SHORTCUT_COUNT; i++) {
                        views.addView(R.id.container_actions, inflateEmptyShortcut(context, currentTab == SUGGESTIONS_TAB));
                    }

                    if(changePage) {
                        if(currentTab == SUGGESTIONS_TAB && shortcutsCount == 0) {
                            views.setInt(R.id.flipper, "setDisplayedChild", EMPTY_SHORTCUTS_PAGE);
                        } else {
                            views.setInt(R.id.flipper, "setDisplayedChild", SHORTCUTS_LIST_PAGE);
                        }
                    }
                }
            }
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    void loadShortcuts(final Context context, final int appWidgetId, final boolean changePage) {
        if(!isLoggedIn())
            return;

        userService.getShortcuts()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<Shortcuts>() {
                @Override
                public void accept(Shortcuts shortcuts) {
                    updateAppWidget(context, appWidgetId, shortcuts.getShortcuts(), false, false, changePage);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    updateAppWidget(context, appWidgetId, null, true);
                }
            });
    }

    private void loadSuggestions(final Context context, final int appWidgetId, final boolean changePage) {
        if(!isLoggedIn())
            return;

        userService.getShortcutsSuggestions()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Consumer<ShortcutSuggestions>() {
                @Override
                public void accept(ShortcutSuggestions shortcutsSuggestions) {
                    updateAppWidget(context, appWidgetId, shortcutsSuggestions.getShortcuts(), false, false, changePage);
                }
            }, new Consumer<Throwable>() {
                @Override
                public void accept(Throwable throwable) {
                    updateAppWidget(context, appWidgetId, null, true);
                }
            });
    }

    private boolean isLoggedIn() {
        return preferencesRepositorys.getUser() != null;
    }

    private void invokeUpdate(Context context) {
        invokeUpdate(context, true);
    }

    private void invokeUpdate(Context context, boolean changePage) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, ShortcutWidgetProvider.class));
        for (int appWidgetId : appWidgetIds) {
            if(!isLoggedIn()) {
                updateAppWidget(context, appWidgetId, null, false);
            } else {
                invokeUpdate(context, appWidgetId, changePage);
            }
        }
    }

    private void invokeUpdate(Context context, int appWidgetId) {
        invokeUpdate(context, appWidgetId, true);
    }

    private void invokeUpdate(Context context, int appWidgetId, boolean changePage) {
        if(changePage) {
            updateAppWidget(context, appWidgetId, null, false, true, true);
        }
        int currentTab = WidgetPreferences.loadCurrentTabPref(context, appWidgetId);
        if(currentTab == SHORTCUTS_TAB) {
            loadShortcuts(context, appWidgetId, changePage);
        } else {
            loadSuggestions(context, appWidgetId, changePage);
        }
    }

    private RemoteViews inflateShortcut(final Context ctx, final int appWidgetId, Shortcut shortcut,
                                        int index) {

        Intent intent = new Intent(ACTION_WIDGET_BUTTON_CLICK);
        intent.putExtra(EXTRA_BUTTON_ID, index);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.putExtra(EXTRA_SHORTCUT_ID, shortcut.getId());
        intent.putExtra(EXTRA_SHORTCUT_ORIGIN, shortcut.getOrigin());

        PendingIntent pendingIntent = PendingIntent.getBroadcast(ctx, index, intent, 0);

        final RemoteViews shortcutView = new RemoteViews(ctx.getPackageName(), R.layout.shortcut_vertical);

        shortcutView.setOnClickPendingIntent(R.id.btn_shortcut, pendingIntent);
        String label = shortcut.getLabel();
        if(label == null) {
            label = ctx.getString(R.string.unnamed);
        }
        shortcutView.setTextViewText(R.id.shortcut_title, label);

        List<Integer> executingShortcuts = WidgetPreferences.loadExecutingShortcutsPref(ctx, appWidgetId);
        for (int executingShortcut : executingShortcuts) {
            if(executingShortcut == index) {
                shortcutView.setViewVisibility(R.id.progress, View.VISIBLE);
            }
        }

        List<Pair<Integer, Boolean>> showFeedbackShortcuts = WidgetPreferences.loadShowFeedbackShortcutsPref(ctx, appWidgetId);
        for (Pair<Integer, Boolean> showFeedbackShortcut : showFeedbackShortcuts) {
            if(showFeedbackShortcut.first == index) {
                final int btnId = showFeedbackShortcut.first;
                if(showFeedbackShortcut.second) {
                    shortcutView.setTextViewText(R.id.text_done, ctx.getString(R.string.mobile_done));
                } else {
                    shortcutView.setTextViewText(R.id.text_done, ctx.getString(R.string.shortcut_execute_failed));
                }
                shortcutView.setViewVisibility(R.id.btn_shortcut, View.INVISIBLE);
                shortcutView.setViewVisibility(R.id.text_done, View.VISIBLE);

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        WidgetPreferences.removeShowFeedbackShortcutPref(ctx, appWidgetId, btnId);
                        invokeUpdate(ctx, false);
                    }
                }, 1000);
            }
        }
        if(!shortcut.isShowInWatch()) {
            shortcutView.setViewVisibility(R.id.icon_show_on_watch, View.INVISIBLE);
        }

        if(shortcut.getColor() != null) {
            shortcutView.setInt(R.id.bg_shortcut, "setColorFilter", Color.parseColor(shortcut.getColor()));
        } else {
            shortcutView.setInt(R.id.bg_shortcut, "setColorFilter", ctx.getResources().getColor(R.color.colorPrimary));
        }

        return shortcutView;
    }

    private RemoteViews inflateEmptyShortcut(Context ctx, boolean isSuggestion) {
        RemoteViews shortcutView = new RemoteViews(ctx.getPackageName(), R.layout.shortcut_vertical);
        if(isSuggestion) {
            shortcutView.setTextViewText(R.id.shortcut_title, "");
            shortcutView.setViewVisibility(R.id.icon_show_on_watch, View.INVISIBLE);
            shortcutView.setViewVisibility(R.id.bg_shadow, View.INVISIBLE);
            shortcutView.setInt(R.id.bg_shortcut, "setColorFilter", ctx.getResources().getColor(R.color.gray_light));
        } else {
            Intent intent = new Intent(ctx, DevicePickerActivity.class);
            intent.setAction(DevicePickerActivity.ACTION_SHORTCUT_CREATE);
            intent.putExtra(Constants.EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION, true);
            intent.putExtra(Constants.EXTRA_DEVICE_PICKER_EDITTEXT_HINT, ctx.getString(R.string.mobile_device_search));
            intent.putExtra(Constants.EXTRA_DEVICE_PICKER_FIRST_STRING, ctx.getString(R.string.mobile_worker_select_trigger));
            intent.putExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE, Constants.AGENTS_ACTIONABLE);
            intent.putExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT, ctx.getString(R.string.mobile_shortcut_add));

            PendingIntent pendingIntent = PendingIntent.getActivity(ctx, 2, intent, 0);

            shortcutView.setImageViewResource(R.id.btn_shortcut, R.drawable.ic_new);
            shortcutView.setOnClickPendingIntent(R.id.btn_shortcut, pendingIntent);
            shortcutView.setTextViewText(R.id.shortcut_title, ctx.getString(R.string.mobile_shortcut_new));
            shortcutView.setViewVisibility(R.id.icon_show_on_watch, View.INVISIBLE);
            shortcutView.setViewVisibility(R.id.bg_shadow, View.INVISIBLE);
            shortcutView.setImageViewResource(R.id.bg_shortcut, R.drawable.background_shortcut_add);
        }

        return shortcutView;
    }
}
