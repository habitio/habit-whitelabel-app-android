package com.muzzley.util.ui

import android.app.ProgressDialog
import android.content.Context
import com.muzzley.R
import io.reactivex.functions.Consumer

class ProgDialog {

    companion object {
        @JvmStatic
        @JvmOverloads
        fun show(context:Context , title:CharSequence? =null, message: CharSequence? =null): ProgressDialog  =
            ProgressDialog(context,R.style.ProgressTheme).apply {
                setCancelable(false)
                setIndeterminate(true)

                if (title != null) {
                    setTitle(title)
                }
                if (message != null) {
                    setMessage(message)
                }
                show()
            }

        @JvmStatic
        fun getLoader(context: Context): Consumer<Boolean>  {
            var dialog:ProgressDialog?  = null;
            return Consumer { show ->
                if (dialog == null && show) {
                    dialog = ProgDialog.show(context);
                } else if (dialog != null && !show) {
                    dialog?.dismiss()
                    dialog = null
                }
            }
        }
    }
}