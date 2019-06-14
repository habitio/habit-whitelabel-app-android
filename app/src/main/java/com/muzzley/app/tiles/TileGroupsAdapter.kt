package com.muzzley.app.tiles

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.muzzley.model.tiles.Tile
import com.muzzley.model.tiles.TileControl
import com.muzzley.model.tiles.TileGroup

class TileGroupsAdapter
    @JvmOverloads constructor(val context: Context, val groupCallBack: TilesGroupHolder.GroupCallBack? = null)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    private var tiles: List<Any>? = null
    val AREA = 1
    val TILE = 2
    val GROUP = 3
    val GROUP_CONTROL = 4

    override
    fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        when (viewType) {
            AREA -> AreaHolder(parent,viewType)
            TILE -> TilesHolder(parent,viewType)
            GROUP -> TilesGroupHolder(parent,viewType, groupCallBack)
            else -> TileControlHolder(parent,viewType) // GROUP_CONTROL
        }

    override
    fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        tiles?.getOrNull(position)?.let{
            when (getItemViewType(position)) {
                AREA -> (holder as AreaHolder).bind(it as TileGroup,position)
                TILE -> (holder as TilesHolder).bind(it as Tile,position)
                GROUP -> (holder as TilesGroupHolder).bind(it as TileGroup,position)
                GROUP_CONTROL -> (holder as TileControlHolder).bind(it as TileControl,position)
            }
        }
    }

    override
    fun getItemViewType(position: Int): Int =
        tiles?.getOrNull(position)?.let {
            when (tiles!![position]) {
                is TileControl -> GROUP_CONTROL
                is TileGroup -> if ((tiles!![position] as TileGroup).parent != null) GROUP else AREA
                is Tile -> TILE
                else -> 0
            }
        } ?: 0

    fun setData(tiles: List<Any> ) {
        this.tiles = tiles
        notifyDataSetChanged()
    }

    override
    fun getItemCount(): Int =
        tiles?.size ?: 0

}
