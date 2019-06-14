package com.muzzley.app.workers

import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.muzzley.R
import com.muzzley.model.workers.Worker
import com.muzzley.model.workers.WorkerUnit
import com.muzzley.util.Utils
import com.muzzley.util.fromHtml
import com.muzzley.util.isNotNullOrEmpty
import com.muzzley.util.picasso.RoundedTransformationBuilderCustom
import com.muzzley.util.ui.*
import com.muzzley.util.ui.triStateToggle.Statable
import com.muzzley.util.ui.triStateToggle.TriStateToggle
import com.squareup.picasso.Transformation
import kotlinx.android.synthetic.main.agent_is_compromised.view.*
import kotlinx.android.synthetic.main.agent_is_unable_to_work.view.*
import kotlinx.android.synthetic.main.agent_item.view.*
import kotlinx.android.synthetic.main.agent_not_editable.view.*
import kotlinx.android.synthetic.main.agent_unable_because_capabilities.view.*
import kotlinx.android.synthetic.main.agents_insufficient_permissions.view.*
import kotlinx.android.synthetic.main.agents_location_disabled.view.*
import kotlinx.android.synthetic.main.worker_force_disabled.view.*
import timber.log.Timber

class WorkerViewHolder(view: View, val af: WorkersFragment) : RecyclerView.ViewHolder(view) , WorkersAdapter.Composer {

    val context1 = view.context

    enum class Item { MAIN, NON_EDITABLE}
    enum class Overlay { FORCE_DISABLED, INVALID, MISSING_CAPABILITIES, LOCATION_DISABLED, MISSING_PERMISSIONS, WORKING, DONE, ERROR, DISABLED, NONE }

    fun showItem(state: Item) {
        itemView.main_card.displayedChild = state.ordinal
    }
    fun showOverlay(state: Overlay) {
        if (state == Overlay.NONE) {
            itemView.overlays.hide()
        } else {
            itemView.overlays.show()
            itemView.overlays.displayedChild = state.ordinal
        }
    }

    private fun bindActionImageView(img: ImageView, action: WorkerUnit, color: Int){
        val image = action?.photoUrl ?: af?.modelsStore?.getTileAgents(action.profile,action?.channel,action.component)?.photoUrl
        Timber.d("actionImage: $image")
        if(image != null){
            val transformation: Transformation = RoundedTransformationBuilderCustom(context1)
                    .borderColor(ContextCompat.getColor(context1,color))
                    .borderWidthDp(Utils.dpToPx(context1, 3))
                    .cornerRadiusDp(30f)
                    .oval(true)
                    .build()

//            Picasso.get()
//                    .load(image) //FIXME: v3 stuff
////                    .fit() //TODO: make sure this doesn"t brake anything
//                    .transform(transformation)
//                    .into(img)
            img.loadUrl(image,transformation)
        } else {
            img.setVisibility(View.GONE)
        }
    }

    private fun bindImages(ivs: List<ImageView>, wus: List<WorkerUnit>, color: Int) {
//        for (int i = ivs.size-1; i >= 0; i--) {

        for ( i in ivs.size-1 downTo 0) {
            if (wus.size > i) {
                bindActionImageView(ivs[i], wus[i],color)
                ivs[i].show()
                ivs[i].bringToFront()
            } else {
                ivs[i].hide()
            }
        }
    }

