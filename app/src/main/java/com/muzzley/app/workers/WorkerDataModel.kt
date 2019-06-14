package com.muzzley.app.workers

import com.muzzley.app.tiles.Models
import com.muzzley.model.Preferences
import com.muzzley.model.workers.Workers
import com.muzzley.model.tiles.TilesData
import com.muzzley.model.units.UnitsTable

/**
 *
 * This model have the necessarie data to use the agents flow.
 * TileData is to get the name of the tiles from the agent items
 * State Models to know how much state items we can have while creating a agent
 * Worker because we need to show the agents (workers - old name)
 *
 * Created by bruno.marques on 29/01/2016.
 */
class WorkerDataModel(
        val tileData: TilesData,
        val model: Models,
        val workers: Workers,
        preferences: Preferences,
        unitsTable: UnitsTable) {

    init {
        //FIXME: refactor this into proper place
        this.model.preferences = preferences
        this.model.unitsTable = unitsTable
    }
}
