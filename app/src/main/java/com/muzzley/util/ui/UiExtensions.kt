package com.muzzley.util.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

fun View.show() { visibility = View.VISIBLE} // refactor to visible()
fun View.hide() { visibility = View.GONE} // refactor to gone()
fun View.gone() { visibility = View.GONE}
fun View.invisible() { visibility = View.INVISIBLE}
fun View.visible(visible: Boolean) {
    visibility =  if (visible) View.VISIBLE else View.GONE
}

fun ImageView.loadUrl(url: String?) =
        Picasso.get()
                .load(url.toUri()) //because it crashes picasso. But null is ok
                .into(this)

fun ImageView.loadUrl(url: String?, transformation: Transformation) =
        Picasso.get()
                .load(url.toUri())
                .transform(transformation)
                .into(this)

fun ImageView.loadUrlFitCenterCrop(url: String?) =
        Picasso.get()
                .load(url.toUri())
                .fit()
                .centerCrop()
                .into(this)

fun ImageView.loadUrlFitCenterCrop(url: String?, transformation: Transformation) =
        Picasso.get()
                .load(url.toUri())
                .fit()
                .centerCrop()
                .transform(transformation)
                .into(this)

fun ImageView.loadUrlFitCenterInside(url: String?, transformation: Transformation) =
        Picasso.get()
                .load(url.toUri())
                .fit()
                .centerInside()
                .transform(transformation)
                .into(this)

fun String?.toUri() =
        if(this == null || this.isBlank()) {
            null
        } else {
            try {
                Uri.parse(this)
            } catch (t: Throwable) {
                null
            }
        }


fun Context.toast(s: String) =
        Toast.makeText(this,s,Toast.LENGTH_LONG).show()

fun ViewGroup.inflate(layout: Int, attachToRoot: Boolean = false) =
        LayoutInflater.from(this.context).inflate(layout,this,attachToRoot)
