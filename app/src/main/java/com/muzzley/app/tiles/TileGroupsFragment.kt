package com.muzzley.app.tiles

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.model.tiles.Tile
import com.muzzley.model.tiles.TileControl
import com.muzzley.providers.BusProvider
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.ScreenInspector
import com.muzzley.util.isNullOrEmpty
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.startActivityForResult
import kotlinx.android.synthetic.main.tile_groups_v2.*
import javax.inject.Inject


class TileGroupsFragment : Fragment(){

    @Inject lateinit var modelsStore: ModelsStore
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var userService: UserService

    private var gridLayoutManager: GridLayoutManager? = null
    private var adapter: TileGroupsAdapter? = null

    val EDIT_REQUEST = 1

    companion object {
        @JvmStatic
        fun newInstance(groupId: String): TileGroupsFragment  {
            val fragment = TileGroupsFragment()

            val args = Bundle()
            args.putString(Constants.GROUP_ID, groupId)
            fragment.arguments = args

            return fragment
        }
    }

    override
    fun onCreateView(inflater: LayoutInflater , container: ViewGroup?, savedInstanceState: Bundle?): View {
        val viewContainer = inflater.inflate(R.layout.tile_groups_v2, container, false)

        val viewWidth = ScreenInspector.getScreenWidth(activity)
        val cardViewWidth = activity!!.resources.getDimension(R.dimen.tile_min_width)
        val spanCount = Math.max(2, Math.floor((viewWidth / cardViewWidth).toDouble()).toInt())


        gridLayoutManager = GridLayoutManager(activity, spanCount).apply {
            recycleChildrenOnDetach = true
            orientation = LinearLayoutManager.VERTICAL
        }
        recyclerView.layoutManager = gridLayoutManager
        recyclerView.setHasFixedSize(true)

        adapter = TileGroupsAdapter(inflater.context)
        recyclerView.adapter = adapter

        return viewContainer
    }

    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        App.appComponent.inject(this)
        val group = modelsStore.models?.tileGroupsData?.tileGroups?.find {
            it.id == this.arguments?.getString(Constants.GROUP_ID)
        }
        if (group == null) { //FIX for invalid group from backend, or recovering from kill
            goBack()
            return
        }

        adapter?.setData( listOf( TileControl(group)) + (group.children ?: emptyList<Any>()))

        groupTitle.text = group.label

        edit.setOnClickListener {
            val tileInGroup = modelsStore.models?.tileGroupsData?.tileGroups?.find {
                it.id == group.id
            }?.children?.filterIsInstance<Tile>()

            if (tileInGroup.isNullOrEmpty()) {
                AlertDialog.Builder(activity!!,R.style.AlertDialogStyle)
                        .setTitle(R.string.mobile_group_edit)
                        .setMessage(R.string.mobile_group_delete_text)
                        .setPositiveButton(android.R.string.yes) { _, _ -> ungroup(group.id)
                        }.setNegativeButton(android.R.string.no) { _, _ ->
                            analyticsTracker.trackSimpleEvent(AnalyticsEvents.EDIT_GROUP_UNGROUP_CANCEL_EVENT)
                        }.show()
            }

            activity?.startActivityForResult<EditGroupActivity>(EDIT_REQUEST) {
                putExtra(Constants.GROUP_ID,group.id)
            }
        }
        closeGroupView.setOnClickListener { goBack()}

    }

    fun ungroup(groupId: String) {
        userService.deleteEmptyGroup(groupId).subscribe(
                {
                    analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_UNGROUP_FINISH_EVENT, EventStatus.Success, "Success")
                    modelsStore.models?.tileGroupsData?.tileGroups?.removeAll { it.id == groupId}
                    //FIXME: we should probably also update the tiles, but let"s wait for refresh
                    BusProvider.getInstance().post(TilesRefresh())
                    goBack()
                },
                {
                    analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_UNGROUP_FINISH_EVENT, EventStatus.Error, it.message)
                    FeedbackMessages.showMessage(recyclerView, it.message)
                }
        )
    }


    override
    fun onActivityResult(requestCode: Int , resultCode: Int , data: Intent? ) {
        if (requestCode == EDIT_REQUEST) {
            if (resultCode == Activity.RESULT_OK) {
                BusProvider.getInstance().post(TilesRefresh())
                goBack()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun goBack(){
        //FIXME: could we run into trouble if last fragment wasn"t this ?
        activity?.supportFragmentManager?.popBackStack()
    }

}
