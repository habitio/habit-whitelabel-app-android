package com.muzzley.util.rx

import io.reactivex.CompletableObserver
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.net.UnknownHostException

class LogCompletableObserver

    @JvmOverloads
    constructor(private val label: String = "") : CompletableObserver {

    override fun onSubscribe(@NonNull d: Disposable) {
        Timber.d("onSubscribe $label")
    }

    override fun onComplete() {
        Timber.d("onComplete $label")
    }

    override fun onError(@NonNull e: Throwable) {
        when (e){
            is UnknownHostException -> Timber.e("onError $label ${e.message}")
            else -> Timber.e(e, "onError $label")
        }

    }
}
