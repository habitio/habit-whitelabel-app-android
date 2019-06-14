package com.muzzley.app.tiles

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.muzzley.model.Preferences
import com.muzzley.model.profiles.ProfilesData
import com.muzzley.model.channels.Channel
import com.muzzley.model.channels.ChannelData
import com.muzzley.model.channels.Property
import com.muzzley.model.profiles.Bundles
import com.muzzley.model.realtime.RealtimeMessage
import com.muzzley.model.shortcuts.Shortcut
import com.muzzley.model.tiles.*
import com.muzzley.model.units.UnitsTable
import com.muzzley.util.Utils
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.math.Parser
import com.muzzley.util.math.SyntaxException
import com.muzzley.util.math.Variable
import timber.log.Timber
import java.text.DecimalFormat

//    make this a singleton
//we could migrate this later to SQL

class Models
    @JvmOverloads
    constructor(
        val tileGroupsData: TileGroupsData,
        val tilesData: TilesData,
        channelData: ChannelData,
        var preferences: Preferences? = null,
        var unitsTable: UnitsTable? = null
    ) {

    val gson = GsonBuilder().create()
    val channels: List<Channel>? = channelData.channels
    var shortcuts: List<Shortcut>? = null
    var serviceSubscriptions: List<ServiceSubscriptions.Subscription>? = null
    val tilesViewModel: List<Any> = buildTilesModel()
    val tilesAreas: List<TileGroup> = buildGrouplessTilesModel()

    var profilesData: ProfilesData? = null
    var bundlesAndServices: Bundles? = null


    fun buildTilesModel(): List<Any>  {

        val (areas,groups) = tileGroupsData.tileGroups.partition { it.parent == null }

        // add groups to areas
        groups.forEach { group -> areas.find { it.id == group.parent }?.children?.add(group) }

        //add tiles to groups

//        tilesData.tiles.removeAll { !getChannel(it.channel)} // remove tiles with no channel
        tilesData.tiles = tilesData.tiles?.filter { getChannel(it.channel) != null } // remove tiles with no channel

        tilesData.tiles.forEach { tile ->
            tile.groups.forEach { groupId ->
                tileGroupsData.tileGroups.find { it.id == groupId }?.children?.add(tile)
            }

            tile.information.forEach { info ->
                //FIX missing label from backend
                if (info.label == null) {
                    info.label = getChannelPropertyLabel(tile.channel, info.property)
                }
                //FIX missing componentId from backend
                info.componentId = tile.components.find { it.type == info.componentType }?.id


//                var suffix:String? = ""
                val pref = preferences
                val ut = unitsTable
                val suffix:String? = if (info.type == "text-unit" && pref != null && ut != null){ //FIXME: quick hack to prevent NPE when using old constructor

                    getChannelProperty(tile.channel,info.property)?.unitsOptions?.let {
                        val targetUnit = if (pref.isMetric())
                            it.targetMetric
                        else
                            it.targetImperial
                        unitsTable?.unitSpec?.get(targetUnit)?.suffix ?: "?US"
                    }
//                    val unitOptions = getChannelProperty(tile.channel,info.property)?.unitsOptions
//                    if (unitOptions == null) {
//                        Timber.d("No unit options for $tile.profile, $tile.channel,$info.property ")
//                    } else {
//                        val targetUnit = if (pref.isMetric()) unitOptions.targetMetric else unitOptions.targetImperial
////                        suffix = unitsTable.unitSpec[targetUnit]?.suffix ?: '?!US'
//                        suffix = firstNotEmpty(unitsTable?.unitSpec?.get(targetUnit)?.suffix, "?!US")
//                    }
                } else
                    null
//                info.unit = suffix ?: info.options.inputUnit ?: info.options.suffix
                info.unit = firstNotEmpty(suffix, info.options?.inputUnit, info.options?.suffix)

            }
            //FIX missing componentId from backend
            tile.actions.forEach { action ->
                action.componentId = tile.components.find { it.type == action.componentType }?.id
            }

            //FIX missing interface from backend
            tile._interface = getChannel(tile.channel)?._interface

        }

//        try {
//            Timber.d("MODEL: ${new GsonBuilder().create().toJson(tilesData.tiles)}")
//            val json = new GsonBuilder().create().toJsonTree(tilesData.tiles)
//            Timber.d("MODEL: Tree:  $json")
//            val file = new File("/sdcard/Download/my_tile_model.json")
//            file.write(json.toString())
//
//        } catch (Exception e) {
//            Timber.e(e,"Error creating json model")
//        }

        return areas.filter{ !it.children.isNullOrEmpty()} // filter out childless areas
                .flatMap { arrayListOf(it) +  it.children }
    }

    fun Collection<Any?>?.isNullOrEmpty() = this == null || this.isEmpty()

    fun tileFlatten(l: List<Any>): List<Tile> =
        l.flatMap {
            when (it) {
                is TileGroup -> tileFlatten(it.children)
                is Tile -> listOf(it)
                else -> listOf()
            }
        }

    fun firstNotEmpty(vararg s: String?) =
            s.find { !it.isNullOrEmpty() }

    fun buildGrouplessTilesModel(): List<TileGroup> {

        val groupCopy = tileGroupsData.tileGroups.map { g -> TileGroup().apply {id = g.id; label = g.label; parent = g.parent} }

        tilesData.tiles.forEach { tile ->
            tile.groups.forEach { groupId ->
                groupCopy.find { it.id == groupId }?.let { group ->
                    val children =
                            if (group.parent == null )
                                group.children
                            else
                                groupCopy.find { it.id == group.parent}?.children

                    children?.let { children ->
                        if (tile.id !in children.filterIsInstance<Tile>().map { it.id }) {
                            children.add(tile)
                        }
                    }
                }
            }
        }

        return groupCopy.filter { it.parent == null }
    }


    fun <T> err(error: String): T? {
        Timber.e(error)
        return null
    }

    fun getChannel(channelId: String?): Channel?
            = channels?.find {it.id == channelId } ?:  err("MODEL: channel not found ! channelId: $channelId")
//        if (!channel) Timber.e("MODEL: channel not found ! channelId: $channelId, did you mean: ${channels*.id}")
//        channel
//        }


    fun getTile(profileId: String,channelId: String, component: String ): Tile? =
       tilesData.tiles?.find {it.profile == profileId && it.channel == channelId && it.components.map { it.id }.contains(component)}

    fun getTile(tileId: String): Tile? =
        tilesData.tiles?.find {it.id == tileId}

    fun getChannelFromTile(tileId: String ): Channel? =
        getChannel(tilesData.tiles.find {it.id == tileId}?.channel)

    fun getTileComponentClasses(tileId: String ): List<String> =
        getChannelFromTile(tileId)?.components?.flatMap { it.classes }?.distinct() ?: arrayListOf()

    fun getTileComponentProperties(tile: Tile): List<Property> {
        val types = tile.components.map { it.type }
        return  getChannel(tile.channel)?._properties?.filter {
            it.components.any { it in types }
        } ?: emptyList()
    }


    fun anythingGroupable(): Boolean? =
            tilesAreas.any {
                val groupableTiles = it.children.filterIsInstance<Tile>().filter { it.isGroupable }
                groupableTiles.indices.any {
                    val classes = getTileComponentClasses(groupableTiles[it].id)
                    groupableTiles.drop((it+1)).any {
                        classes.intersect(getTileComponentClasses(it.id)).isNotNullOrEmpty()
                    }
                }
            }

    fun anythingActionable(): Boolean {
        return true
//        tilesData.tiles.any { getChannelFromTile(it.id)?.isActionable }
//        channels?.any { it.isActionable }
    }


    fun getChannelProperty(channelId: String, propertyId: String ): Property? =
        getChannel(channelId)?._properties?.find {it.id == propertyId } ?: err("MODEL: property not found ! id: $channelId, propertyId: $propertyId")


    fun getChannelPropertyLabel(channelId: String ,propertyId: String ): String?  
            = getChannelProperty(channelId, propertyId)?.label


    fun getTiles(channelId: String ): List<Tile> =
        tilesData.tiles.filter { it.channel == channelId }
//                ?: run {Timber.w("MODEL: could not find any tile with channel: $channelId"); return null}
//
    fun getTileInfo(channelId: String , componentId: String , propertyId: String): Information? =
        getTiles(channelId)
                .flatMap { it.information }
                .find { it.componentId == componentId && it.property == propertyId }


    fun getTileAction(channelId: String , componentId: String, propertyId: String): Action? =
        getTiles(channelId)
                .flatMap { it.actions }
                .find { it.componentId == componentId && it.property == propertyId }


    //Only valid for Area Siblings
//    @Deprecated
//    fun getTileAreaSiblings(pos: Int): List<Tile>  {
//
//        if (tilesViewModel[pos] is Tile ) {
//            int groupIdx = pos - 1
//            while(tilesViewModel[groupIdx].class != TileGroup || (tilesViewModel[groupIdx] as TileGroup).parent) groupIdx -= 1
//            (tilesViewModel[groupIdx] as TileGroup).children.filter { it is Tile} as List<Tile>
//        }
//    }

    fun getTileGroupSiblings(groupId: String): List<Tile>? =
            getTileGroup(groupId)?.children?.filter { it is Tile} as List<Tile>

    fun getTileGroup(groupId: String ): TileGroup? =
        tileGroupsData.tileGroups.find {it.id == groupId}


    fun updateTilesDisplayProp(rtmsg: RealtimeMessage ) {
        //FIXME: remove conversion to json again, or assume paylod data as JsonElement
        rtmsg.address?.let {
            updateTilesDisplayProp(it.channel, it.component, it.property, gson.toJsonTree(rtmsg.payload).asJsonObject )
        }
    }
    fun updateTilesDisplayProp(channelId: String , componentId: String ,propertyId: String , json: JsonObject ) {

        try {

            val data = json.get("data")

            val info = getTileInfo(channelId,componentId,propertyId)
            if (info != null) {

                val result =
                    when (info.type) {
                        "icon-color" ->
                            if (isHSV(data)) {
                                convertHSVToHEXA(data.asJsonObject);
                            } else {
    //                            val rgb = arrayOf("r","g","b").map { data.asJsonObject.get(it).asInt }
                                val rgb = "rgb".split("").map { data.asJsonObject.get(it).asInt }
                                if (rgb.all { it > 240})
                                    "#fefbd0"
                                else
                                    String.format("#%02x%02x%02x",rgb.toIntArray() )
                            }

                        "text-expression" ->
                                //apply math expression
                                try {
                                    val expr = Parser.parse(info.options?.mathExpression)
                                    val x = Variable.make("x");
                                    val value = data.asString.toDouble()
                                    x.setValue(value);
                                    val df = DecimalFormat("#.#")
                                    df.isGroupingUsed = false
                                    df.format(expr.value());
                                } catch (exception: SyntaxException) {
                                    Timber.d("Error parsing math expression: " + exception.explain());
                                    ""
                                }
                        "text-unit" -> {
                                val unitOptions = getChannelProperty(channelId, propertyId)?.unitsOptions
                                val targetUnit = if (preferences!!.isMetric()) unitOptions?.targetMetric else unitOptions?.targetImperial
                                val originUnit = unitOptions?.muzzleyUnit
                                convert(originUnit, targetUnit, data.asString)
                            }
                        else ->
                            data.asString
                    }
                info.lastValue.onNext(result)
            } else {
                getTileAction(channelId,componentId,propertyId)?.apply {
                    options?.mappings?.let {
                        lastValue.onNext(when(data.asString) {
                            it.on.asString -> "on"
                            it.off.asString -> "off"
                            else -> ""
                        })
                    }
                } ?: Timber.i("No tile interested in $channelId, $componentId, $propertyId")
            }

        } catch (e: Exception ) {
            Timber.e("Could not update $channelId, $componentId, $propertyId, ${e.message}")
        }

    }

    fun convert(originUnit: String? ,targetUnit: String? ,value: String): String{

        try {
            val us = unitsTable?.unitSpec?.get(targetUnit) ?: return "?!US" // no unit spec
            val df = DecimalFormat()
            df.setMaximumFractionDigits(us.decimalPlaces)
            df.setGroupingUsed(false)
            if (originUnit == targetUnit)
                return df.format(value.toDouble())
            else{
                val ou = unitsTable?.unitCalc?.get(originUnit) ?: return "?!OU"

                val tu = ou[targetUnit] ?: return "?!TU"

                val expr = Parser.parse(tu);
                val x = Variable.make("x");
                x.setValue(value.toDouble());
                return df.format(expr.value().toDouble())
            }
        } catch (e: Exception ) {
            Timber.e(e,"Exception converting text-unit")
            return "?!EX"
        }
    }

    fun isHSV(jsonElement: JsonElement ): Boolean =
        jsonElement.isJsonObject() && arrayOf("h","s","v").any {jsonElement.asJsonObject.get(it) != null}

    fun convertHSVToHEXA(jsonObject: JsonObject ): String {
        val hue = jsonObject.get("h")?.toString()
        val sat = jsonObject.get("s")?.toString()

        if(hue!=null && !hue.contains("null") && sat!=null && !sat.contains("null")) {
            val h = hue.toFloat()
            val s = sat.toFloat()
            val v = 100f
            //we dont care about brigtness in this place, we need the color, we dont need the brightnss

            if(s > 5) {
                return Utils.hsvToHexa(h, s, v)
            }
        }
        return "#fefbd0"

    }

    fun getFirstComponentWithClass(channelId: String ,propertyClass: String ): List<String>?  {
        try {
//            val channel = getChannel(channelId)
            getChannel(channelId)?.let { channel ->
                channel._properties.find { it.classes.contains(propertyClass)}?.let { prop ->
//                if (prop) {
                    val componentTypes = prop.components
                    val matchingComponent = channel.components.find { componentTypes.contains(it.type) } ?: return null
                    return arrayListOf(prop.id,matchingComponent.id)
                }
            }
            Timber.i("Could not find component with channel: $channelId and property class: $propertyClass")
        } catch (throwable: Throwable ){
            Timber.i(throwable, "Error finding component with channel: $channelId and property class: $propertyClass")
        }
        return null
    }

}
