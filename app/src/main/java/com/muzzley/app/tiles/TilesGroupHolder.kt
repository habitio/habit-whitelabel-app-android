package com.muzzley.app.tiles

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.muzzley.R
import com.muzzley.model.tiles.Tile
import com.muzzley.model.tiles.TileGroup
import com.muzzley.util.ui.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.adapter_item_device_group.view.*

class TilesGroupHolder(parent: ViewGroup, viewType: Int, val groupCallBack: GroupCallBack?) :
        RecyclerView.ViewHolder(parent.inflate(R.layout.adapter_item_device_group)) {

    interface GroupCallBack {
        fun onGroupClick(groupId: String)
    }

    val transform = Grayshade()
    val context = parent.context

//    @InjectViews([R.id.image_background_device_card1,R.id.image_background_device_card2,R.id.image_background_device_card3])
//    List<ImageView> imageViews
    val imageViews = arrayOf(itemView.image_background_device_card1,itemView.image_background_device_card2,itemView.image_background_device_card3 )

//    @InjectView(R.id.plus) lateinit var plus: TextView
//    @InjectView(R.id.text_title_device_card) lateinit var title: TextView
//    @InjectView(R.id.layout_device_card) lateinit var cardView: View


    fun bind(tileGroup: TileGroup, position: Int) {

        itemView.text_title_device_card.setText(tileGroup.label)

        imageViews.forEachIndexed{ i, _->
            val tile = if (tileGroup.children.size > i)
                tileGroup.children[i] as Tile
            else
                null
//            Picasso.get()
//                    .load(tile?.photoUrl?.takeIf { !it.isBlank() } )
//                    .fit().centerInside()
//                    .transform(transform)
//                    .into(imageViews[i])

            imageViews[i].loadUrlFitCenterInside(tile?.photoUrl,transform)

//            imageViews[i].setVisibility(tile ? View.VISIBLE : View.GONE)
            imageViews[i].visible(tile != null)
        }

        val pl = tileGroup.children.size - 3
        if( pl > 0){
            itemView.plus.text = "+$pl"
            itemView.plus.show()
        } else {
            itemView.plus.invisible()
        }

        itemView.layout_device_card.setOnClickListener {
            //((Activity)context).startActivity(Intent(context,TileGroupsActivity.class).putExtra(Constants.GROUP_ID,tileGroup.id))
            groupCallBack?.onGroupClick(tileGroup.id)
        }
    }

}
