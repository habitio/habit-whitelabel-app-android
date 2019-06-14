package com.muzzley.app.userprofile

import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Html
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.App
import com.muzzley.BuildConfig
import com.muzzley.R
import com.muzzley.app.Log
import com.muzzley.app.WebViewActivity
import com.muzzley.app.analytics.AnalyticsEvents
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.util.fromHtml
import com.muzzley.util.startActivity
import kotlinx.android.synthetic.main.activity_user_profile_about.*
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class AboutActivity : AppCompatActivity() {

    @Inject lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_user_profile_about)
        configActionBar()
        bindData()

        setLink(website, R.string.website_url, R.string.mobile_about_website)
        setLink(placeholder_terms_conditions_label, R.string.about_terms_conditions_url, R.string.mobile_about_tc)
        setLink(placeholder_privacy_policy_label, R.string.about_privacy_policy_url, R.string.mobile_about_pp)

        analyticsTracker.trackSimpleEvent(AnalyticsEvents.ABOUT_VIEW_EVENT)

        val disposable = RxView.clicks(placeholder_logo)
                .buffer(2, TimeUnit.SECONDS)
                .filter {
                    it.size >= 5
                }
                .subscribe {
                    Timber.d("Got 5 clicks in under 2s")
                    startActivity<WebViewActivity> {
                        putExtra(WebViewActivity.EXTRA_URL, "file://${Log.logFile.absolutePath}")
                    }

                }
    }

    internal fun setLink(tv: TextView, @StringRes url: Int, @StringRes text: Int) {
        if (TextUtils.isEmpty(getString(url))) {
            tv.visibility = View.GONE
        } else {
            tv.text = String.format("<a href=\"%1\$s\">%2\$s</a>", getString(url), getString(text)).fromHtml()
            tv.setOnClickListener {
                startActivity<WebViewActivity> {
                    putExtra(WebViewActivity.EXTRA_URL, getString(url))
                }
            }

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    private fun configActionBar() {
        supportActionBar?.apply {
            setTitle(R.string.mobile_about)
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    private fun bindData() {
        placeholder_version_label.text = String.format("%s/%s", BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        val calendar = Calendar.getInstance()
        placeholder_copyright_label.text = getString(R.string.mobile_about_copyright, getString(R.string.copyright_start_year), calendar.get(Calendar.YEAR).toString(), getString(R.string.copyright_company_name), getString(R.string.app_name))
        placeholder_read_more_label.text = getString(R.string.mobile_about_privacy, getString(R.string.copyright_company_name))
    }
}
