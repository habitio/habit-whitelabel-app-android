package com.muzzley.util

import com.google.android.material.snackbar.Snackbar
import android.view.View

import com.muzzley.R

/**
 * Created by ruigoncalo on 11/03/15.
 */
object FeedbackMessages {

    @JvmStatic
    fun showMessage(view: View?, message: String?) {
        if (view != null) {
            Snackbar.make(view, message?: "", Snackbar.LENGTH_LONG).show()
        }
    }

    @JvmStatic
    fun showMessage(view: View?, messageRes: Int) {
        if (view != null) {
            Snackbar.make(view, messageRes, Snackbar.LENGTH_LONG).show()
        }
    }
    @JvmStatic
    fun showError(view: View?) {
        if (view != null) {
            Snackbar.make(view, R.string.mobile_error_text, Snackbar.LENGTH_LONG).show()
        }
    }

//    fun showMessageWithAction(view: View, duration: Int? = null, message: String?, actionText: String, action: View.OnClickListener) {
//        Snackbar
//                .make(view, message ?: view.resources.getString(R.string.mobile_error_text), duration ?: Snackbar.LENGTH_LONG)
//                .setAction(actionText, action).show()
//    }

    @JvmStatic
    fun showMessageWithAction(view: View, actionText: String, duration: Int? = null, message: String?= null, action: (View?)->Unit) {
        Snackbar
                .make(view, message ?: view.resources.getString(R.string.mobile_error_text), duration ?: Snackbar.LENGTH_LONG)
                .setAction(actionText, action).show()
    }
}
