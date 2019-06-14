package com.muzzley.app.analytics;

public class AnalyticsEvents {

    public static final String NAVIGATE_TO_EVENT = "Navigate To";

    /* App */
    public static final String APP_START_EVENT = "Start App";
    public static final String APP_EXIT_EVENT = "Exit App";

    /* Auth */
    public static final String SIGN_UP_START_EVENT = "Sign Up - Start";
    public static final String SIGN_UP_FINISH_EVENT = "Sign Up - Finish";
    public static final String SIGN_IN_START_EVENT = "Sign In - Start";
    public static final String SIGN_IN_FINISH_EVENT = "Sign In - Finish";
    public static final String SIGN_OUT_EVENT = "Sign Out";
    public static final String FORGOT_PASSWORD_START_EVENT = "Forgot your password - Start";
    public static final String FORGOT_PASSWORD_REQUEST_FINISH_EVENT = "Forgot your password - Request Finish";

    /* Shortcuts */
    public static final String CREATE_SHORTCUT_START_EVENT = "Create Shortcut - Start";
    public static final String CREATE_SHORTCUT_FINISH_EVENT = "Create Shortcut - Finish";
    public static final String CREATE_SHORTCUT_CANCEL_EVENT = "Create Shortcut - Cancel";
    public static final String CREATE_SHORTCUT_ADD_ACTION_START = "Create Shortcut - Add Action Start";
    public static final String CREATE_SHORTCUT_ADD_ACTION_FINISH = "Create Shortcut - Add Action Finish";
    public static final String CREATE_SHORTCUT_DELETE_ACTION_FINISH = "Create Shortcut - Delete Action Finish";
    public static final String CREATE_SHORTCUT_DELETE_RULE_ACTION_FINISH = "Create Shortcut - Delete Rule Action Finish";
    public static final String EDIT_SHORTCUT_START_EVENT = "Edit Shortcut - Start";
    public static final String EDIT_SHORTCUT_FINISH_EVENT = "Edit Shortcut - Finish";
    public static final String EDIT_SHORTCUT_CANCEL_EVENT = "Edit Shortcut - Cancel";
    public static final String EDIT_SHORTCUT_ADD_ACTION_START = "Edit Shortcut - Add Action Start";
    public static final String EDIT_SHORTCUT_ADD_ACTION_FINISH = "Edit Shortcut - Add Action Finish";
    public static final String EDIT_SHORTCUT_DELETE_ACTION_FINISH = "Edit Shortcut - Delete Action Finish";
    public static final String EDIT_SHORTCUT_DELETE_RULE_ACTION_FINISH = "Edit Shortcut - Delete Rule Action Finish";
    public static final String REMOVE_SHORTCUT_START_EVENT = "Remove Shortcut - Start";
    public static final String REMOVE_SHORTCUT_FINISH_EVENT = "Remove Shortcut - Finish";
    public static final String REMOVE_SHORTCUT_CANCEL_EVENT = "Remove Shortcut - Cancel";
    public static final String EXECUTE_SHORTCUT_EVENT = "Execute Shortcut";

    /* Groups */
    public static final String CREATE_GROUP_START_EVENT = "Create Group - Start";
    public static final String CREATE_GROUP_FINISH_EVENT = "Create Group - Finish";
    public static final String CREATE_GROUP_CANCEL_EVENT = "Create Group - Cancel";
    public static final String EDIT_GROUP_START_EVENT = "Edit Group - Start";
    public static final String EDIT_GROUP_FINISH_EVENT = "Edit Group - Finish";
    public static final String EDIT_GROUP_CANCEL_EVENT = "Edit Group - Cancel";
    public static final String EDIT_GROUP_UNGROUP_FINISH_EVENT = "Edit Group - Ungroup - Finish";
    public static final String EDIT_GROUP_UNGROUP_CANCEL_EVENT = "Edit Group - Ungroup - Cancel";

