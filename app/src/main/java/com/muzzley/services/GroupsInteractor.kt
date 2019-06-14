package com.muzzley.services

import android.content.Context
import com.muzzley.R
import com.muzzley.model.tiles.TileGroupsData
import com.muzzley.util.retrofit.UserService
import io.reactivex.Observable
import retrofit2.http.QueryMap
import javax.inject.Inject

class GroupsInteractor
    @Inject constructor(
        val context: Context,
        val userService: UserService
    ){

    private fun shouldTransform() =
        !context.getString(R.string.app_namespace).contains("allianz")

    fun getUserTileGroupData(): Observable<TileGroupsData> =
        mapped(userService.getUserTileGroupData())


    fun getUserTileGroupData(@QueryMap options: Map<String, String>): Observable<TileGroupsData> =
        mapped(userService.getUserTileGroupData(options))

    fun mapped(original: Observable<TileGroupsData> ): Observable<TileGroupsData> =
        if (!shouldTransform())
            original
        else
            original.map {
                it.tileGroups.forEach { it.label = it.label.replace("Allianz",context.getString(R.string.mobile_group_name))}
                it
            }

}