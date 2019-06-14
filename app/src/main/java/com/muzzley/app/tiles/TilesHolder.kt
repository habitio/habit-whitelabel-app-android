package com.muzzley.app.tiles

import android.app.Activity
import android.content.Context
import android.graphics.Color
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.model.tiles.Tile
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.inflate
import com.muzzley.util.ui.loadUrlFitCenterCrop
import com.muzzley.util.ui.show
import com.muzzley.util.ui.triStateToggle.OnStateChangeListener
import com.muzzley.util.ui.triStateToggle.TriStateToggle
import com.squareup.picasso.Picasso
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.adapter_item_device_card.view.*
import timber.log.Timber
import javax.inject.Inject


class TilesHolder(parent: ViewGroup, viewType: Int) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.adapter_item_device_card)) {

    @Inject lateinit var navigator: Navigator
    @Inject lateinit var tilesController: TilesController
    @Inject lateinit var modelsStore: ModelsStore

    private var context: Context? = null
    private var compositeDisposable: CompositeDisposable? = null


    init {
//        super(LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_item_device_card, parent, false))
//        SwissKnife.inject(this, this.itemView)
        context = parent.context
        App.appComponent.inject(this)
    }

    fun setText(s: String, vararg views: TextView) {
        views.forEach { it.text = s }
    }

    fun bind(tile: Tile, position: Int) {

        compositeDisposable?.dispose()
        compositeDisposable  = CompositeDisposable()

        itemView.textTitle.text = tile.label

        itemView.image_background_device_card.loadUrlFitCenterCrop(tile.photoUrl)
        itemView.ivOverlay.loadUrlFitCenterCrop(tile.overlayUrl)

        val idx0 = if (tile.information.size > 0 && tile.information[0].type == "icon-color") {
            itemView.textTop.show()
            val info = tile.information[0]
            itemView.textTop.text = info.options?._char
            compositeDisposable?.add(
                    info.lastValue
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe {
                                if (it.length >= 6) { // ignore invalid colors
                                    try {
                                        itemView.textTop.setTextColor(Color.parseColor(it))
                                    } catch (e: Exception) {
                                        Timber.d(e, "TH: Error setting color")
                                    }
                                }
                            }
            )
            1
        } else {
            itemView.textTop.hide()
            0
        }
        val idx1 = idx0 + 1

        itemView.run {

            if (tile.information.size > idx0) {
                setTileInfo(tile,idx0,textSubtitleGroup1,textSmallGroup1,textTitleGroup1)

                if (tile.information.size  > idx1 ) {
                    setTileInfo(tile,idx1,textSubtitleGroup2,textSmallGroup2,textTitleGroup2)
                } else {
                    setText("",textTitleGroup2, textSmallGroup2, textSubtitleGroup2)
                }
            } else {
                setText("",textTitleGroup1, textSmallGroup1, textSubtitleGroup1,
                        textTitleGroup2, textSmallGroup2, textSubtitleGroup2)
            }
        }


        itemView.layout_device_card.setOnClickListener {
            if (tile._interface!= null) {
                (context as Activity).startActivityForResult(
                        navigator.gotoInterface()
                                .putExtra(Constants.TILE_ID, tile.id)
                        , Constants.REQUEST_CONNECTOR)
            } else {
                Timber.d("TH: No interface found for ${tile.profile}, ${tile.channel}")
            }
        }

        Timber.d("tile ${tile.label} actions: ${tile.actions.size}")
        if (tile.actions.isNotNullOrEmpty() && tile.actions[0].type == "tri-state") {
            Timber.d("tile ${tile.label} actions: ${tile.actions.size} showing")
            itemView.triStateToggle.show()
            val listener= OnStateChangeListener{ _: View, state : Int->
//                val action = tile.actions[0]
//                val value = state == TriStateToggle.STATE_ON ? action.options.mappings.on : action.options.mappings.off
//                tilesController.sendAction(tile.profile,tile.remoteId, action.componentId, action.property,value,
//                        AnalyticsCallback(label: tile.label, property: action.property, tracker: analyticsTracker))

//                val json = state == TriStateToggle.STATE_ON ? "on" : "off"
                Timber.d("Got state $state")
                tilesController.sendAction(tile,if (state == TriStateToggle.STATE_ON) "on" else "off")
//                tilesController.sendAction(tile,json, AnalyticsCallback(label: tile.label, property: action.property, tracker: analyticsTracker))
            }
            compositeDisposable?.add(
                    tile.actions[0].lastValue
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { lastValue ->
                                itemView.triStateToggle.apply {
                                    setOnStateChangeListener(null) // we don"t want to trigger message sending just because we are recycling a view
//                                  slideToState(["off", "on"].indexOf(lastValue))
                                    state = arrayOf("off", "on").indexOf(lastValue)
//                    setState("on" == lastValue ? TriStateToggle.STATE_ON : TriStateToggle.STATE_OFF) //FIXME: handle unval state
                                    setOnStateChangeListener(listener)
                }
            })

        } else {
            Timber.d("tile ${tile.label} actions: ${tile.actions.size} hiding")
            itemView.triStateToggle.hide()
        }

    }

    fun setTileInfo(tile: Tile,idx: Int, subtitle: TextView, small: TextView, title: TextView){
        val info = tile.information[idx]

        subtitle.text = info.label
        small.text = if (info.unit != null) " (${info.unit?.trim()})" else  "" // ex: (%)

        //FIXME: do proper view binding to afun mem leaks. discard previous subscription ?
        compositeDisposable?.add(
                info.lastValue
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            title.text = it
                        }
        )

    }
//    static class AnalyticsCallback , Callback<Response> {
//
//        String label
//        String property
//        AnalyticsTracker tracker
//
//        override
//        fun onSuccess(obj: Response) {
//            tracker.trackDeviceInteraction(label, property, 0, "Individual", "Tile", EventStatus.Success, "Success")
//        }
//
//        override
//        fun onError(e: Exception) {
//            //FIXME: why are allways getting error, if the command was sent ?! Check this later
//            Boolean error = !(e instanceof TimeoutException)
//            tracker.trackDeviceInteraction(label, property, 0, "Individual", "Tile",
//                    error ? EventStatus.Error : EventStatus.Success,
//                    error ? e.getMessage() : "Success")
////            tracker.trackDeviceInteraction(label, property, 0, "Individual", "Tile", EventStatus.Error, e.getMessage())
//        }
//    }
}
