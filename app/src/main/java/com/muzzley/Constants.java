package com.muzzley;

/**
 * Created by ruigoncalo on 23/06/15.
 */
public class Constants {

    public static final int REQUEST_CODE_INTERFACES = 1;

    public static final String EXTRA_DEVICE_NAME = "extra-device-name";
    public static final String EXTRA_DEVICE_ID = "extra-device-id";
    public static final String EXTRA_CHANNEL_ID = "extra-channel-id";
    public static final String EXTRA_PROFILE_ID = "extra-profile-id";

    public static final int REQUEST_CONNECTOR = 4;
//    public static final int REQUEST_TRIGGER = 5;
//    public static final int REQUEST_ACTION = 6;
    public static final int REQUEST_PROFILE_NEW_PHOTO = 7;
    public static final int REQUEST_PROFILE_NEW_PHOTO_CAMERA = 8;
    public static final int REQUEST_PROFILE_NEW_PHOTO_OLD_GALLERY = 9; // pre KitKat versions
    public static final int REQUEST_PROFILE_NEW_PHOTO_NEW_GALLERY = 10; // post KitKat versions

    public static final String EXTRA_MESSAGE = "extra-message";
    public static final String EXTRA_UPDATE_CHANNELS = "extra-update-channels";
    public static final String EXTRA_UPDATE_WORKERS = "extra-update-workers";
    public static final String EXTRA_NAVIGATE_FRAGMENTS = "fragment";
    public static final String EXTRA_Y_POSITION = "extra-y-position";
    public static final String EXTRA_POSITION = "extra-position";
    public static final String EXTRA_CHANNEL_POSITION = "extra-channel-position";
    public static final String EXTRA_HYBRID_VIDEO = "extra-hybrid-video";
    public static final String EXTRA_INTERFACE = "extra-interface";
    public static final String EXTRA_CLASSES = "extra-classes";
    public static final String EXTRA_DEVICES = "extra-devices";
    public static final String EXTRA_REQUEST_ID = "extra-request-id";
    public static final String EXTRA_DEVICE_REQUEST = "extra-device-request";
    public static final String EXTRA_MAP_REQUEST = "extra-location";
    public static final String EXTRA_REQUIRE_LOGIN = "extra-require-login";

    public static final String EXTRA_DEVICE_PICKER_CREATE_GROUP = "extra-agents-create-group";
    public static final String EXTRA_DEVICE_PICKER_MULTIPLE_SELECTION = "extra-agents-multiple-selection";
    public static final String EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT = "extra-agents-actionbar-text";
    public static final String EXTRA_DEVICE_PICKER_EDITTEXT_HINT = "extra-agents-edittext-hint";
    public static final String EXTRA_DEVICE_PICKER_FIRST_STRING = "extra-agents-first-string";
    public static final String EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE = "extra-agents-device-type";
    public static final String EXTRA_DEVICE_PICKER_DEVICE_EXCLUDE = "extra-agents-exclude";
    public static final String EXTRA_DEVICE_PICKER_DEVICE_STATES_ALREADY = "extra-agents-states-already-in";

    public static final String EXTRA_DEVICE_PICKER_WEBVIEW_TILE = "extra-agents-device-webview-tile";
    public static final String EXTRA_DEVICE_PICKER_WEBVIEW_CHANNEL = "extra-agents-device-webview-channel";

//    public static final String FRAG_TIMELINE = "timeline";
//    public static final String FRAG_WORKERS = "workers";
//    public static final String FRAG_CHANNELS = "channels";
//    public static final String FRAG_NOTIFICATIONS = "notifications";
//    public static final String FRAG_USER = "user";
    public enum Frag { timeline, workers, channels, /* notificatons, */ user}


    public static final String DEFAULT_SEGMENT = "defaultView";
    public enum Active { PROFILES, BUNDLES, SERVICES }


    public static final String WORKER_TYPE = "worker-type";
    public static final int CARD_CREATE = 0;
    public static final int CARD_EDIT = 1;

    public static final int CREATE_TRIGGER = 0;
    public static final int CREATE_ACTION = 1;

    public static final int ACTION_DELETE = 0;
    public static final int ACTION_EDIT = 1;
    public static final int ACTION_EXECUTE = 2;

    public static final String ACTION_LOCATION = "com.muzzley.Location";
    public static final String ACTION_GEOFENCE = "com.muzzley.Geofence";

    public static final String EXTRA_LOCATION_INTENT = "location_extra";
    public static final String EXTRA_LOCATION_VALUE = "location_extra_value";
    public static final String EXTRA_GEOFENCE_VALUE = "geofence_extra_value";

    public static final String EXTRA_NEW_AGENTS_JSON = "extra_new_agent_name";
    public static final String EXTRA_NEW_AGENTS_EDITING = "extra_new_agent_editing";
    public static final String EXTRA_AGENTS_IS_EDITING = "extra_agent_editing";
    public static final String EXTRA_AGENTS_ID = "extra_agent_id";
    public static final String EXTRA_AGENTS_NAME = "extra_agent_name";
    public static final String EXTRA_AGENTS_TRIGGERABLE = "extra_agent_trigger";
    public static final String EXTRA_AGENTS_ACTIONABLE  = "extra_agent_action";
    public static final String EXTRA_AGENTS_STATEFULL  = "extra_agent_state";

    public static final String AGENTS_COMPONENT_LOCATION = "location";
    public static final String AGENTS_COMPONENT_PHONE = "phone";

    public static final String AGENTS_TRIGGERABLE = "triggerable";
    public static final String AGENTS_ACTIONABLE  = "actionable";
    public static final String AGENTS_STATEFULL  = "stateful";

