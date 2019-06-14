package com.muzzley.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.ui.ProgDialog
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_interface_settings.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by ruigoncalo on 01/10/15.
 */
class InterfaceSettingsActivity : AppCompatActivity() {

    @Inject lateinit var userService: UserService
    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var navigator: Navigator
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var modelsStore: ModelsStore

    private var deviceName: String? = null
    private var deviceId: String? = null
    private var profileId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_interface_settings)

        configActionBar()
        if (isIntentDataValid()) {
            setupLayoutEditDeviceName()
        } else {
            FeedbackMessages.showError(editText)
            doneButton.isEnabled = false
            editText.isEnabled = false
        }

        analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_START_EVENT, profileId)
        doneButton.setOnClickListener { onDoneButtonClick() }
    }

    private fun setupLayoutEditDeviceName() {
        editText.setText(deviceName)
    }

    private fun configActionBar() {
        supportActionBar?.apply {
            setTitle(R.string.mobile_device_edit)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.delete, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            R.id.menu_delete -> {
                onDeleteButtonClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    fun onDoneButtonClick() {

        // disable button to avoid more than one click

        if (isNameValid()) {
            doneButton.isEnabled = false

            val label = editText.text.toString()

            userService.updateTile(deviceId, mapOf("label" to label))
                    .compose(RxComposers.applyIoRefresh(ProgDialog.getLoader(this)))
                    .subscribe(
                            { tile ->
                                Timber.d("ISA: got $tile")
                                goBackAndUpdate(label)

                                analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_FINISH_EVENT,
                                        profileId,
                                        EventStatus.Success,
                                        "Success")
                            },
                            { throwable ->
                                Timber.d(throwable, "Error updating tile")
                                FeedbackMessages.showError(editText)
                                doneButton.isEnabled = true

                                analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_FINISH_EVENT,
                                        profileId,
                                        EventStatus.Error,
                                        throwable.message)
                            }
                    )
        } else {
            FeedbackMessages.showMessage(editText, getString(R.string.error_invalid_device_name))
            doneButton.isEnabled = true
        }
    }

    fun onDeleteButtonClick() {

        //check if the group would be left with only one element, and if so, also delete group
        val tile = modelsStore.models!!.getTile(deviceId!!)
        val groupId = if (tile?.groups.isNotNullOrEmpty() && modelsStore.models?.getTileGroup(tile!!.groups[0])?.parent != null)
            tile!!.groups[0] //FIXME: only works if tile is only inside one group
        else
            null

        val obs: Observable<Boolean> =
        if (groupId != null && modelsStore.models!!.getTileGroupSiblings(groupId)!!.size <= 2) {
            RxDialogs.confirm(this, deviceName,
                    getString(R.string.mobile_device_and_group_delete_text),
                    getString(R.string.mobile_delete),
                    getString(R.string.mobile_cancel))
                    .observeOn(Schedulers.io())
                    .flatMap{ confirmed ->
                            if (confirmed) {
                                userService.deleteEmptyGroup(groupId).map { confirmed }
                            } else {
                                Observable.just(confirmed)
                            }
                    }

        } else {
            RxDialogs.confirm(this, deviceName,
                    getString(R.string.mobile_device_delete_text),
                    getString(R.string.mobile_delete),
                    getString(R.string.mobile_cancel))
                    .observeOn(Schedulers.io())
        }

        obs.flatMap{
                    if (it) {
                        userService.deleteTile(deviceId).andThen(Observable.just(true))
                    } else {
                        analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_DELETE_CANCEL_EVENT, profileId)
                        Observable.empty()
                    }
                }
                .compose(RxComposers.applyIoRefresh(ProgDialog.getLoader(this)))
                .subscribe(
                        {
                            Timber.d("ISA: got $it")
                            navigateToHomeAndFinish()

                            analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_DELETE_FINISH_EVENT,
                                    profileId,
                                    EventStatus.Success,
                                    "Success")
                        },
                        {
                            Timber.d(it, "Error deleting tile")
                            FeedbackMessages.showMessage(editText, getString(R.string.devices_delete_error))

                            analyticsTracker.trackDeviceAction(AnalyticsEvents.EDIT_DEVICE_DELETE_FINISH_EVENT,
                                    profileId,
                                    EventStatus.Error,
                                    it.message)
                        }
                )

    }

    /**
     * Go to home activity and send result OK to interface activity (so that it can be destroyed)
     *
     * Send extra to update devices list on home activity
     */
    private fun navigateToHomeAndFinish() {
        startActivity(navigator.newTilesWithRefresh())
        setResult(Activity.RESULT_OK)
        finish()
    }

    /**
     * Go back and send result OK to interface activity (so that it can be updated)
     */
    private fun goBackAndUpdate(newTitle: String) {
        setResult(Activity.RESULT_OK, Intent().putExtra(Constants.INTERFACE_TITLE, newTitle))
        finish()
    }

    private fun isIntentDataValid(): Boolean =
        intent?.run{
            deviceName = getStringExtra(Constants.EXTRA_DEVICE_NAME)
            deviceId = getStringExtra(Constants.EXTRA_DEVICE_ID)
            profileId = getStringExtra(Constants.EXTRA_PROFILE_ID)

            deviceName != null && deviceId != null
        } ?: false

    private fun isNameValid(): Boolean {
        val newName = editText.text.toString()
        return !newName.isEmpty()
    }


}
