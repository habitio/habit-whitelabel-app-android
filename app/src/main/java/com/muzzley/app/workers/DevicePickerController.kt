package com.muzzley.app.workers

import com.muzzley.Constants
import com.muzzley.app.tiles.Models
import com.muzzley.model.tiles.Tile
import com.muzzley.util.iff
import com.muzzley.util.isNullOrEmpty
import java.util.*

/**
 * Created by bruno.marques on 27/11/15.
 */
object DevicePickerController {

    fun createModelViewData(models: Models, muzzCapabilities: List<String>, dataType: String): List<DevicePickerAdapter.Item> {
        return createModelViewData(models, ArrayList(), muzzCapabilities, dataType)

    }

    fun createModelViewDataX(models: Models, f: (String,Tile) -> DevicePickerAdapter.Item? ): List<DevicePickerAdapter.Item> =

            models.tilesAreas.flatMap {  tileGroup ->
                tileGroup.children?.let{
                    val x =tileGroup.children.filterIsInstance<Tile>()
                            .mapNotNull {
                                f(tileGroup.id,it)
                            }
                    iff (x.isNotEmpty()) {

                        listOf(DevicePickerAdapter.Item(DevicePickerAdapter.PARENT, tileGroup.label, tileGroup.id)) + x

                    }
                } ?: emptyList()
            }

    fun createModelViewData(models: Models, tiles2exclude: ArrayList<String>, muzzCapabilities: List<String>, dataType: String): List<DevicePickerAdapter.Item> =
            createModelViewDataX(models) { tileGroupId, tile ->
                iff (tile.id !in tiles2exclude && anyCapabilitiesMatches(models, tile, muzzCapabilities, dataType)) {

                    DevicePickerAdapter.Item(DevicePickerAdapter.CHILD,
                            tile.label,
                            tileGroupId,
                            tile.id,
                            tile.photoUrlAlt,
                            tile.photoUrl)

                }
            }

    fun createModelViewDataGroups(models: Models): List<DevicePickerAdapter.Item> =
            createModelViewDataX(models) { tileGroupId, tile ->
                iff (tile.isGroupable) {
                    DevicePickerAdapter.Item(DevicePickerAdapter.CHILD,
                            tile.label,
                            tileGroupId,
                            tile.id,
                            tile.photoUrlAlt,
                            tile.photoUrl,
                            tile.isGroupable,
                            models.getChannel(tile.channel)?.components?.flatMap { it.classes }
                    )
                }
            }


//    fun anyCapabilitiesMatches(models: Models, tile: Tile, muzzCapabilities: List<String>, dataType: String?): Boolean {
//        val channel = models.getChannel(tile.channel)
//        if (channel != null) {
//            for (property in channel._properties) {
//                if (dataType != null) {
//                    val ignore =
//                        when (dataType) {
//                            Constants.AGENTS_TRIGGERABLE -> !property.isTriggerable
//                            Constants.AGENTS_ACTIONABLE -> !property.isActionable
//                            Constants.AGENTS_STATEFULL -> !property.isStateful
//                            else -> false
//                        }
//                    if (ignore) {
//                        Timber.d("ignoring prop " + property.id + " for tile " + tile.label)
//                        continue
//                    }
//                }
//                for (component in property.components) {
//                    for (component1 in tile.components) {
//                        if (component1.type == component) { // component match
//                            if (property.requiredCapabilities.isEmpty()) {
//                                Timber.d("returning true for " + tile.id + ", " + tile.label + ", empty cap")
//                                return true
//                            } else {
//                                for (requiredCapability in property.requiredCapabilities) {
//                                    if (muzzCapabilities.contains(requiredCapability)) {
//                                        Timber.d("returning true for " + tile.id + ", " + tile.label + ", matching cap " + requiredCapability)
//                                        return true
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//        Timber.d("returning false for " + tile.id + ", " + tile.label)
//        return false
//    }


    fun anyCapabilitiesMatches(models: Models, tile: Tile, muzzCapabilities: List<String>, dataType: String?): Boolean =
            models.getTileComponentProperties(tile)
                    .any {
                        when (dataType) {
                            Constants.AGENTS_TRIGGERABLE -> it.isTriggerable
                            Constants.AGENTS_ACTIONABLE -> it.isActionable
                            Constants.AGENTS_STATEFULL -> it.isStateful
                            else -> false
                        } && ( it.requiredCapabilities.isNullOrEmpty() || it.requiredCapabilities.any { it in muzzCapabilities })
                    }


}