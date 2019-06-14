package com.muzzley.app.profiles

import android.content.Intent
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.muzzley.App
import com.muzzley.R
import com.muzzley.model.profiles.Bundle
import com.muzzley.util.ui.inflate
import com.muzzley.util.ui.loadUrl
import kotlinx.android.synthetic.main.tutorial_activity.*
import javax.inject.Inject

class TutorialActivity :  AppCompatActivity() {

    @Inject lateinit var bundleFlow: BundleFlow

    var bundle: Bundle? = null
    var steps: Int= 0

    override
    fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.tutorial_activity)

        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }


//        bundle = intent.getSerializableExtra("bundle") as Bundle
        bundle = bundleFlow.bundle
        steps = bundle?.tutorial?.steps?.size ?: 0


        viewPager.adapter = object: PagerAdapter() {

            override
            fun getCount(): Int  {
                return steps!!
            }

            override
            fun isViewFromObject(view: View, obj: Any): Boolean {
                return view == obj
            }

            override
            fun instantiateItem(container: ViewGroup, position: Int): Any {
                val view = container.inflate(R.layout.tutorial_item)
                bundle?.tutorial?.steps?.let {
                    view.findViewById<ImageView>(R.id.image).loadUrl(it[position].url)
                    view.findViewById<TextView>(R.id.description).text = it[position].description
                }
                container.addView(view)
                return view
            }

            override
            fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
                container.removeView(obj as View)
            }

            override
            fun getPageTitle(position: Int): CharSequence {
//                getString(R.string.tutorial_step,position+1)
                return getString(R.string.mobile_step)+" "+(position+1)
            }
        }

        viewPager.addOnPageChangeListener( object: ViewPager.OnPageChangeListener {
            override
            fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

            override
            fun onPageSelected(position: Int) {
                updateButtonsAndTitle()
            }

            override
            fun onPageScrollStateChanged(state: Int) {}
        })
        indicator.setViewPager(viewPager)


        updateButtonsAndTitle()
        arrayOf(prev, next, skip).forEach { it.setOnClickListener(this::buttonClick) }

//        presenter.<TutorialPresenter.TutorialView>attachView(this)
    }

//    @OnClick([R.id.prev, R.id.next, R.id.skip])
    fun buttonClick(view: View) {
        when (view.id) {
            R.id.prev -> viewPager.currentItem -= 1
            R.id.next -> viewPager.currentItem += 1
            R.id.skip -> {
                bundleFlow.currBundleState = BundleFlow.BundleState.tutorial
                BundleNavigator.navigateTo(this,bundleFlow.nextBundleState)
            }
        }
    }

    fun updateButtonsAndTitle() {
        stepTitle.text = viewPager.adapter?.getPageTitle(viewPager.currentItem)
        val last = viewPager.currentItem == steps - 1
        prev.isEnabled = viewPager.currentItem > 0
        next.isEnabled = !last
        skip.setText(if (last) R.string.mobile_finish else R.string.mobile_skip_tutorial )
        skip.setBackgroundResource(if (last) R.drawable.selector_button_footer else R.drawable.selector_button_rounded_transparent)
        skip.setTextColor(ContextCompat.getColor(this,if (last) R.color.white else R.color.blue))
    }

    override
    fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (!bundle?.tutorial?.url.isNullOrBlank()) {
            menuInflater.inflate(R.menu.tutorial, menu)
        }
//        menu.findItem(R.id.info).setEnabled(bundle?.tutorial?.url as boolean)
        return true
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> onBackPressed()
            R.id.info ->
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(bundle?.tutorial?.url)))
//                bundleFlow.info(this);

        }
        return true
    }

}