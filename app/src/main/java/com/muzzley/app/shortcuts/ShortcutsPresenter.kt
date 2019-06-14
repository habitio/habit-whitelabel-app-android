package com.muzzley.app.shortcuts

import androidx.recyclerview.widget.RecyclerView
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.ViewModel
import timber.log.Timber
import javax.inject.Inject

class ShortcutsPresenter
    @Inject constructor(
            val shortcutsInteractor: ShortcutsInteractor
    ){



    var shortcutVMS: List<ViewModel>? = null
    var adapter: RecyclerView.Adapter<*>? = null


    fun executeShortcut(shortcutVM: ShortcutVM) {

        val showState =  { state: ShortcutVM.State -> showShortcutState(shortcutVM,state) }

        shortcutVM.shortcut?.let {
            shortcutsInteractor.executeShortcut(it)
                    .compose(RxComposers.applyIoCompletable())
                    .doOnSubscribe { showState(ShortcutVM.State.running) }
                    .subscribe(
                            {
                                showState(ShortcutVM.State.shortcut)
                            },
                            {
                                showState(ShortcutVM.State.error)
                                Timber.d(it, "Error executing shortcut");
                            }
                    )
        }

    }

    fun showShortcutState(shortcutVM: ShortcutVM , state: ShortcutVM.State ) {
        shortcutVMS?.let { vms ->
            vms.indexOf( shortcutVM ).let { i->
                Timber.d("index: $i, Thread: ${Thread.currentThread().name}")
                (vms[i] as ShortcutVM).state = state;
                adapter?.notifyItemChanged(i)
            }
        }
    }

//    fun setShortcutVMS(viewModels: List<ViewModel>) {
//        shortcutVMS = viewModels
//    }

}