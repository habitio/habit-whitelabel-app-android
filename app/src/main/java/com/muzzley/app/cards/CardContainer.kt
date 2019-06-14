package com.muzzley.app.cards

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.muzzley.App
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.model.cards.*
import com.muzzley.providers.BusProvider
import com.muzzley.services.PreferencesRepository
import com.muzzley.services.Realtime
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.MuzzleyCoreService
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.LogCompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import timber.log.Timber
import java.lang.Thread.sleep
import javax.inject.Inject

/**
 * Created by caan on 25-09-2015.
 */
class CardContainer
    @JvmOverloads constructor(
            context: Context,
            attrs: AttributeSet? = null,
            defStyleAttr: Int = 0
    ): LinearLayout(context,attrs,defStyleAttr) , Container<Card> {

    @JvmField
    var card: Card? = null
    @Inject lateinit var channelService: ChannelService 
    @Inject lateinit var userService: UserService 
    @Inject lateinit var realtime: Realtime 
    @Inject lateinit var analyticsTracker: AnalyticsTracker 
    @Inject lateinit var preferencesRepository: PreferencesRepository 
    @Inject lateinit var muzzleyCoreService: MuzzleyCoreService 

    override
    fun onFinishInflate() {
        super.onFinishInflate()
        App.appComponent.inject(this)
    }

    override 
    fun setContainerData(card: Card ) {

        //FIXME: remove previous views if any ?
        
        this.card?.let { 
            Timber.d("same card " + (card.id == it.id) + " " + card.id)
            removeAllViews()
        } ?: Timber.d("card " + card.id)

        this.card = card
        if (!card.tracked) {
            card.tracked = true
            analyticsTracker.trackSuggestionView(card)
        }
        try {
            setBackgroundColor(Color.parseColor("#"+card.colors.main.background))
        } catch (e: Exception ) {
            Timber.d(e, "Invalid Color")
        }
        addStage()
    }

    fun addStage() {
        card?.interaction?.let {
            val stage = it.stages?.getOrNull(it.destStage)
            val  stageContainer = StageContainer(context)
            addView(stageContainer)
            stageContainer.setContainerData(stage)
            it.currStage = it.destStage
        }
    }

    fun gotoStage(action: Action ) {
        card?.let {
            it.interaction.destStage = action.args.nStage
            analyticsTracker.trackSuggestionUserEngage(it,action)
            BusProvider.getInstance().post(CardUpdateEvent(it.id))
        }
    }

    fun delayEvent(event: Any) {
        delay{
            BusProvider.getInstance().post(event)
        }
    }

    fun delay(runnable: () -> Unit) {
        Thread{
                try {
                    sleep(2000)
                    Timber.d("slept 2s")
                } catch (e: InterruptedException ) {
                    e.printStackTrace()
                }
                Timber.d("going to execute runnable")
                runnable()
        }.start()
    }

    fun postUserCardFeedback(cardFeedback: CardFeedback , actionsContainer: ActionsContainer ?, action: Action? ){
        val refreshAfter = cardFeedback.triggeredAction?.refreshAfter ?: false
        val dismissImmediately = actionsContainer == null || action?.type == "dismiss"
        if (dismissImmediately) {
            BusProvider.getInstance().post(CardDismissEvent(cardFeedback.id,refreshAfter))
        }
        userService.postUserCardFeedback(cardFeedback.id, cardFeedback)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(
                {
                    //move UI to SUCCESS and dismiss after 2s
                    if (!dismissImmediately) {
                        actionsContainer?.showState(ActionsContainer.State.SUCCESS)
                        delayEvent(CardDismissEvent(cardFeedback.id, refreshAfter))
                        analyticsTracker.trackSuggestionFinish(card,action,true,null) //FIXME: add action
                    }
                },
                { throwable ->
                    val notRealError = throwable is HttpException && throwable.code() in arrayOf(404, 409)
                    if(!notRealError) {
                        Timber.e(throwable, "Error posting")
                    }
                    if (!dismissImmediately) {
                        actionsContainer?.showState( if (notRealError) ActionsContainer.State.SUCCESS else ActionsContainer.State.ERROR)
                        delay{
                            Timber.d("Runnable executing")
                            if (notRealError) {
                                Timber.d("going to dismiss")
                                BusProvider.getInstance().post(CardDismissEvent(cardFeedback.id,refreshAfter))
                            } else {
                                Timber.d("showing actions again")
                                actionsContainer?.showState(ActionsContainer.State.ACTION)
                            }
                        }
                        analyticsTracker.trackSuggestionFinish(card,action,notRealError,throwable.message) //FIXME: add action
                    }
                }
        )

    }


    fun feedback(key: String) {
        val cardFeedback = CardFeedback().apply {
            feedback = key
            id = card?.id
        }
        analyticsTracker.trackSuggestionHide(card,key)
        postUserCardFeedback(cardFeedback, null, null)
    }


    fun reply(action: Action , actionsContainer: ActionsContainer ) {
        postUserCardFeedback(CardFeedback().apply { triggeredAction = action ; id = card?.id }, actionsContainer, action)
    }

    fun done(action: Action , stage: Stage , actionsContainer: ActionsContainer ) {
        val cardFeedback = CardFeedback().apply { triggeredAction = action ; id = card?.id }
        cardFeedback.fields =  stage.fields.map{ field ->
            Field().apply {
                id = field.id
                type = field.type
                filter = field.filter
                value = if (field.type in arrayOf("multi-choice", "single-choice")) field.placeholder.filter { it.selected } else field.placeholder
            }
        }
        postUserCardFeedback(cardFeedback, actionsContainer, action)
    }

    fun browse(action: Action ) {
//        CardFeedback cardFeedback = CardFeedback()
//        cardFeedback.triggeredAction = action
//        cardFeedback.id = card.id
//        BusProvider.getInstance().post(CardDismissEvent(cardFeedback.id)); //FIXME: maybe we should register bus onCreate/Destroy
//        cardsController.postUserCardFeedback(cardFeedback)
//        getContext().startActivity(Intent(getContext(), WebViewActivity.class).putExtra(WebViewActivity.EXTRA_URL, action.args.url))
        getContext().startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(action.args.url)))
        analyticsTracker.trackSuggestionUserEngage(card,action)
    }

    fun notifyOnClick(action: Action ) {
        if (action.notifyOnClick == true) {
            card?.id?.let { cardId ->

                userService.postUserCardFeedback(cardId, CardFeedback().apply { clickedAction =  action ;  id = cardId})
                        .subscribe({
                            Timber.d("Success notifying")
                        }, {
                            Timber.d(it,"Error notifying")
                        })
            }
        }
    }


    fun pubMqtt(action: Action ) {

        if (action.pubMQTT != null) {
//            JsonElement payload = action.pubMQTT.payload
//            payload.asJsonObject.addProperty("sender",preferencesRepository.getUser().id)
//            realtime.publish(action.pubMQTT.topic,payload)
//                    .compose(RxComposers.<Boolean>applyIo())
//                    .subscribe(LogObserver<Boolean>("sending action to mqtt"))
            muzzleyCoreService.sendProperty(action.pubMQTT.topic, action.pubMQTT.payload)
                    .subscribe(LogCompletableObserver("sending action to mqtt"))
        }
    }

    companion object {
        @JvmStatic
        fun from(view: View ): CardContainer {
            var vp = view.parent
            while (vp != null && vp !is CardContainer) {
                vp = vp.parent
            }
            return vp as CardContainer
        }
    }
}
