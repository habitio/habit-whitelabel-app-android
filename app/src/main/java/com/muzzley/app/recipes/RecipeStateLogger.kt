package com.muzzley.app.recipes

import io.reactivex.disposables.Disposable
import timber.log.Timber

class RecipeStateLogger(private val sm : StateMachineStore) {

    private var disposable: Disposable? = null
    fun cancel() =
            disposable?.dispose()

    fun run() {
        disposable = sm.listenState()
                .subscribe(
                        { state ->
//                            Timber.d("State :${state::class.java.simpleName}")
                        },
                        {
                            Timber.e(it, "StateMachineError")
                        }
                )
    }
}
