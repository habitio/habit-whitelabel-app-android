package com.muzzley.app.tiles

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.app.profiles.ProfilesInteractor
import com.muzzley.model.Preferences
import com.muzzley.model.profiles.ProfilesData
import com.muzzley.model.channels.Address
import com.muzzley.model.channels.ChannelData
import com.muzzley.model.profiles.Bundles
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.model.tiles.*
import com.muzzley.model.units.UnitsTable
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.services.GroupsInteractor
import com.muzzley.util.retrofit.CdnService
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.LogObserver
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Function3
import io.reactivex.functions.Function8
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class TilesController
    @Inject constructor(
    val realtime: Realtime,
    val channelService: ChannelService,
    val userService: UserService,
    val cdnService: CdnService,
    val preferencesRepository: PreferencesRepository,
    val gson: Gson,
    var analyticsTracker: AnalyticsTracker,
    val groupsInteractor: GroupsInteractor,
    val profilesInteractor: ProfilesInteractor
)

{
    var tilesDevicesStatusSubscription: Disposable? = null

    fun getModels(): Observable<Models> =
        Observable.zip(
//                userService.getUserTileGroupData(),

                groupsInteractor.getUserTileGroupData(),
                userService.getUserTileData(),
                userService.getChannelData(),

//                lanRepository.getUserTileGroupData(),
//                lanRepository.getUserTileData(),
//                lanRepository.getChannelData(),

                userService.getPreferences(),
                cdnService.getUnitsTable(),
                userService.serviceSubscriptions,
//                jsonBlob.serviceSubscriptions,
                profilesInteractor.getProfiles(),
                channelService.serviceBundles,

                Function8 {
                    tileGroupsData: TileGroupsData,
                    tilesData: TilesData,
                    channelData: ChannelData,
                    preferences: Preferences,
                    unitsTable: UnitsTable,
                    subscriptions: ServiceSubscriptions,
                    profilesData1: ProfilesData,
                    bundles: Bundles
                    ->
                    Models(tileGroupsData, tilesData, channelData,preferences, unitsTable).apply {
                        serviceSubscriptions = subscriptions.serviceSubscriptions
                        profilesData = profilesData1
                        bundlesAndServices = bundles
                    }
                }
                )
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

    fun getModelsWithType(typeId: String): Observable<Models> =
        Observable.zip(
//                userService.getUserTileGroupData([include: "unsorted,context"]),
                groupsInteractor.getUserTileGroupData(mutableMapOf("include" to "unsorted,context")),
                userService.getUserTileDataWithType(mutableMapOf("include" to "specs,context,capabilities", "type" to typeId)),
                userService.getChannelData(),
                Function3 {
//                    TileGroupsData tileGroupsData, TilesData tilesData, ChannelData channelData  ->
                    tileGroupsData: TileGroupsData , tilesData: TilesData , channelData:ChannelData   ->
                        Models(tileGroupsData, tilesData, channelData)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())


    fun getTilesDevicesStatus(models: Models) {

        models.tilesData.tiles.forEach { tile ->
            tile.information.forEach { info ->
//                getDeviceStatus(tile.remoteId, info.componentId, info.property, models)
                getDeviceStatus(Address(tile.channel, info.componentId!!, info.property), models)
            }
            tile.actions.forEach { action ->
//                getDeviceStatus(tile.remoteId, action.componentId, action.property, models)
                getDeviceStatus(Address(tile.channel, action.componentId!!, action.property), models)
            }
        }
    }

    @Deprecated("") // should  be done on connection
    fun subscribeAll() {
//        realtime.subscribe("v1/iot/#")
//                .subscribe(LogObserver<Boolean>("subscribe v1/iot/#"))
        val subscribeAll = "/v3/users/${preferencesRepository.user!!.id}/channels/#"
        realtime.subscribe(subscribeAll)
                .subscribe(LogObserver<Boolean>("subscribe $subscribeAll"))

        val grants = "/v3/users/${preferencesRepository.user!!.id}/grants/#"
        realtime.subscribe(grants)
                .subscribe(LogObserver<Boolean>("subscribe $grants"))
    }

    fun subscribeTilesDevicesStatus(models: Models) {

        tilesDevicesStatusSubscription?.dispose()
        tilesDevicesStatusSubscription = realtime.listenToRTM()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { models.updateTilesDisplayProp(it) },
                    { Timber.d(it, "Error listening mqtt") }
                )


//        models.tilesData.tiles.unique(false) { a, b ->
//            2*(a.profile <=> b.profile) + (a.remoteId <=> b.remoteId)
//        }.forEach {
//            subscribeDeviceStatus(it.profile,it.remoteId,models)
//        }
    }


    fun getDeviceStatus(address: Address ,models: Models ) {
        realtime.send(RealtimeMessage.read(address))
                .subscribe(LogObserver<Boolean>("getDeviceStatus"))
    }

    fun sendAction(address: Address , value: JsonElement , observer: Observer<Boolean> ) {
        Timber.d("publishing topic: ${address.toTopic()}, payload: $value");
        realtime.send(RealtimeMessage.write(address, value)).subscribe(observer)
    }


    fun sendAction(tile: Tile , value: String ){
        val action = tile.actions[0]
        val json = if ("on" == value) action.options.mappings.on else action.options.mappings.off
//        sendAction(tile.profile,tile.remoteId,action.componentId,action.property,json,
//                AnalyticsCallback(label: tile.label, property: action.property, tracker: analyticsTracker))
        sendAction(Address(tile.channel,action.componentId!!,action.property),json,
                AnalyticsSubscriber(label= tile.label, property= action.property, tracker= analyticsTracker))
    }


    fun createGroup(tileGroup: TileGroup , tilesIds: List<String>): Observable<Tile> =
        userService.createGroup(tileGroup).flatMap {
            addToGroup(it,tilesIds)
        }

    fun addToGroup(tileGroup: TileGroup ,tilesIds: List<String> ): Observable<Tile> {
        val newGroup = mapOf("groups" to  arrayOf(tileGroup.id))
        return Observable.fromIterable(tilesIds).flatMap {
            userService.updateTile(it,newGroup)
        }
    }



    // backend now handles the ungroup when deleting a tile
//    @Deprecated
//    public Observable<JsonElement> ungroup(TileGroup tileGroup) {
//        //find group parent (area), update tiles with area id and delete empty group
//        moveTiles2Parent(tileGroup,tileGroup.children.findAll {it instanceof Tile} as List<Tile>).all {
//            it.groups == [tileGroup.parent]
//        }.flatMap {
//            it ?
//            A        userService.deleteEmptyGroup(tileGroup.id)
//                    : Observable.error(Exception("Not all groups updated"))
//        }
//    }


    fun moveTiles2Parent(tileGroup: TileGroup , tiles: List<Tile>): Observable<Tile> =
        //update tiles with area id
        Observable.fromIterable(tiles).flatMap {
            val groups = mapOf("groups" to arrayOf(tileGroup.parent))
            userService.updateTile(it.id,groups)
        }

    fun editGroup(tileGroup: TileGroup , tiles2remove: List<Tile>, tileIds2add: List<String>): Observable<Tile> =
        Observable.merge(
                moveTiles2Parent(tileGroup,tiles2remove),
                addToGroup(tileGroup,tileIds2add)
        )

    class AnalyticsSubscriber(

        val label: String,
        val property: String,
        val tracker: AnalyticsTracker
    ): Observer<Boolean> {

        override
        fun onComplete() {
            tracker.trackDeviceInteraction(label, property, 0, "Individual", "Tile", EventStatus.Success, "Success")
        }

        override
        fun onError(e: Throwable) {
            Timber.e(e,"Error sending button action")
            tracker.trackDeviceInteraction(label, property, 0, "Individual", "Tile", EventStatus.Error, e.message)
        }

        override
        fun onSubscribe(@NonNull d: Disposable ) {
        }

        override
        fun onNext(o: Boolean ) {
            Timber.d("Sent action")
        }
    }



}
