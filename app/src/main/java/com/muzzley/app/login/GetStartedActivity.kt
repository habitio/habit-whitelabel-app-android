package com.muzzley.app.login

import android.app.Activity
import android.os.Bundle
import android.os.ResultReceiver
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.view.View
import android.widget.TextView
import com.muzzley.App
import com.muzzley.BuildConfig
import com.muzzley.R
import com.muzzley.app.WebViewActivity
import com.muzzley.util.fromHtml
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.startActivity
import com.muzzley.util.ui.hide
import com.muzzley.util.ui.show
import kotlinx.android.synthetic.main.activity_getstarted3.*
import me.saket.bettermovementmethod.BetterLinkMovementMethod
import timber.log.Timber

class GetStartedActivity : AppCompatActivity() {

    lateinit var finisher: ResultReceiver 

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_getstarted3)

        version_label.text = "${BuildConfig.VERSION_CODE}"

        terms.text =
                getString(R.string.mobile_accept_terms_text,
                        String.format("""<a href="%s">%s</a>""",getString(R.string.about_terms_conditions_url), getString(R.string.mobile_about_tc)),
                        String.format("""<a href="%s">%s</a>""",getString(R.string.about_privacy_policy_url), getString(R.string.mobile_about_pp)))
                        .fromHtml()

        BetterLinkMovementMethod.linkifyHtml(terms)
                .setOnLinkClickListener { _: TextView, url2 : String->
                    Timber.d("url: $url2")
                    startActivity<WebViewActivity>{ putExtra(WebViewActivity.EXTRA_URL, url2) }
                     true
                }

        finisher = object : ResultReceiver(null) {
            override
            fun onReceiveResult(resultCode: Int, resultData: Bundle) {
                Timber.d("got receive result")
                finish()
            }
        }

        create_account.setOnClickListener{ createAccount()}
        login.setOnClickListener{ login()}

    }

    fun createAccount(){
        popup<SignUpActivity>()
    }

    fun login() {
        popup<LoginActivity>()
    }

    inline fun <reified T : Activity> popup() {
        if (accepted.isChecked) {
            startActivity<T> {
                putExtra("finisher",finisher)
            }
        } else {
            RxDialogs.confirm(context = this, message = getString(R.string.mobile_must_accept_terms)).subscribe()
        }
    }
}