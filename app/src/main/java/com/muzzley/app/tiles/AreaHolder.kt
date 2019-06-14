package com.muzzley.app.tiles

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.muzzley.R
import com.muzzley.model.tiles.TileGroup
import com.muzzley.util.ui.inflate
import kotlinx.android.synthetic.main.adapter_section_device.view.*

class AreaHolder(parent: ViewGroup, viewType: Int) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.adapter_section_device)) {

    fun bind(tileg: TileGroup, position: Int) {
        itemView.section_text.text = tileg.label
    }
}
