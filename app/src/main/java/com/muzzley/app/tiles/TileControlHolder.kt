package com.muzzley.app.tiles

import android.app.Activity
import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.model.tiles.TileControl
import com.muzzley.util.ui.inflate
import javax.inject.Inject

class TileControlHolder(parent: ViewGroup, viewType: Int)
    : RecyclerView.ViewHolder(parent.inflate(R.layout.control_all)) {

    var context: Context = parent.context
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var modelsStore: ModelsStore

    init {
        App.appComponent.inject(this)
    }

    fun  bind(tileControl: TileControl ,position: Int) {
        itemView.setOnClickListener {

            tileControl.group?.id?.let {
                (context as Activity).startActivityForResult(
                        navigator.gotoInterface().putExtra(Constants.GROUP_ID, it)
                        , Constants.REQUEST_CONNECTOR);
            } ?: Toast.makeText(context, "Group is empty, please delete it!", Toast.LENGTH_LONG).show()

        }
    }

}