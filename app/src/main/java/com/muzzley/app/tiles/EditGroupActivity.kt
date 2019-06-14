package com.muzzley.app.tiles

import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.MenuItem
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.app.cards.ContainerAdapter
import com.muzzley.model.tiles.Tile
import com.muzzley.model.tiles.TileGroup
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.retrofit.UserService
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_edit_group.*
import javax.inject.Inject


class EditGroupActivity : AppCompatActivity(){

    @Inject lateinit var modelsStore: ModelsStore
    @Inject lateinit var tilesController: TilesController
    @Inject lateinit var userService: UserService
    @Inject lateinit var analyticsTracker: AnalyticsTracker

//    @InjectView(R.id.edit_name) lateinit var editText: EditText
//    @InjectView(R.id.recyclerview) lateinit var recyclerView: RecyclerView

    private var tileGroup: TileGroup? = null
    private lateinit var adapter: ContainerAdapter<EditGroupVM>
    private lateinit var tilesInGroupMap: List<EditGroupVM>
    private lateinit var tilesInAreaMap: List<EditGroupVM>


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        tileGroup = modelsStore.models?.tileGroupsData?.tileGroups?.find {
            it.id == intent.getStringExtra(Constants.GROUP_ID)
        }
        val tilesInGroup = tileGroup?.children?.filterIsInstance<Tile>() ?: emptyList()

        if (tileGroup == null || tilesInGroup.isEmpty()) { // should never happen, unless backend allowed the creation of empty groups
            finish()
            return
        }
        setContentView(R.layout.activity_edit_group)

        editText.setText(tileGroup?.label)

        adapter = ContainerAdapter(this, R.layout.adapter_item_edit_group, R.id.device)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter

        val items = ArrayList<EditGroupVM>()

        tilesInGroupMap = tilesInGroup.map { EditGroupVM(it, true) }

        items.add(EditGroupVM(label = getString(R.string.mobile_devices)))


        items.addAll(tilesInGroupMap)
        items.add(EditGroupVM(label= getString(R.string.mobile_ungroup), click= this::ungroupDialog ))

        //common classes between all tiles components
        val groupClasses = tilesInGroup.map {
            modelsStore.models?.getTileComponentClasses(it.id) ?: emptyList()
        }.reduce { acc,i ->
            acc.intersect(i).toList()
        }
        val tilesInArea = modelsStore.models?.tileGroupsData?.tileGroups?.find {
            it.id == tileGroup?.parent
        }?.children?.filterIsInstance<Tile>()?.filter {
                    it.isGroupable && modelsStore?.models?.getTileComponentClasses(it.id)?.intersect(groupClasses).isNotNullOrEmpty()
                }
        tilesInAreaMap = tilesInArea?.map { EditGroupVM(tile= it, checked = false) } ?: emptyList()

        if (tilesInArea.isNotNullOrEmpty()) {
            items.add( EditGroupVM(label= getString(R.string.mobile_group_add_devices)))
            items.addAll(tilesInAreaMap)
        }
        adapter.setData(items)

        analyticsTracker.trackSimpleEvent(AnalyticsEvents.EDIT_GROUP_START_EVENT)
        button_done.setOnClickListener {
            done()
        }
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                cancelActivity()
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    fun done(){
        if (tilesInGroupMap.all { it.checked } && tilesInAreaMap.all {! it.checked } && editText.text.toString() == tileGroup?.label) {
            FeedbackMessages.showMessage(recyclerView,R.string.group_no_change)
        } else if (( tilesInAreaMap + tilesInGroupMap ).count { it.checked } < 2) {
            FeedbackMessages.showMessage(recyclerView, R.string.mobile_group_min_devices_text)
        } else if ( editText.text.toString().isBlank()){
            FeedbackMessages.showMessage(recyclerView, R.string.group_empty_name)
        } else {
            var op1: Observable<TileGroup>?  = null
            var op2: Observable<Tile>?  = null
            if (editText.text.toString() != tileGroup!!.label) {

                 op1 = userService.editGroup(tileGroup!!.id,mapOf("label" to editText.text.toString()))
            }

            if ( ! tilesInGroupMap.all { it.checked } || ! tilesInAreaMap.all {! it.checked } ) {
                op2 = tilesController.editGroup(tileGroup!!,
                        tilesInGroupMap.filter { !it.checked }.mapNotNull { it.tile },
                        tilesInAreaMap.filter { it.checked }.mapNotNull { it.tile?.id }
                )
            }
            val op  =
                    if (op1 != null && op2 != null)
                        op1.flatMap { op2 }
                    else
                        op1 ?: op2 ?: Observable.error(RuntimeException("Both observables are null!"))

            op.subscribe({
                analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_FINISH_EVENT, EventStatus.Success, "Success")
                setResult(RESULT_OK)
                finish()
            }, {
                analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_FINISH_EVENT, EventStatus.Error, it.message)
                analyticsTracker.trackSimpleEvent(AnalyticsEvents.EDIT_GROUP_FINISH_EVENT)
                FeedbackMessages.showError(recyclerView)
            })

        }

    }

    fun ungroupDialog(){
        AlertDialog.Builder(this,R.style.AlertDialogStyle)
                .setTitle(R.string.mobile_group_edit)
                .setMessage(R.string.mobile_group_delete_text)
                .setPositiveButton(android.R.string.yes) { _: DialogInterface, _: Int -> ungroup() }
                .setNegativeButton(android.R.string.no) { _: DialogInterface, _: Int ->
                    analyticsTracker.trackSimpleEvent(AnalyticsEvents.EDIT_GROUP_UNGROUP_CANCEL_EVENT)
                }.show()
    }

    fun ungroup() {
//        tilesController.ungroup(tileGroup)
        userService.deleteEmptyGroup(tileGroup!!.id).subscribe(
                {
                    analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_UNGROUP_FINISH_EVENT, EventStatus.Success, "Success")

                    modelsStore.models?.tileGroupsData?.tileGroups?.removeAll { it.id == tileGroup?.id}
                    //FIXME: we should probably also update the tiles, but let"s wait for refresh
                    setResult(RESULT_OK)
                    finish()
                },
                {
                    analyticsTracker.trackGroupAction(AnalyticsEvents.EDIT_GROUP_UNGROUP_FINISH_EVENT, EventStatus.Error, it.message)
                    FeedbackMessages.showMessage(recyclerView, it.message)
                }
        )
    }

//    fun navigateToHomeAndFinish(){
//        startActivity(Intent(this, HomeActivity.class)
//                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                .putExtra(Constants.EXTRA_UPDATE_CHANNELS, true)
//                .putExtra(Constants.EXTRA_NAVIGATE_FRAGMENTS, Constants.FRAG_CHANNELS)
//        )
//        setResult(RESULT_OK)
//        finish()
//    }

    override
    fun onBackPressed() {
        cancelActivity()
        super.onBackPressed()
    }

    fun cancelActivity() {
        analyticsTracker.trackSimpleEvent(AnalyticsEvents.EDIT_GROUP_CANCEL_EVENT)
    }

}
