package com.muzzley.app.profiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.muzzley.App
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.model.profiles.Summary
import com.muzzley.util.ui.loadUrl
import kotlinx.android.synthetic.main.activity_summary.*
import javax.inject.Inject

class SummaryActivity : AppCompatActivity() {

    @Inject lateinit var bundleFlow: BundleFlow
    @Inject lateinit var navigator: Navigator


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)

//        var summary = intent?.getSerializableExtra("data") as Summary?
//        if (summary == null) {
//            summary = bundleFlow.bundle?.summary
//            bundleFlow.currBundleState = null // reset state
//        }

        val summary =( intent?.getSerializableExtra("data") as Summary?
                ?: bundleFlow.bundle?.summary).also {bundleFlow.currBundleState = null }

        setContentView(R.layout.activity_summary)

        summary?.let {
            ttitle.text = it.title
            body.text = it.body

            top_url.loadUrl(it.topUrl)
            bot_url.loadUrl(it.botUrl)
        }

        button.setOnClickListener{
            finish()
            startActivity(navigator.newTilesWithRefresh())
        }
    }
}