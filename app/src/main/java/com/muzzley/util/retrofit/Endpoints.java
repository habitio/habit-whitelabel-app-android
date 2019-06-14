package com.muzzley.util.retrofit;

/**
 * Created by ruigoncalo on 31/10/14.
 */
public class Endpoints {
//    public static final String AUTHORIZE = "/v3/auth/authorize?client_id=2809c4f0-04ef-11e7-a4e3-4b8c949f2706&response_type=password&scope=sysadmin%2Capplication%2Cuser&username={user_name}&password={password}";
//    public static final String AUTHORIZE = "/v3/auth/authorize?client_id=2809c4f0-04ef-11e7-a4e3-4b8c949f2706&response_type=password&scope=sysadmin%2Capplication%2Cuser";
//public static final String AUTHORIZE = "/v3/auth/authorize?response_type=password&scope=sysadmin%2Capplication%2Cuser";
    public static final String AUTHORIZE = "/v3/auth/authorize?response_type=password&scope=sysadmin%2Capplication%2Cuser";
    public static final String EXCHANGE = "/v3/auth/exchange?grant_type=password";
    public static final String SIGN_IN = "signin";
    public static final String SIGN_UP = "account";
    public static final String RESET_PASSWORD = "sendreset";
    public static final String PROFILES = "profiles";
    public static final String PROFILE = "profiles/{profile_id}";
    public static final String CHANNEL_TEMPLATES= "/v3/users/self/channel-templates?order_by=+order_index&order_index=gt/-999999/n";
    public static final String SERVICE_BUNDLES = "services?include=tutorial";
    public static final String PROFILE_AUTHORIZATION = "profiles/{profile_id}/authorization?policy=one";
    public static final String TEMPLATE_AUTHORIZATION = "{path}?policy=one";
    public static final String SERVICE_BUNDLE_AUTHORIZATION = "services/{bundle_id}/authorization";
    public static final String SERVICE_SUBSCRIBE = "services/{service_id}/authorization?op=subscribe";
    public static final String PROFILE_CHANNELS = "profiles/{profile}/channels";
    public static final String INTERFACE_ARCHIVE = "widgets/{uuid}/archive";
//    public static final String CORE_PROPERTY = "iot/profiles/{profileId}/channels/{remoteChannelId}/components/{componentId}/properties/{property}";
    public static final String CORE_PROPERTY = "v3/channels/{channelId}/components/{componentId}/properties/{propertyId}/value";
    public static final String VERSION_SUPPORT = "clients/android/versions/{version}";
    public static final String STORE_PRODUCT = "{product_id}";
    public static final String UNITS_TABLE = "units/units_table.json";
    public static final String RECIPE_META = "/v3/recipes/{recipe_id}/meta";
    public static final String RECIPE_EXECUTE = "{entry_point_url}/execute";


    // all this endpoints are relative to BASE + "users/{user_id}" + path.
    // The user_id is now injected in the retrofit adapter when using UserService
    public static class User {
        public static final String USER = "users/$user_id/";
        public static final String CARD_FEEDBACK = USER+"cards/{card_id}/feedback";
        public static final String CARDS = USER+"cards";
        public static final String CARDS_DELETE = "/v3/"+USER+"cards";
        public static final String WORKERS_ID = USER+"workers/{worker_id}";
        public static final String WORKER_PLAY = USER+"workers/{worker_id}/play";
        public static final String WORKERS = USER+"workers?muz-capabilities=usecases,workers";
        public static final String TILE_DATA_CONTEXT = USER+"tiles?include=specs,context,capabilities";
        public static final String CHANNELS = USER+"channels";
        public static final String CHANNELS_PROPERTIES = USER+"channels?include=channelProperties,context,capabilities";
        public static final String PLACES = USER+"places";
        public static final String PLACES_ID = USER+"places/{place_id}";
        public static final String TAGS = USER+"tags";
        public static final String SURVEYS = USER+"survey-responses";

        public static final String TILE_GROUP_DATA_UNSORTED = USER+"tile-groups?include=unsorted";
        public static final String TILE_GROUP = USER+"tile-groups/{group_id}";
        public static final String TILE_GROUPS = USER+"tile-groups";
        public static final String TILE_DATA = USER+"tiles?include=specs";
        public static final String TILE = USER+"tiles/{tile_id}";
        public static final String TILES = USER+"tiles";
        public static final String SERVICE_SUBSCRIPTIONS = USER+"services";

        public static final String SHORTCUTS = USER+"shortcuts";
        public static final String SHORTCUTS_REORDER = USER+"shortcuts/reorder";
        public static final String SHORTCUT = USER+"shortcuts/{shortcut_id}";
        public static final String SHORTCUT_PLAY = USER+"shortcuts/{shortcut_id}/play";
        public static final String SHORTCUT_SUGGESTIONS = USER+"shortcut-suggestions";

        public static final String STORE_AD_VIEW = USER+"cards/{card_id}/ads/{store_id}/view";
        public static final String STORE_AD_CLICK = USER+"cards/{card_id}/ads/{store_id}/click";

        public static final String PREFERENCES = "/v3/"+USER+"preferences";
    }

}
