package com.muzzley.app.login

//import com.jaychang.slm.SocialLoginManager

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.LauncherActivity
import com.muzzley.app.analytics.AnalyticsProperties
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.model.Me
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.startActivity
import com.muzzley.util.ui.ProgDialog
import io.reactivex.Observable
import io.reactivex.functions.Function3
import kotlinx.android.synthetic.main.activity_sign_up3.*
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject


class SignUpActivity : AppCompatActivity() {

    @Inject lateinit var analyticsTracker: AnalyticsTracker
    @Inject lateinit var accountInteractor: AccountInteractor

    var username: String? = null
    var email: String? = null
    var password: String? = null

    override
    fun onCreate(savedInstanceState: Bundle ?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_sign_up3)

        Observable.combineLatest(RxTextView.textChanges(nameEt),RxTextView.textChanges(emailEt),RxTextView.textChanges(passwordEt),
                Function3{ u:CharSequence ,e:CharSequence,p:CharSequence ->
                    username = u.toString() ; email = e.toString() ; password = p.toString()
                    u.isNotEmpty() && e.isNotEmpty() && p.isNotEmpty()
                }).subscribe{ submitBtn.isEnabled = it }


        analyticsTracker.trackSignUpStart(AnalyticsProperties.SIGN_TYPE_EMAIL)
        submitBtn.setOnClickListener { submit() }
    }

    fun submit() {

        val error = accountInteractor.validate(emailEt.text.toString(),passwordEt.text.toString())
        if (error != null) {
            FeedbackMessages.showMessage(submitBtn,error)
            return
        }

        val me = Me().also {
            it.name = username
            it.email = email
            it.type = AccountInteractor.TYPE_ACCOUNT
            it.password = password
        }

        showLoading(true)

        accountInteractor.signUp(me)
                .compose(RxComposers.applyIoRefreshCompletable(this::showLoading))
                .subscribe(
                { onUserSignUp() },
                { handleError(it) }
        )
    }

    private var progressDialog: ProgressDialog? = null
    @Synchronized fun showLoading(show: Boolean) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = ProgDialog.show(this as Context)
            }
        } else {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }


    fun onUserSignUp() {
        //FIXME: smartwatch legacy stuff. review this
        sendBroadcast(Intent(Constants.ACTION_LOGIN).putExtra(Constants.EXTRA_LOGIN_TYPE, Constants.EMAIL_LOGIN_TYPE))

        intent.getParcelableExtra<ResultReceiver>("finisher")?.send(1, Bundle()) //finish calling activity
        startActivity<LauncherActivity>{ flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
        finish()
    }

    fun handleError(throwable: Throwable ) {
        Timber.d(throwable,"Error in login")
        val errorText =
                when (throwable) {
                    is IOException -> getString(R.string.mobile_no_internet_title)
                    is HttpException -> getString(if (throwable.response()?.headers()?.get("X-Error") == "1211") R.string.mobile_signup_already_done else R.string.mobile_signup_invalid_text)
                    else -> getString(R.string.mobile_error_text)
                }

        //Status 412: precondition failed, but reason is in "text" field, in english
        // - user already exists
        // - email is not valid
        // - ?
        // server is no longer validating minimum password size

        //FIX for legacy hardcoded messages
//        if (errorText == "Password is too weak. Minimum 6 chars.") {
//            errorText = getString(R.string.mobile_pass_weak)
//        } else if (errorText == "Invalid Email") {
//            errorText = getString(R.string.mobile_email_invalid)
//        } else if (!errorText) {
//            errorText = getString(R.string.mobile_error_text)
//        }

        FeedbackMessages.showMessage(submitBtn,errorText)
    }

    //FIXME: must upgrade to authorize non legacy !
//    @OnClick(R.id.fbLoginButton)
//    private fun loginByFacebook() {
//        loginPreference.set(LoginHttpService.TYPE_FACEBOOK)
//
//        SocialLoginManager.getInstance(this)
//                .facebook()
//                .login()
//                .flatMap {
//            Me me = Me(it.userId, it.profile.name, it.profile.email, it.photoUrl, LoginHttpService.TYPE_FACEBOOK, it.accessToken)
//            loginService.login(me).toObservable().compose(RxComposers.<User>applyIo())
//        }
//        .subscribe(
//                { onUserSignUp() },
//                {handleError(it) }
//        )
//    }
//
//    //FIXME: must upgrade to authorize non legacy !
//    @OnClick(R.id.googleLoginButton)
//    private fun loginByGoogle() {
//        SocialLoginManager.getInstance(this)
//                .google(getString(R.string.default_web_client_id))
//                .login()
//                .flatMap {
//            Me me = Me(it.userId, it.profile.name, it.profile.email, it.photoUrl, LoginHttpService.TYPE_GOOGLE, it.accessToken)
//            loginService.login(me).toObservable().compose(RxComposers.<User>applyIo())
//        }
//        .subscribe(
//                { onUserSignUp() },
//                {handleError(it) }
//        )
//    }

}