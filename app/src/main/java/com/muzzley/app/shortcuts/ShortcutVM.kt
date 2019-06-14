package com.muzzley.app.shortcuts

import com.muzzley.model.shortcuts.Shortcut
import com.muzzley.util.ui.ViewModel

import io.reactivex.Observer
import io.reactivex.subjects.Subject

class ShortcutVM constructor(layout: Int, val shortcut: Shortcut?, val execute: Observer<ShortcutVM>?) : ViewModel(layout) {

    var state = State.shortcut
    var edit: Observer<ShortcutVM>? = null
    var delete: Observer<ShortcutVM>? = null
    var drag: Observer<ShortcutVM>? = null
    var editMode: Subject<Boolean>? = null


    enum class State {
        empty, shortcut, running, error
    }
}
