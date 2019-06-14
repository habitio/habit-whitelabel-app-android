package com.muzzley.app.profiles

import android.content.Context
import androidx.core.content.ContextCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.AttributeSet
import com.muzzley.R
import com.muzzley.app.cards.Container
import com.muzzley.model.profiles.Profile
import com.muzzley.model.channels.Channel
import com.muzzley.util.ui.ViewModelAdapter
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.show
import com.muzzley.util.ui.visible
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.profile_channels_card.view.*
import timber.log.Timber


class BundleItem
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = androidx.cardview.R.attr.cardViewStyle
    ): CardView(context,attrs,defStyleAttr) , Container<BundleVM>{

    var adapter: ViewModelAdapter<ChannelVM>? = null

    override
    fun onFinishInflate() {
        super.onFinishInflate()
        if (!isInEditMode){
            recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false).apply { setAutoMeasureEnabled(true) }
            adapter = ViewModelAdapter(context)
            recyclerView.adapter = adapter
        }
    }

    override
    fun setContainerData(data: BundleVM) {

        Timber.d("AAAAAAAAA3 state = ${data.state} idx: ${data.idx}")
        tryAgain.visible(data.state == AddState.error && data.click != null )
        tryAgain.setOnClickListener(data.click)
//        progress.visible(channels == null && (!data.error || data.state == BundleCardsActivity.AddState.running ) )
//        progress.visible(data.state == BundleCardsActivity.AddState.running )
        progress.visible(data.state  <= AddState.running )
        getLayouts(data.channels,data,data.profile).let {
            data.channelData = it
            adapter?.setData(it)
        }
        val totalSteps = data.totalSteps
        val stepNo = data.stepNo
        if (totalSteps != null && stepNo != null) {
            stepLabel.show()
            stepLabel.text = "$stepNo/$totalSteps"
        } else {
            stepLabel.hide()
        }
        if (data.stepTitle != null) {
            progressText.show()
            progressText.text = data.stepTitle
        } else {
            progressText.hide()
        }
    }

    private fun getLayouts(channels: List<Channel>? ,data: BundleVM , profile: Profile): List<ChannelVM>  {
        val occurrences = profile.occurrences
        header.hide()
        if (channels == null) {
            return List((if (occurrences < 0) 1 else occurrences )) {
                ChannelVM(
                        layout = R.layout.channel_item,
                        photoUrl = profile.photoUrlSquared,
                        label =  profile.name,
                        error =  data.error
                )
            }
//            return listOf(cvm) * (if (occurrences < 0) 1 else occurrences )
        }

        var results = channels.map {
            ChannelVM(
                layout = R.layout.channel_item,
                photoUrl = it.photoUrl ?: profile.photoUrlSquared,
                label = it.content
            )

        }
        val toAdd = if(channels.size == 0 && occurrences == -1)
            1
        else
            occurrences - channels.size

//        header.visible( channels.size > occurrences)

        val validRx = data.validRx //as Observer<Boolean>

        when {
            toAdd == 0 -> validRx.onNext(true)
            toAdd < 1 -> {
                header.show()
                results.forEach { it.selected = false; it.selectedRx = PublishSubject.create() }
                Observable.combineLatest(results.map { it.selectedRx }){ barr ->

                    val selectedCount = barr.count { it == true }
                    if (occurrences < 0 )
                        selectedCount > 0
                    else
                        selectedCount == occurrences
                }.subscribe(
                        { matches ->
                            Timber.d("matches: $matches")
                            header.setTextColor(ContextCompat.getColor(context, if(matches) R.color.black else R.color.red))
                            data.state = if(matches) AddState.finished else AddState.input
                            validRx.onNext(matches)
                        },
                        {
                            Timber.d(it, "error in checkboxes")
                        }
                )
                header.text = if(occurrences < 0 )
                    context.getString (R.string.mobile_device_selection_select_devices)
                else
                    context.getString(R.string.mobile_service_device_selection_number_devices, occurrences.toString())
            }
            toAdd > 0 -> {
                results += List(toAdd){
                    ChannelVM(
                            layout = R.layout.channel_item,
                            photoUrl = profile.photoUrlSquared,
                            label = profile.name,
                            error = RuntimeException("Not enough items")
                    )
                }
                data.state = AddState.error
                tryAgain.show()
                validRx.onNext(false)
            }
        }
        return results
    }

}
