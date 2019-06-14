package com.muzzley.util.retrofit;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.muzzley.model.Preferences;
import com.muzzley.model.ProfileFeedback;
import com.muzzley.model.Subscription;
import com.muzzley.model.User;
import com.muzzley.model.workers.Worker;
import com.muzzley.model.workers.Workers;
import com.muzzley.model.cards.CardFeedback;
import com.muzzley.model.cards.UserCards;
import com.muzzley.model.channels.ChannelData;
import com.muzzley.model.shortcuts.Shortcut;
import com.muzzley.model.shortcuts.ShortcutSuggestions;
import com.muzzley.model.shortcuts.Shortcuts;
import com.muzzley.model.tiles.ServiceSubscriptions;
import com.muzzley.model.tiles.Tile;
import com.muzzley.model.tiles.TileGroup;
import com.muzzley.model.tiles.TileGroupsData;
import com.muzzley.model.tiles.TilesData;
import com.muzzley.model.user.Place;
import com.muzzley.model.user.Places;
import com.muzzley.model.user.Tags;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;

/**
 * Created by caan on 08-04-2016.
 */
public interface UserService {
    @POST(Endpoints.User.CARD_FEEDBACK)
    Observable<JsonObject> postUserCardFeedback(@Path("card_id") String cardId, @Body CardFeedback cardFeedback);

    @Headers({"muz-capabilities-graphical: browse,reply,done,create-shortcut,dismiss,show-info,share,no-image,create-usecase", //TODO: navigate
            "muz-capabilities-functional: picker-location,picker-device,picker-time-weekday,picker-text,picker-single-choice,picker-multi-choice,picker-ads-list"}) //TODO: edit-worker
    @GET(Endpoints.User.CARDS)
    Observable<UserCards> getUserCards(@QueryMap Map<String, String> options);

    @DELETE(Endpoints.User.CARDS_DELETE)
    Completable deleteUserCards(@QueryMap Map<String, String> options);

    @DELETE(Endpoints.User.WORKERS_ID)
    Completable deleteWorker(@Path("worker_id") String workerId);

    @PATCH(Endpoints.User.WORKERS_ID)
    Completable enableWorker(@Path("worker_id") String workerId, @Body Map<String,Boolean> json);

    @POST(Endpoints.User.WORKER_PLAY)
    Completable executeWorker(@Path("worker_id") String workerId);

    @POST(Endpoints.User.WORKERS)
    Observable<Worker> createWorker(@Body Worker agent);

    @PUT(Endpoints.User.WORKERS_ID)
    Completable editWorker(@Path("worker_id") String workerId, @Body Worker agent);


    /**
     * @see <a href="https://bitbucket.org/muzzley/muzzley-api/wiki/worker-resource#markdown-header-capabilities">https://bitbucket.org/muzzley/muzzley-api/wiki/worker-resource#markdown-header-capabilities</a>
     */
    @GET(Endpoints.User.WORKERS)
    Observable<Workers> getWorkers();

    @GET(Endpoints.User.WORKERS_ID)
    Observable<Worker> getWorker(@Path("worker_id") String workerId);

    @GET(Endpoints.User.TILE_DATA_CONTEXT)
    Observable<TilesData> getUserTileDataContext();

    @GET(Endpoints.User.CHANNELS_PROPERTIES)
    Observable<ChannelData> getChannelData();

    @DELETE(Endpoints.User.TILE_GROUP)
    Observable<JsonElement> deleteEmptyGroup(@Path("group_id") String groupId);

    @PUT(Endpoints.User.TILE_GROUP)
    Observable<TileGroup> editGroup(@Path("group_id") String groupId, @Body Object labelBody);

    @GET(Endpoints.User.PLACES)
    Observable<Places> getPlaces();

    @PATCH(Endpoints.User.PLACES_ID)
    Observable<Place> setPlace(@Path("place_id") String placeId, @Body Place place);

