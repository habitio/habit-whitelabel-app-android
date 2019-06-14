package com.muzzley.app.workers

import com.muzzley.app.tiles.Models
import com.muzzley.model.channels.ChannelData
import com.muzzley.model.tiles.TileGroupsData
import com.muzzley.model.tiles.TilesData
import com.muzzley.services.GroupsInteractor
import com.muzzley.util.retrofit.UserService
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function3
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class DevicePickerMashData

    @Inject constructor(
            val userService: UserService,
            val groupsInteractor: GroupsInteractor
    ){



    fun getItemsWithTypeAndExclude(typeId: String , excludeValue: String ): Observable<Models> =
        Observable.zip(
//                userService.getUserTileGroupData(mapOf("include" to "unsorted,context")),
                groupsInteractor.getUserTileGroupData(mapOf("include" to "unsorted,context")),
                userService.getUserTileDataWithType(mapOf("include" to "specs,context,capabilities", "type" to typeId, "exclude" to  excludeValue)),
                userService.getUserChannelsWithType(mapOf("include" to "channelProperties,context,capabilities", "type" to  typeId)),
                Function3{ tileGroupsData: TileGroupsData , tilesData: TilesData , channelData: ChannelData  ->
                        Models(tileGroupsData, tilesData, channelData)
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())

}
