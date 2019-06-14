package com.muzzley.app.shortcuts

import android.content.Context
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.tiles.Models
import com.muzzley.model.channels.ChannelData
import com.muzzley.model.shortcuts.Shortcut
import com.muzzley.model.shortcuts.Shortcuts
import com.muzzley.model.tiles.TileGroupsData
import com.muzzley.model.tiles.TilesData
import com.muzzley.util.retrofit.UserService
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject

class ShortcutsInteractor
    @Inject constructor(
            val userService: UserService,
//            @Named("mock")  val userServiceMock: UserService,
//            @Named("io")  val ioScheduler: Scheduler,
//            @Named("main")  val mainScheduler: Scheduler,
            val analyticsTracker: AnalyticsTracker,
            val context: Context
    ){



    fun getShortcuts(): Observable<Models> =
        Observable.error(RuntimeException("fake exception"))

//        Observable.zip(
//                userService.getChannelData(),
////                userService.getShortcuts(),
////                userServiceMock.getShortcuts(),
//                Observable.just(Shortcuts(shortcuts= listOf())),
//                BiFunction<ChannelData,Shortcuts, Models> {
//                    channelData, shortcuts ->
//                    Models(TileGroupsData(), TilesData(),channelData).apply {
//                        this.shortcuts = shortcuts.shortcuts
//                    }
//                }
//        )
//        .subscribeOn(Schedulers.io())
//        .observeOn(AndroidSchedulers.mainThread())


    fun deleteShortcut(shortcut: Shortcut ):Observable<Shortcut>  =
        userService.deleteShortcut(shortcut.id)

    fun reorderShortcuts(shortcuts: List<Shortcut>): Completable =
        userService.reorderShortcuts(mapOf("order" to shortcuts.map { it.id }))

    fun executeShortcut(shortcut: Shortcut ): Completable =
        userService.executeShortcut(shortcut.id)
            .doOnComplete { analyticsTracker.trackShortcutExecute(shortcut.getId(), "App", shortcut.getOrigin()) }
            .doOnError{  }

}