    @POST(Endpoints.User.PLACES)
    Observable<Place> setPlace(@Body Place place);

    @DELETE(Endpoints.User.PLACES_ID)
    Observable<Place> deletePlace(@Path("place_id") String placeId);

    @GET(Endpoints.User.TAGS)
    Observable<Tags> getTags();

    @POST(Endpoints.User.SURVEYS)
    Completable postUserSurvey(@Body ProfileFeedback feedback);

    @POST(Endpoints.User.CHANNELS)
    Observable<List<Map<String,Object>>> postSubscriptions(@Body Subscription channels);

    @GET(Endpoints.User.TILE_GROUP_DATA_UNSORTED)
    Observable<TileGroupsData> getUserTileGroupData();

    @POST(Endpoints.User.TILE_GROUPS)
    Observable<TileGroup> createGroup(@Body TileGroup tileGroup);

    @GET(Endpoints.User.TILE_DATA)
    Observable<TilesData> getUserTileData();

    @PATCH(Endpoints.User.TILE)
    Observable<Tile> updateTile(@Path("tile_id") String tileId, @Body Object body);

    @DELETE(Endpoints.User.TILE)
    Completable deleteTile(@Path("tile_id") String tileId);

    @GET(Endpoints.User.TILES)
    Observable<TilesData> getUserTileDataWithType(@QueryMap Map<String, String> options);

    @GET(Endpoints.User.CHANNELS)
    Observable<ChannelData> getUserChannelsWithType(@QueryMap Map<String, String> options);

    @GET(Endpoints.User.TILE_GROUPS)
    Observable<TileGroupsData> getUserTileGroupData(@QueryMap Map<String, String> options);

    @GET(Endpoints.User.SHORTCUTS)
    Observable<Shortcuts> getShortcuts();

    @POST(Endpoints.User.SHORTCUTS)
    Observable<Shortcut> createShortcut(@Body Shortcut shortcut);

    @PUT(Endpoints.User.SHORTCUT)
    Observable<Shortcut> editShortcut(@Path("shortcut_id") String shortcutId, @Body Shortcut shortcut);

    @DELETE(Endpoints.User.SHORTCUT)
    Observable<Shortcut> deleteShortcut(@Path("shortcut_id") String shortcutId);

    @POST(Endpoints.User.SHORTCUT_PLAY)
    Completable executeShortcut(@Path("shortcut_id") String shortcutId);

    @GET(Endpoints.User.SHORTCUT_SUGGESTIONS)
    Observable<ShortcutSuggestions> getShortcutsSuggestions();

    @Deprecated
    @POST(Endpoints.User.SHORTCUTS_REORDER)
    Observable<String> reorderShortcutsOld(@Body JsonObject order);

    @POST(Endpoints.User.SHORTCUTS_REORDER)
    Completable reorderShortcuts(@Body Map<String,Object> order);

    @POST(Endpoints.User.STORE_AD_VIEW)
    Observable<String> putStoreView(@Path("card_id") String cardId, @Path("store_id") String shortcutId);

    @POST(Endpoints.User.STORE_AD_CLICK)
    Observable<String> sendAdCardClick(@Path("card_id") String cardId, @Path("store_id") String storeId);

    @GET(Endpoints.User.SERVICE_SUBSCRIPTIONS)
    Observable<ServiceSubscriptions> getServiceSubscriptions();

    @GET(Endpoints.User.PREFERENCES)
    Observable<Preferences> getPreferences();

    @POST(Endpoints.User.PREFERENCES)
    Observable<Preferences> createPreferences(@Body Preferences preferences);

    @PATCH(Endpoints.User.PREFERENCES)
    Observable<Preferences> updatePreferences(@Body Preferences preferences);

    @GET("/v3/users/$user_id")
    Observable<User> getUser();

    @PATCH("/v3/users/$user_id")
    Observable<User> setUser(@Body User user);

}