    override
    fun compose(context: Context, worker: Worker, position: Int) {

        // main card
        if (!worker.editable) {
            showItem(Item.NON_EDITABLE)
            itemView.category_not_editable.setText(worker.category)
            if (worker.categoryColor != null) {
                try {
                    itemView.category_not_editable.setTextColor(Color.parseColor("#" +worker.categoryColor))
                } catch (e: Exception) {
                    Timber.e(e, "Invalid categoryColor")
                }
            }
            itemView.label_not_editable.text = worker.label
            itemView.description_not_editable.text = worker.devicesText ?: worker.description
            lastExecuted(itemView.agent_last_time_executed_not_editable,worker)
            if (worker.deletable) {
                itemView.remove_agent_card_not_editable.show()
                itemView.remove_agent_card_not_editable.setOnClickListener { af.deleteAgentDialog(worker) }
            } else {
                itemView.remove_agent_card_not_editable.hide()
            }
            toggle(itemView.toggle_agent_card_not_editable,worker, position)
//            if (worker.devicesText && worker.description) {
            itemView.agent_not_editable_layout.setOnClickListener {

                    AlertDialog.Builder(context1)
                            .setMessage("""
                                    <small><font color="#${worker.categoryColor}">
                                    ${worker.category?.toUpperCase() ?: ""}
                                    </font></small><br /><br />
                                    <b>${worker.devicesText ?: ""}</b><br /><br />
                                    ${worker.description ?: ""}<br /><br />
                                    <font color ="#99a7aa">
                                    <i>${context.getString(R.string.mobile_footer_usecase)}</i></font>
                                    """.fromHtml())
                            .setPositiveButton(R.string.mobile_got_it, null)
                            .show()
                }
//            }
            worker.categoryImage?.let {
                itemView.categoryImage.loadUrl(it)
                itemView.categoryImage.show()
            } ?: itemView.categoryImage.invisible()

            worker.clientImage?.let {
                itemView.clientImage.loadUrl(it)
            } ?: itemView.clientImage.invisible()
            
        } else {
            showItem(Item.MAIN)
            itemView.agent_card_parent.setOnClickListener {
//                if (worker.missingCapabilities.isNotEmpty()) {
//                    showOverlay(Overlay.MISSING_CAPABILITIES)
//                } else {
                    af.onWorkerClick(worker)
//                }
            }
//            if (worker.missingCapabilities.isNotEmpty()) {
//                itemView.agent_card_parent.setOnClickListener { showOverlay(Overlay.MISSING_CAPABILITIES) }
//                itemView.agent_missing_capabilities.text = Html.fromHtml(context.resources.getString(R.string.worker_hardwarecapabilities_html,worker.label))
////                agent_missing_capabilities.setText(Html.fromHtml(
////                        "<b>"+context.resources.getString(R.string.mobile_worker_hardwarecapabilities_title)+"</b><br/>"
////                                +context.resources.getString(R.string.mobile_worker_hardwarecapabilities_text,worker.label)))
//                itemView.agent_capabilities_button.setOnClickListener { showOverlay(Overlay.NONE) }
//                itemView.agent_img_missing_capabilities.show()
//            } else {
//                itemView.agent_card_parent.setOnClickListener { af.onWorkerClick(worker) }
//                itemView.agent_img_missing_capabilities.hide()
//            }


            itemView.name_agent.text = worker.label

            lastExecuted(itemView.agent_last_time_executed,worker)

            worker.triggers.getOrNull(0)?.let { trig ->
                //            val triggerImage = trig.photoUrl ?: af?.modelsStore?.getTileAgents(trig.profile, trig.channel,trig.component)?.photoUrl
                val triggerImage = af?.modelsStore?.getTileAgents(trig.profile, trig.channel,trig.component)?.photoUrl
                Timber.d("triggerImage: $triggerImage")
                itemView.agent_img_trigger.loadUrl(triggerImage)
            }


            worker.actions.let { actions ->
                if (actions.size > 3) {
                    itemView.img_action_more.text = "+" + (actions.size - 3)
                    itemView.img_action_more.show()
                } else {
                    itemView.img_action_more.hide()
                }
                bindImages(listOf(itemView.img_action_one,itemView.img_action_two,itemView.img_action_three),actions,R.color.device_picker_circular_border)
            }


            worker.states.let { states ->

                if (states.isNotNullOrEmpty()) {
                    itemView.agent_card_state_ll.show()
                    bindImages(listOf(itemView.img_state_one,itemView.img_state_two),states,R.color.transparent)
                } else {
                    itemView.agent_card_state_ll.hide()
                }
            }
            itemView.agent_description.text = worker.description.fromHtml()
            itemView.preview_agent_card.setOnClickListener { af.executeWorker(worker) }
            if (worker.deletable) {
                itemView.remove_agent_card.show()
                itemView.remove_agent_card.setOnClickListener { af.deleteAgentDialog(worker) }
            } else {
                itemView.remove_agent_card.hide()
            }
            toggle(itemView.toggle_agent_card,worker, position)
        }

        if (worker.missingCapabilities.isNullOrEmpty()) {
            itemView.agent_img_missing_capabilities.gone()
        }

        // overlays
        if (worker.forceDisabled) {
            showOverlay(Overlay.FORCE_DISABLED)
            itemView.disable_reason.text = worker.forceDisabledMessage ?: context.getString(R.string.mobile_worker_force_disabled_text)
        } else if (worker.invalid) {
            showOverlay(Overlay.INVALID)
            itemView.compromisedLabel.text = context.resources.getString(R.string.mobile_worker_invalid_html, worker.label).fromHtml()
            itemView.delete_agent_button.setOnClickListener { af.deleteAgentDialog(worker) }
//            } else if (worker.missingCapabilities) {
//                showOverlay(Overlay.MISSING_CAPABILITIES)
//                agent_missing_capabilities.setText(Html.fromHtml(context.resources.getString(R.string.agent_missing_capabilities,worker.label)))
//                agent_capabilities_button.setOnClickListener { showOverlay(Overlay.NONE) }
        } else if (worker.missingCapabilities.isNotEmpty()) {
//            itemView.agent_card_parent.setOnClickListener { showOverlay(Overlay.MISSING_CAPABILITIES) }
            showOverlay(Overlay.MISSING_CAPABILITIES)
            itemView.agent_missing_capabilities.text = context.resources.getString(R.string.worker_hardwarecapabilities_html,worker.label).fromHtml()
            itemView.agent_capabilities_button.setOnClickListener { showOverlay(Overlay.NONE) }
            itemView.agent_img_missing_capabilities.show()
        } else if (worker.missingPermissions.isNotNullOrEmpty()) {
            showOverlay(Overlay.MISSING_PERMISSIONS)
            itemView.agent_insufficient_permissions.text = context.resources.getString(R.string.mobile_worker_permission_html,worker.label).fromHtml()
            itemView.agent_update_permissions_button.setOnClickListener { af.onAskPermissions(worker) }
        } else if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) {
            showOverlay(Overlay.MISSING_PERMISSIONS)
            itemView.agent_insufficient_permissions.text = context.resources.getString(R.string.mobile_worker_permission_html,worker.label).fromHtml()
            itemView.agent_update_permissions_button.setOnClickListener { af.onAskNotifications() }
        } else if (worker.locationDisabled) {
            showOverlay(Overlay.LOCATION_DISABLED)
            itemView.agent_location_disabled.text = context.resources.getString(R.string.mobile_worker_location_permission_html,worker.label).fromHtml()
            itemView.agent_update_location.setOnClickListener { af.onAskLocation() }
        } else if (!worker.enabled) {
            showOverlay(Overlay.DISABLED)
        } else {
            when (worker.executionState) {
                WorkerExecutionState.error -> {
                    showOverlay(Overlay.ERROR)
                    itemView.agent_unable_cancel_button.setOnClickListener { showOverlay(Overlay.NONE) }
                    itemView.agent_unable_retry_button.setOnClickListener { af.executeWorker(worker) }
                }
                WorkerExecutionState.running -> {
                    showOverlay(Overlay.WORKING)
                    itemView.agent_card_include_layout_being_tested.show()
                }
                WorkerExecutionState.success ->
                    showOverlay(Overlay.DONE)
                else ->
                    showOverlay(Overlay.NONE)
            }
        }

    }

    fun lastExecuted(tv: TextView, cardsView: Worker) {
        if (cardsView.subtitle?.contains("Never") == true) {
            tv.invisible()
        } else {
            tv.show()
            tv.text = cardsView.subtitle
        }
    }

    fun toggle(toggleBtn: TriStateToggle, cardsView: Worker, position: Int) {
        if (cardsView.allowdisable) {
            toggleBtn.setOnClickListener(null)
            if (cardsView.enabled) {
                toggleBtn.state = Statable.STATE_ON
                itemView.agents_card_transparent_disable_view.invisible()
            } else {
                toggleBtn.state = Statable.STATE_OFF
                itemView.agents_card_transparent_disable_view.show()
            }
            toggleBtn.setOnClickListener { af.onSwitchClick(cardsView) }
        } else {
            toggleBtn.hide()
        }
    }
}

