package com.muzzley.app.tiles

import com.muzzley.model.tiles.Tile
import com.muzzley.model.tiles.TilesData

import javax.inject.Inject
import javax.inject.Singleton;

/**
 * Created by caan on 15-12-2015.
 */
@Singleton
class ModelsStore @Inject constructor(){

    @JvmField var models: Models? = null
    @JvmField var modelsStates: Models? = null
    @JvmField var tileDataAgents: TilesData? = null

    fun getTileAgents(profileId: String? , channelId: String? , component: String?): Tile? =
        tileDataAgents?.tiles?.find {
//            it.profile == profileId && it.channel == channelId && it.components.id.contains(component)
            it.profile == profileId && it.channel == channelId && component in it.components.map { it.id }
        }

    fun clear(){
        models = null;
        modelsStates = null;
        //TODO alterar o models para uma maneira mais estavel de guardar os dados
        //FIXME arranjar maneira mais estavel de limpar estes dados
        //tileDataAgents = null
    }
}
