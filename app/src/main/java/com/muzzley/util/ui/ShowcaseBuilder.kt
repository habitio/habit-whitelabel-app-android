package com.muzzley.util.ui

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import android.view.View
import android.widget.TextView
import com.muzzley.Constants
import com.muzzley.R
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.annotations.NonNull
import io.reactivex.functions.Function
import timber.log.Timber
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView

import java.util.concurrent.TimeUnit

class ShowcaseBuilder {


    companion object {

        @JvmStatic
        fun showcase(activity: Activity, title: String, text: String, button: String, v: View, @StringRes key: Int): Observable<Boolean> =
                shown(activity, key).flatMap {
                    Timber.d("shown: $it, key: ${activity.getString(key)}")
                    if (!it) {
                        showcase(activity, title, text, button, v)
                                .flatMap { showedNow ->
                                    setShown(activity, key, showedNow).map { showedNow } //because what we want to return is if it has shown or not
                                }
                    } else {
                        Observable.just(false)
                    }
                }

        @JvmStatic
        fun showcase(activity: Activity, title: String, text: String, button: String, viewId: Int, @StringRes key: Int): Observable<Boolean> =
                shown(activity, key).flatMap {
                    if (!it) {
                        showcase(activity, title, text, button, activity.findViewById(viewId))
                                .flatMap { showedNow ->
                                    setShown(activity, key, showedNow)
                                            .map { showedNow } //because what we want to return is if it has shown or not, and not if it was saved
                                }
                    } else {
                        Observable.just(false)
                    }
                }

        @JvmStatic
        fun showcase(activity: Activity, title: String, text: String, button: String, target: View): Observable<Boolean> =

                Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
                    try {
                        val listener = object : IShowcaseListener {
                            override
                            fun onShowcaseDisplayed(materialShowcaseView: MaterialShowcaseView) {
                            }

                            override
                            fun onShowcaseDismissed(materialShowcaseView: MaterialShowcaseView) {
                                Timber.d("dismissed")
                                emitter.onNext(true)
                                emitter.onComplete()
                            }
                        }
                        val showcase = MaterialShowcaseView.Builder(activity)
                                .setTarget(target)
                                .setMaskColour(ContextCompat.getColor(activity, R.color.onboardingBackground))
                                .setTitleTextColor(ContextCompat.getColor(activity, R.color.onboardingTextColor))
                                .setContentTextColor(ContextCompat.getColor(activity, R.color.onboardingTextColor))
                                .setDismissTextColor(ContextCompat.getColor(activity, R.color.onboardingTextColor))
                                .setTitleText(title)
                                .setContentText(text)
                                .setDismissText(button)
                                .setDismissOnTouch(true)
                                .setListener(listener)
//                            .setDelay(100) // optional but starting animations immediately in onCreate can make them choppy
                                .build()

                        //FIXME: ugly library hack
                        try {
                            val tv_content = showcase.findViewById(R.id.tv_content) as TextView
                            tv_content.alpha = 1f //undo library obnoxious behaviour
                        } catch (throwable: Throwable) {
                            Timber.d(throwable, "Hack did not work")
                        }
                        showcase.show(activity)
                        Timber.d("shown")
                    } catch (throwable: Throwable) { // could be null or not visible
                        Timber.e(throwable, "error")
                        emitter.onError(throwable)
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread())
                .delaySubscription(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())

        //FIXME: is it overkill to wrap this in observable ?
        //preferences are IO and probably should not be called in MainThread
        //so we should use a Scheduler.io somewhere
        @JvmStatic
        fun shown(context: Context, @StringRes animationFolder: Int): Observable<Boolean> =

                Observable.fromCallable {

                    //keeping this because legacy ...

                    context.getSharedPreferences(Constants.ON_BOARDING_PREFERENCES, Context.MODE_PRIVATE)
                            .getBoolean(String.format("%s_pref", "onboarding/" + context.getString(animationFolder)), false);
                }

        @JvmStatic
        fun setShown(context: Context, @StringRes animationFolder: Int, value: Boolean): Observable<Boolean> =
                Observable.fromCallable {
                    context.getSharedPreferences(Constants.ON_BOARDING_PREFERENCES, Context.MODE_PRIVATE).edit().run {
                        putBoolean(String.format("%s_pref", "onboarding/" + context.getString(animationFolder)), value);
                        commit()
                    }
                }

        @JvmStatic
        fun clearShown(context: Context): Observable<Boolean> =
                Observable.fromCallable {
                    context.getSharedPreferences(Constants.ON_BOARDING_PREFERENCES, Context.MODE_PRIVATE).edit().run {
                        clear()
                        commit()
                    }
                }

    }

}