    public static final String BRIDGE_AGENTS_TRIGGERABLE = "trigger";
    public static final String BRIDGE_AGENTS_ACTIONABLE  = "action";
    public static final String BRIDGE_AGENTS_STATEFULL  = "state";

    public static final String DEVICE_PICKER_GENERIC_ID_LOCATION_TIME = "generic_father_id";
    public static final String DEVICE_PICKER_GENERIC_ID_TIME = "generic_childre_time";
    public static final String DEVICE_PICKER_GENERIC_ID_LOCATION = "generic_children_location";
    public static final int DEVICE_PICKER_ID_INTENT = 70;
    public static final int DEVICE_PICKER_ID_INTENT_WEBVIEW = 71;
    public static final String DEVICE_PICKER_ID_INTENT_RESULT = "device_picker_result";

    public static final String FRAGMENT_GROUP = "fragment-group";
    public static final String EXTRA_WORKER_TYPE = "extra-worker-type";
    public static final String TYPE_TRIGGER = "trigger";
    public static final String TYPE_ACTION = "action";

    public static final String INTERFACE_ARCHIVE_HEADER_ETAG = "Etag";
    public static final String INTERFACE_ARCHIVE_HEADER_SHA256 = "Content-SHA256";
    public static final String INTERFACE_ARCHIVE_META_PROPERTY_MAIN = "main";
    public static final String INTERFACE_ARCHIVE_META_PROPERTY_UUID = "uuid";

    public static final int FILE_CACHE_STATUS_SAVE = 1;
    public static final int FILE_CACHE_STATUS_UNZIP = 2;

    public static final String DIR_INTERFACES = "interfaces";
    public static final String ZIP_NAME = "archive.zip";
    public static String GROUP_ID = "group_id";
    public static String TILE_ID = "tile_id";

    public static  final String SHORTCUT_CHANGE_EVENT = "shortcutInsertEvent";

    /*Wearable message paths*/

    public static final String SHORTCUTS_SYNC_PATH = "/shortcuts-sync";
    public static final String SHORTCUTS_PATH = "/shortcuts";
    public static final String SHORTCUT_EXECUTED_PATH = "/shortcut-executed";
    public static final String SHORTCUT_EXECUTE_PATH = "/shortcut-execute";
    public static final String SHORTCUT_CREATE_PATH = "/shortcut-create";
    public static final String SHORTCUT_DELETE_PATH = "/shortcut-delete";
    public static final String LOGIN_PATH = "/login";
    public static final String LOGOUT_PATH = "/logout";
    public static final String IS_LOGGED_PATH = "/is-logged";

    /*Actions*/

    public static final String ACTION_LOGIN = "com.muzzley.action.LOGIN";
    public static final String ACTION_LOGOUT = "com.muzzley.action.LOGOUT";

    /*Extras*/

    public static final String EXTRA_LOGIN_TYPE = "com.muzzley.extra.LOGIN_TYPE";


    public static final int EMAIL_LOGIN_TYPE = 1;
    public static final int NORMAL_LOGIN_TYPE = 2;
    public static final int FACEBOOK_LOGIN_TYPE = 3;
    public static final int GOOGLE_LOGIN_TYPE = 4;

    public static final String SIGN_IN_TYPE_GOOGLE = "Google";
    public static final String SIGN_IN_TYPE_FACEBOOK = "Facebook";
    public static final String SIGN_IN_TYPE_EMAIL = "Email";

    /*Interface Titles*/
    public static final String INTERFACE_TITLE = "interface_title";
    /*Formatting*/
//    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";

    /*Application preferences*/
//    public static final String APP_PREFS = "AppPrefs";
//    public static final String GOOD_UNTIL_PREF = "goodUntil";


    /*Muzz Capabilities - tags to save and use depending on the capabilities od the device where muzzley is running*/
    public static final String MUZ_CAP_BLUETOOTH = "bluetooth";
    public static final String MUZ_CAP_LOCATION_GPS = "location";

    public static final String MUZ_CAP_TRIGGER_CALL_INCOMING = "trigger-call-incoming";
    public static final String MUZ_CAP_TRIGGER_CALL_INCOMING_NO_NUMBER = "trigger-call-incoming-no-number";
    public static final String MUZ_CAP_TRIGGER_CALL_MISSED = "trigger-call-missed";
    public static final String MUZ_CAP_TRIGGER_CALL_MISSED_NO_NUMBER = "trigger-call-missed-no-number";
    public static final String MUZ_CAP_TRIGGER_SMS_INCOMING = "trigger-sms-incoming";
    public static final String MUZ_CAP_TRIGGER_SMS_INCOMING_NO_NUMBER = "trigger-sms-incoming-no-number";
    public static final String MUZ_CAP_PUSH_NOTIFICATIONS = "push-notifications";
    public static final String MUZ_CAP_BG_AUDIO = "bg-audio";
    public static final String MUZ_CAP_TALK_FOSCAM = "talk-foscam";


    /*Muzz Capabilities
    public static final String TRIGGER_CALL_INCOMING = "trigger-call-incoming";
    public static final String TRIGGER_CALL_MISSED = "trigger-call-missed";
    public static final String TRIGGER_SMS_INCOMING = "trigger-sms-incoming";
    public static final String DIVIDER = ",";*/
    public static final String ON_BOARDING_PREFERENCES = "on_boarding_prefs";
    public static final String PRODUCT_DETAIL = "PRODUCT_DETAIL";

    public static final String NOTIFICATION_CHANNEL_PLATFORM = "platform";
    public static final int NOTIFICATION_LOCATION_ID = 123;
}
