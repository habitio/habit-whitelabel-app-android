package com.muzzley.app.login

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import com.muzzley.App
import com.muzzley.R
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.app.analytics.EventStatus
import com.muzzley.model.login.ResetData
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.plusAssign
import com.muzzley.util.retrofit.AuthService
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.ui.ProgDialog
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_reset_password3.*
import javax.inject.Inject

class ResetPasswordActivity : AppCompatActivity() {

    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var authService: AuthService

    var email: String = ""
    val disposable = CompositeDisposable()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_reset_password3)

        email = intent.getStringExtra("email") ?: ""
        if (!email.isNullOrBlank()) {
            emailEt.setText(email)
            submitBtn.isEnabled = true
        }
        disposable += RxTextView.textChanges(emailEt).subscribe {
            email = emailEt.text.toString()
            submitBtn.isEnabled = it.isNotEmpty()
        }
        submitBtn.setOnClickListener{ submit()}
        analyticsTracker.trackForgotPasswordStart(email);
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    fun submit() {
        disposable += authService.resetPassword(ResetData(getString(R.string.app_namespace), emailEt.text.toString()))
                .compose(RxComposers.applyIoRefreshCompletable(ProgDialog.getLoader(this)))
                .subscribe( {
                    FeedbackMessages.showMessage(submitBtn, getString(R.string.reset_password_ok));
                    analyticsTracker.trackForgotPasswordFinish(email, EventStatus.Success, "Success");
                },
                { handleError(it); }
        );

    }

    //FIXME: fix this ... this was probably copy pasted from login
    fun handleError(exception: Throwable) {
//        val errorText: String;
//        if (exception instanceof HttpException) {
//            def error = (HttpException) exception
//            if (error?.response?.body) {
//                GsonConverter gsonConverter = new GsonConverter(gson);
//                try {
//                    LoginHttpService.LoginResponse response = (LoginHttpService.LoginResponse) gsonConverter.fromBody(error.response.body, LoginHttpService.LoginResponse);
//                    if(response != null && !response.isSuccess()){
//                        errorText = response.getMessage();
//                    } else {
//                        errorText = getString(R.string.invalid_credentials);
//                    }
//                } catch (ConversionException e){
//                    errorText = getString(R.string.invalid_credentials);
//                }
//
//            } else {
//                errorText = getString( error.kind == RetrofitError.Kind.NETWORK
//                        ? R.string.mobile_no_internet_title: R.string.error_connecting_muzzley)
//            }
//        } else {
            val errorText = getString(R.string.mobile_error_text);
//        }
        Snackbar.make(submitBtn, errorText, Snackbar.LENGTH_LONG).apply {
//            view.setBackgroundColor(ContextCompat.getColor(this@ResetPasswordActivity, R.color.red))
            show()
        }

        analyticsTracker.trackForgotPasswordFinish(email, EventStatus.Error, errorText);

    }
}