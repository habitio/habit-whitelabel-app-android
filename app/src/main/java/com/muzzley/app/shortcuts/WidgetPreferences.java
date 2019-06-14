package com.muzzley.app.shortcuts;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by kyryloryabin on 03/12/15.
 */
public class WidgetPreferences {

    private static final String PREFS_NAME = "com.muzzley.ShortcutWidgetProvider";
    private static final String PREF_PREFIX = "appwidget_";

    private static final String PREF_CURRENT_TAB = "_current_tab";
    private static final String PREF_EXECUTING_SHORTCUTS = "_executing_shortcuts";
    private static final String PREF_SHOWFEEDBACK_SHORTCUTS = "_showfeedback_shortcuts";

    public static void saveCurrentTabPref(Context context, int appWidgetId, int currentTab) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(PREFS_NAME, 0).edit();
        preferences.putInt(PREF_PREFIX + appWidgetId + PREF_CURRENT_TAB, currentTab);
        preferences.commit();
    }

    public static int loadCurrentTabPref(Context context, int appWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);

        return preferences.getInt(PREF_PREFIX + appWidgetId + PREF_CURRENT_TAB, 1);
    }

    public static void deleteCurrentTabPref(Context context, int appWidgetId) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(PREFS_NAME, 0).edit();
        preferences.remove(PREF_PREFIX + appWidgetId + PREF_CURRENT_TAB);
        preferences.commit();
    }

    public static void saveExecutingShortcutPref(Context context, int appWidgetId, int btnId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor preferencesEdit = preferences.edit();
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS, new HashSet<String>());
        values.add(Integer.toString(btnId));

        preferencesEdit.putStringSet(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS, values);
        preferencesEdit.commit();
    }

    public static void removeExecutingShortcutPref(Context context, int appWidgetId, int btnId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor preferencesEdit = preferences.edit();
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS, new HashSet<String>());
        values.remove(Integer.toString(btnId));

        preferencesEdit.putStringSet(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS, values);
        preferencesEdit.commit();
    }

    public static List<Integer> loadExecutingShortcutsPref(Context context, int appWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS, new HashSet<String>());

        return parseExecutingShortcutPref(values);
    }

    public static void deleteExecutingShortcutsPref(Context context, int appWidgetId) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(PREFS_NAME, 0).edit();
        preferences.remove(PREF_PREFIX + appWidgetId + PREF_EXECUTING_SHORTCUTS);
        preferences.commit();
    }


    public static void saveShowFeedbackShortcutPref(Context context, int appWidgetId, int btnId, boolean success) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor preferencesEdit = preferences.edit();
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS, new HashSet<String>());
        values.add(String.format("%d:%d", btnId, success ? 1 : 0));
        preferencesEdit.putStringSet(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS, values);
        preferencesEdit.commit();
    }

    public static void removeShowFeedbackShortcutPref(Context context, int appWidgetId, int btnId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor preferencesEdit = preferences.edit();
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS, new HashSet<String>());
        for (String value : values) {
            if(value.startsWith(Integer.toString(btnId))) {
                values.remove(value);
                break;
            }
        }

        preferencesEdit.putStringSet(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS, values);
        preferencesEdit.commit();
    }

    public static List<Pair<Integer, Boolean>> loadShowFeedbackShortcutsPref(Context context, int appWidgetId) {
        SharedPreferences preferences = context.getSharedPreferences(PREFS_NAME, 0);
        Set<String> values = preferences.getStringSet(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS, new HashSet<String>());

        return parseShowFeedbackPref(values);
    }

    public static void deleteShowFeedbackShortcutsPref(Context context, int appWidgetId) {
        SharedPreferences.Editor preferences = context.getSharedPreferences(PREFS_NAME, 0).edit();
        preferences.remove(PREF_PREFIX + appWidgetId + PREF_SHOWFEEDBACK_SHORTCUTS);
        preferences.commit();
    }

    public static void deleteAllPrefs(Context context, int appWidgetId) {
        deleteCurrentTabPref(context, appWidgetId);
        deleteExecutingShortcutsPref(context, appWidgetId);
        deleteShowFeedbackShortcutsPref(context, appWidgetId);
    }

    private static List<Pair<Integer, Boolean>> parseShowFeedbackPref(Set<String> values) {
        List<Pair<Integer, Boolean>> result = new ArrayList<>();
        for (String value : values) {
            String[] split = value.split(":");
            Integer btnId = Integer.parseInt(split[0]);
            Boolean success = Integer.parseInt(split[1]) == 1;
            result.add(new Pair<>(btnId, success));
        }

        return result;
    }

    private static List<Integer> parseExecutingShortcutPref(Set<String> values) {
        List<Integer> result = new ArrayList<>();
        for (String value : values) {
            result.add(Integer.parseInt(value));
        }

        return result;
    }

}
