package com.muzzley.util.rx

import android.content.Context
import androidx.appcompat.app.AlertDialog
import android.view.View
import android.widget.EditText
import com.muzzley.util.Utils
import com.muzzley.util.ui.PasswordEditText
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import timber.log.Timber

class RxDialogs {

    companion object {

//        @JvmStatic
//        @JvmOverloads
//        fun confirm(context: Context, title: String? = null, message: String): Observable<Boolean> =
//                Observable.create(ObservableOnSubscribe<Boolean> { result ->
//                    AlertDialog.Builder(context)
//                            .setTitle(title)
//                            .setMessage(message)
//                            .setPositiveButton(android.R.string.ok) { _, _ ->
//                                result.onNext(true);
//                            }.setOnDismissListener {
//                                Timber.d("onDismiss")
//                                result.onComplete()
//                            }
//                            .create().show();
////            }
//                })
//                        .defaultIfEmpty(false)
//                        .subscribeOn(AndroidSchedulers.mainThread());

        @JvmStatic
        @JvmOverloads
        fun confirm(context: Context, title: CharSequence? = null, message: CharSequence, positive: CharSequence? = null, negative: CharSequence? = null): Observable<Boolean> =
                Observable.create(ObservableOnSubscribe<Boolean> { result ->
                    val builder = AlertDialog.Builder(context)
                            .setTitle(title)
                            .setMessage(message)
                            .setPositiveButton(positive
                                    ?: context.getString(android.R.string.ok)) { _, _ ->
                                result.onNext(true);
                            }
                            .setOnDismissListener {
                                Timber.d("onDismiss");
                                result.onComplete();
                            }
                    if (!negative.isNullOrEmpty()) {
                        builder.setNegativeButton(negative, null) // does nothing, it's just to set cancel text
                    }
                    builder.create().show();
                })
                        .defaultIfEmpty(false)
                        .subscribeOn(AndroidSchedulers.mainThread())

        @JvmStatic
        @JvmOverloads
        fun confirm(context: Context, title: CharSequence, customView: View, positive: CharSequence? = null, negative: CharSequence? = null): Observable<Boolean> =
                Observable.create(ObservableOnSubscribe<Boolean>() { result ->
                    val builder = AlertDialog.Builder(context)
                            .setTitle(title)
                            .setView(customView)
                            .setPositiveButton(positive
                                    ?: context.getString(android.R.string.ok)) { _, _ ->
                                result.onNext(true);
                            }.setOnDismissListener {
                                Timber.d("onDismiss");
                                result.onComplete();
                            }
                    if (!negative.isNullOrEmpty()) {
                        builder.setNegativeButton(negative, null) // does nothing, it's just to set cancel text
                    }
                    builder.create().show();

                })
                        .defaultIfEmpty(false)
                        .subscribeOn(AndroidSchedulers.mainThread());

        @JvmStatic
        fun askText(context: Context, title: String): Observable<String> =
                Observable.create(ObservableOnSubscribe<String> { result ->
                    val editText = EditText(context);
//                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    val px = Utils.px(context, 10);

                    AlertDialog.Builder(context)
                            .setView(editText, px, px, px, px)
                            .setTitle(title)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val s = editText.getText().toString();
                                Timber.d("captured string: " + s);
                                result.onNext(s);
                            }
                            .setOnDismissListener {
                                Timber.d("onDismiss");
                                result.onComplete();
                            }
                            .create().show();
                }).subscribeOn(AndroidSchedulers.mainThread())

        @JvmStatic
        fun askPassword(context: Context, title: String): Observable<String> =
                Observable.create(ObservableOnSubscribe<String> { result ->
                    val editText = PasswordEditText(context)
                    //                editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)
                    val px = Utils.px(context, 10);

                    AlertDialog.Builder(context)
                            .setView(editText, px, px, px, px)
                            .setTitle(title)
                            .setPositiveButton(android.R.string.ok) { _, _ ->
                                val s = editText.getText().toString();
                                Timber.d("captured string: " + s);
                                result.onNext(s);
                            }
                            .setOnDismissListener {
                                Timber.d("onDismiss");
                                result.onComplete();
                            }
                            .create().show();
                }).subscribeOn(AndroidSchedulers.mainThread())
    }
}