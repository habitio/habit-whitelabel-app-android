package com.muzzley.app.cards

import android.content.Intent
import android.os.Bundle
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.tiles.Models
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.app.tiles.TilesController
import com.muzzley.model.channels.Device
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.ui.visible
import kotlinx.android.synthetic.main.activity_device_pick.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by caan on 30-09-2015.
 */

class DevicePickActivityCards : AppCompatActivity() , SwipeRefreshLayout.OnRefreshListener{

    @Inject lateinit var tilesController: TilesController
    @Inject lateinit var modelsStore: ModelsStore

    private lateinit var adapter: ContainerAdapter<Device>
    private var filteredDevices: List<Device>? = null
    private var deviceRequest: DeviceEventRequest? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_pick)
        deviceRequest =  intent.getSerializableExtra(Constants.EXTRA_DEVICE_REQUEST) as DeviceEventRequest

        App.appComponent.inject(this)

        val  layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        adapter = ContainerAdapter(this, R.layout.adapter_pickdevice_item, R.id.device)

        recyclerView.apply {
            setLayoutManager(layoutManager)
            adapter = this@DevicePickActivityCards.adapter
//            addOnScrollListener{ recyclerView, newState ->
//                    swipeRefresh.setEnabled(layoutManager.findFirstCompletelyVisibleItemPosition() <= 0)
//            }
        }

        swipeRefresh.setOnRefreshListener(this)
        swipeRefresh.setColorSchemeResources(
                R.color.colorPrimary,
                R.color.gray_lighter,
                R.color.colorPrimary,
                R.color.gray_lighter)

    }

    fun onModels(models: Models) {
        swipeRefresh.isRefreshing = false
        filteredDevices = models.channels?.flatMap { channel ->
            channel.components.map { component ->
                Device().apply {
                    profileId = channel.profileId
                    componentId = component.id
                    channelId = channel.id
                    photoUrl = channel.photoUrl
                    label = component.label
                    classes = component.classes
                    propertyClasses = channel._properties.filter { it.components.contains(component.type)}.flatMap{ it.classes}.distinct()
                }
            }.filter { device ->

                val tile = models.getTile(device.profileId,device.channelId,device.componentId)

                device.checked = deviceRequest?.devices?.any { it == device} ?: false
                device.checked || tile != null && deviceRequest?.filter?.any {
//                    device.classes.containsAll(it.componentClasses) && device.propertyClasses.containsAll(it.propertyClasses)
                    device.classes.contains(it.componentClasses) && device.propertyClasses.containsAll(it.propertyClasses)
                } ?: false
            }
        }
//        if (empty != null) {
//            empty = findViewById<TextView>(R.id.empty)
//            Timber.d("DPA: empty is null ? ${empty == null}, recyclerview : ${recyclerView == null}")
//        }
        empty.visible(!filteredDevices.isNotNullOrEmpty())
        adapter.setData(filteredDevices)

    }

    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_device_picker, menu)
        return true
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when {
            item.itemId == R.id.ok -> {

                if (filteredDevices != null) {
                    setResult(RESULT_OK, Intent().putExtra(Constants.EXTRA_DEVICES,
                            DeviceEventResponse(filteredDevices?.filter {it.checked}, deviceRequest?.requestId)))
                }
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }


    override
    fun onResume() {
        super.onResume()
        modelsStore.models?.let {
            onModels(it)
        } ?: run {
            swipeRefresh.post {swipeRefresh.setRefreshing(true)}
            onRefresh()
        }
    }

    override
    fun onRefresh() {
        tilesController.getModels().subscribe(
                { onModels(it)},
                {fail (it.message)}
        )
    }

    fun fail(reason: String?) {
        swipeRefresh.isRefreshing = false
        Toast.makeText(this, "Get Channels failure " +reason, Toast.LENGTH_SHORT).show()
    }
}