    /* Routines */
    public static final String CREATE_ROUTINE_START_EVENT = "Create Routine - Start";
    public static final String CREATE_ROUTINE_FINISH_EVENT = "Create Routine - Finish";
    public static final String CREATE_ROUTINE_CANCEL_EVENT = "Create Routine - Cancel";
    public static final String CREATE_ROUTINE_ADD_TRIGGER_START = "Create Routine - Add Trigger Start";
    public static final String CREATE_ROUTINE_ADD_TRIGGER_FINISH = "Create Routine - Add Trigger Finish";
    public static final String CREATE_ROUTINE_DELETE_TRIGGER_FINISH = "Create Routine - Delete Trigger Finish";
    public static final String EDIT_ROUTINE_ADD_TRIGGER_START = "Edit Routine - Add Trigger Start";
    public static final String EDIT_ROUTINE_ADD_TRIGGER_FINISH = "Edit Routine - Add Trigger Finish";
    public static final String EDIT_ROUTINE_DELETE_TRIGGER_FINISH = "Edit Routine - Delete Trigger Finish";
    public static final String CREATE_ROUTINE_ADD_ACTION_START = "Create Routine - Add Action Start";
    public static final String CREATE_ROUTINE_ADD_ACTION_FINISH = "Create Routine - Add Action Finish";
    public static final String CREATE_ROUTINE_DELETE_ACTION_FINISH = "Create Routine - Delete Action Finish";
    public static final String CREATE_ROUTINE_DELETE_RULE_ACTION_FINISH = "Create Routine - Delete Rule Action Finish";
    public static final String EDIT_ROUTINE_ADD_ACTION_START = "Edit Routine - Add Action Start";
    public static final String EDIT_ROUTINE_ADD_ACTION_FINISH = "Edit Routine - Add Action Finish";
    public static final String EDIT_ROUTINE_DELETE_ACTION_FINISH = "Edit Routine - Delete Action Finish";
    public static final String EDIT_ROUTINE_DELETE_RULE_ACTION_FINISH = "Edit Routine - Delete Rule Action Finish";
    public static final String CREATE_ROUTINE_ADD_BUT_ONLY_IF_START = "Create Routine - Add But Only If Start";
    public static final String CREATE_ROUTINE_ADD_BUT_ONLY_IF_FINISH = "Create Routine - Add But Only If Finish";
    public static final String CREATE_ROUTINE_DELETE_BUT_ONLY_IF_FINISH = "Create Routine - Delete But Only If Finish";
    public static final String EDIT_ROUTINE_ADD_BUT_ONLY_IF_START = "Edit Routine - Add But Only If Start";
    public static final String EDIT_ROUTINE_ADD_BUT_ONLY_IF_FINISH = "Edit Routine - Add But Only If Finish";
    public static final String EDIT_ROUTINE_DELETE_BUT_ONLY_IF_FINISH = "Edit Routine - Delete But Only If Finish";
    public static final String EDIT_ROUTINE_START_EVENT = "Edit Routine - Start";
    public static final String EDIT_ROUTINE_FINISH_EVENT = "Edit Routine - Finish";
    public static final String EDIT_ROUTINE_CANCEL_EVENT = "Edit Routine - Cancel";
    public static final String REMOVE_ROUTINE_START_EVENT = "Remove Routine - Start";
    public static final String REMOVE_ROUTINE_FINISH_EVENT = "Remove Routine - Finish";
    public static final String REMOVE_ROUTINE_CANCEL_EVENT = "Remove Routine - Cancel";
    public static final String ENABLE_ROUTINE_EVENT = "Enable Routine";
    public static final String DISABLE_ROUTINE_EVENT = "Disable Routine";
    public static final String EXECUTE_ROUTINE_EVENT = "Execute Routine";

    /* Devices */
    public static final String ADD_DEVICE_START_EVENT = "Add Device - Start";
    public static final String ADD_DEVICE_FINISH_EVENT = "Add Device - Finish";
    public static final String ADD_DEVICE_CANCEL_EVENT = "Add Device - Cancel";
    public static final String ADD_DEVICE_SELECT_DEVICE_EVENT = "Add Device - Select Device";
    public static final String ADD_BUNDLE_SELECT_BUNDLE_EVENT = "Add Bundle - Select Bundle";
    public static final String ADD_DEVICE_SELECT_CHANNEL_EVENT = "Add Device - Select Channel";
    public static final String EDIT_DEVICE_START_EVENT = "Edit Device - Start";
    public static final String EDIT_DEVICE_FINISH_EVENT = "Edit Device - Finish";
    public static final String EDIT_DEVICE_DELETE_CANCEL_EVENT = "Edit Device - Delete Cancel";
    public static final String EDIT_DEVICE_DELETE_FINISH_EVENT = "Edit Device - Delete Finish";
    public static final String MANUAL_INTERACTION_EVENT = "Manual Interaction";

    /* Force to update */
    public static final String FORCE_TO_UPDATE_SHOW_EVENT = "Update - Show";
    public static final String FORCE_TO_UPDATE_REDIRECT_TO_STORE_EVENT = "Update - Redirect Store";

    /* About */
    public static final String ABOUT_VIEW_EVENT = "About Muzzley - View";

    /* Feedback */
    public static final String FEEDBACK_START_EVENT = "Feedback - Start";
    public static final String FEEDBACK_FINISH_EVENT = "Feedback - Finish";

    /* Suggestions */
    public static final String SUGGESTION_VIEW = "Suggestion - View";
    public static final String SUGGESTION_USER_ENGAGE = "Suggestion - User Engage";
    public static final String SUGGESTION_FINISH = "Suggestion - Finish";
    public static final String SUGGESTION_HIDE = "Suggestion - Hide";

    /* Exceptions */
    public static final String EXCEPTION = "Expection - message";

}
