package com.muzzley.util.rx

import io.reactivex.Observer
import io.reactivex.annotations.NonNull
import io.reactivex.disposables.Disposable
import timber.log.Timber

class LogObserver<T> @JvmOverloads constructor(private val label: String = "") : Observer<T> {

    override fun onError(e: Throwable) {
        Timber.e(e, "Error $label")
    }

    override fun onComplete() {
        Timber.d("onCompleted $label")
    }

    override fun onSubscribe(@NonNull d: Disposable) {
        Timber.d("onSubscribe $label")
    }

    override fun onNext(t: T) {
        Timber.d("onNext value: " + t.toString() + ", " + label)
    }
}
