package com.muzzley.util.ui

import android.graphics.Rect
import android.view.View
import com.jakewharton.rxbinding2.view.RxView


class KeyboardManager{

    /*
    https://stackoverflow.com/a/26964010
     */
    fun showing(view: View) =
            RxView.globalLayouts(view)
                    .map {
                        val r = Rect()
                        view.getWindowVisibleDisplayFrame(r)
                        val screenHeight = view.getRootView().getHeight()

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        val keypadHeight = screenHeight - r.bottom

                        keypadHeight > screenHeight * 0.15  // 0.15 ratio is perhaps enough to determine keypad height.
                    }
                    .distinctUntilChanged()
}

