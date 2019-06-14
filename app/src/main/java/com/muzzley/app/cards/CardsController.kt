package com.muzzley.app.cards

import android.app.Activity
import android.content.Context
import android.net.Uri
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.text.TextUtils
import android.view.View
import com.muzzley.R
import com.muzzley.model.cards.Card
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.retrofit.UserService
import com.muzzley.util.rx.RxDialogs
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Named

class CardsController

    @Inject constructor(
        val userService: UserService,
        @Named("io") val ioScheduler: Scheduler,
        @Named("main") val mainScheduler: Scheduler,
        val context: Context
    ){

    fun getAllCards(exclusions: List<String> ): Observable<MutableList<Card>>  {
        val join = TextUtils.join("\",\"", exclusions);
        Timber.d("exclusions = $join");


        val map = mutableMapOf<String,String>()
        if (exclusions.isNotEmpty()) {
            map["class"] = "nin/[\"$join\"]/j";
        }

//        userServiceMock.getUserCards(map)
        return userService.getUserCards(map)
                .map {
                    it.cards?.forEach {
                        it.interaction.stages.forEach {
                            if (! it?.graphics?.image.isNullOrEmpty())
                                try {

                                    it.graphics.image = Uri.parse(it.graphics.image).buildUpon()
                                            .appendQueryParameter("os", "android")
                                            .appendQueryParameter("size", "xxhdpi") //TODO: fix this hardcoded
                                            .toString();
                                } catch (e: Exception ) {
                                    Timber.e(e, "Invalid graphics uri: " + it.graphics.image);
                                }
                        }
                    }
                    it.cards ?: mutableListOf()
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun anyAutomation(cards: List<Card>): Boolean =
        cards.any { it.type == "automation"}

    //FIXME: quick hack, this should be done cleaner
    fun deleteAutomation(activity: Activity , cards: MutableList<Card>, adapter:ContainerAdapter<*> , clearBtn: View , errorView: View , swipeRefresh: SwipeRefreshLayout) {

        RxDialogs.confirm(activity,null,activity.getString(R.string.mobile_clear_cards_confirmation))
        .filter { it }
        .doOnNext { swipeRefresh.setRefreshing(true) }
        .observeOn(ioScheduler)
        .flatMap { userService.deleteUserCards(mapOf("type" to "automation")).andThen(Observable.just(true)) }
//        .flatMap { Observable.just(true) }
        .observeOn(mainScheduler)
        .subscribe(
                {
                    cards.removeAll { it.type == "automation"}
                    adapter.notifyDataSetChanged()
                    clearBtn.setVisibility(View.GONE)
//                    clearBtn.enabled = false
                    swipeRefresh.setRefreshing(false)
                },
                {
                    Timber.d(it, "UserCards failure");
                    swipeRefresh.setRefreshing(false)
                    FeedbackMessages.showError(errorView);
                }
        )
    }

}
