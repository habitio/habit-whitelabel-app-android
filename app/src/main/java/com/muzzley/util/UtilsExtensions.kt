package com.muzzley.util

import android.app.Activity
import android.app.NotificationManager
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.wifi.WifiManager
import android.text.SpannedString
import androidx.core.text.HtmlCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Task
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.muzzley.Constants
import com.muzzley.util.retrofit.GmtDateTypeAdapter
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.net.UnknownHostException
import java.util.*

class
UtilsExtensions {
    companion object {
//        val gson = GsonBuilder().setDateFormat(Constants.DATE_FORMAT).create();
        val gson = GsonBuilder()
//        .setDateFormat(Constants.DATE_FORMAT)
        .registerTypeAdapter(Date::class.java,GmtDateTypeAdapter())
        .create()
    }
}

fun Collection<*>?.isNotNullOrEmpty() =
        this != null && this.isNotEmpty()

fun Collection<*>?.isNullOrEmpty() =
        this == null || this.isEmpty()

fun Map<*,*>?.isNotNullOrEmpty() =
        this != null && this.isNotEmpty()

fun <T> Collection<T>?.findIndexValues(p: (T) -> Boolean) =
        this?.foldIndexed(kotlin.collections.mutableListOf()){ i, acc, el ->
            if (p(el)) {
                acc.add(i)
            }
            acc

        } ?: emptyList<Int>()


fun <T> Collection<T>?.get(ints: Collection<Int>) =
        this?.filterIndexed { i, _ ->
            i in ints
        } ?: emptyList()


fun <T> MutableList<T>?.set(ints: Collection<Int>, x: T) = {
    ints?.forEach{
        this?.set(it,x)
    }
}

inline fun <reified T> String?.parseJson() =
        if (this == null) {
            null
        } else {
            UtilsExtensions.gson.fromJson<T>(this,object: TypeToken<T>() {}.type)
        }
fun Any?.toJsonString() =
        UtilsExtensions.gson.toJson(this)


inline fun <reified T : Activity> Context.startActivity(noinline block: (Intent.() -> Unit)? = null ){
    val i = Intent(this,T::class.java)
    if (block != null) {
        i.block()
    }
    startActivity(i)
}

inline fun <reified T : Activity> Activity.startActivityForResult(requestCode: Int, noinline block: (Intent.() -> Unit)? = null ){
    val i = Intent(this,T::class.java)
    if (block != null) {
        i.block()
    }
    startActivityForResult(i,requestCode)
}

inline fun <reified T : Activity> Fragment.startActivityForResult(requestCode: Int, noinline block: (Intent.() -> Unit)? = null ){
    val i = Intent(this.context,T::class.java)
    if (block != null) {
        i.block()
    }
    startActivityForResult(i,requestCode)
}

inline fun <reified T : Activity> Fragment.startActivity(noinline block: (Intent.() -> Unit)? = null ){
    val i = Intent(this.context,T::class.java)
    if (block != null) {
        i.block()
    }
    startActivity(i)
}

fun ContentResolver.query(uri: Uri, projection: Array<String>? = null, selection: String? = null, selectionArgs: Array<String>? = null, sortOrder: String? = null) =
        query(uri,projection,selection,selectionArgs, sortOrder)

fun truthy(obj: Any?) : Boolean =
        when(obj) {
            null -> false
            is Boolean -> obj
            is Double -> obj != 0.0
            is String -> !obj.isBlank()
            is Collection<*> -> obj.isNotEmpty()
            is Iterable<*> -> obj.iterator().hasNext()
            else -> true
        }
inline fun <T> iff(obj: Any?, block: () -> T?): T? {
    return if (truthy(obj)) {
        block()
    } else
        null
}

inline fun <T> ifno(obj: Any?, block: () -> T?): T? {
    return if (!truthy(obj)) {
        block()
    } else
        null
}

inline infix fun <T> T?.ffi(block: () -> T): T {
    return this ?: block()
}

inline fun <reified R> Observable<*>.filterIsInstance(): Observable<R> =
        this.ofType(R::class.java)

fun Disposable.addTo(compositeDisposable: CompositeDisposable?) =
        compositeDisposable?.add(this)

operator fun CompositeDisposable.plusAssign(disposable: Disposable): Unit {
        this.add(disposable)
}

inline fun <reified T> Intent.getObjectExtra(key: String) =
        this.getStringExtra(key).parseJson<T>()

fun Intent.putObjectExtra(key: String, obj: Any) =
        this.putExtra(key,obj.toJsonString())
//fun Context.getNotificationManager() =
//        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.notificationManager
    get() = this.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

val Context.connectivityManager
    get() = this.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

val Context.wifiManager
    get() = this.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

val Context.locationManager
    get() = this.applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager

fun rxerr(s: String, block: ((Throwable)->Unit)? = null) : (Throwable) -> Unit =
        {t: Throwable ->
            when(t) {
                is UnknownHostException -> Timber.e("$s, ${t.message}")
                else -> Timber.e(t,s)
            }
            block?.invoke(t)
        }
fun <T> Task<T>.toObservable() =
        Observable.create<T> { emitter ->
            addOnSuccessListener {
                emitter.onNext(it)
                emitter.onComplete()
            }
            addOnFailureListener {
                emitter.onError(it)
            }
        }

fun String?.fromHtml() =
        this?.let { HtmlCompat.fromHtml(it,HtmlCompat.FROM_HTML_MODE_LEGACY)} ?: SpannedString("")