package com.muzzley.app.login

//import com.jaychang.slm.SocialLoginManager

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.os.ResultReceiver
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.rxbinding2.widget.RxTextView
import com.muzzley.App
import com.muzzley.R
import com.muzzley.app.LauncherActivity
import com.muzzley.app.analytics.AnalyticsProperties
import com.muzzley.app.analytics.AnalyticsTracker
import com.muzzley.util.rx.RxComposers
import com.muzzley.util.startActivity
import com.muzzley.util.ui.ProgDialog
import io.reactivex.Observable
import io.reactivex.functions.BiFunction
import kotlinx.android.synthetic.main.activity_login.*
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class LoginActivity : AppCompatActivity(){

    @Inject lateinit var accountInteractor: AccountInteractor
    @Inject lateinit var analyticsTracker: AnalyticsTracker
    
    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        setContentView(R.layout.activity_login)

        button_forgot_password.setOnClickListener{ reset() }
        submitBtn.setOnClickListener{ login()}

        Observable.combineLatest(RxTextView.textChanges(emailEt),RxTextView.textChanges(passwordEt),
        BiFunction<CharSequence,CharSequence,Boolean> { email, password ->
                email.isNotEmpty() && password.isNotEmpty()
        }).subscribe { submitBtn.isEnabled = it }

    }

    fun reset() {
        startActivity<ResetPasswordActivity> {
            putExtra("email",emailEt.text.toString())
        }
    }

    fun login(){
        analyticsTracker.trackSignInStart(AnalyticsProperties.SIGN_TYPE_EMAIL);

        val username = emailEt.text.toString()
        val password = passwordEt.text.toString()

        showLoading(true)
        accountInteractor.auth(username,password)
            .compose(RxComposers.applyIoRefreshCompletable(this::showLoading))
            .subscribe(
                { userSignIn() },
                { handleError(it) }
            )
    }

    override
    fun onDestroy() {
        super.onDestroy()
        showLoading(false)
    }

    private var progressDialog: ProgressDialog? = null
    @Synchronized
    fun showLoading(show: Boolean) {
        if (show) {
            if (progressDialog == null) {
                progressDialog = ProgDialog.show(this)
            }
        } else {
            progressDialog?.dismiss()
            progressDialog = null
        }
    }

    fun userSignIn() {
        //FIXME: smartwatch legacy stuff ? review this !
//        Intent loginIntent = Intent(Constants.ACTION_LOGIN);
//        loginIntent.putExtra(Constants.EXTRA_LOGIN_TYPE, Constants.NORMAL_LOGIN_TYPE);
//        sendBroadcast(loginIntent);

        intent.getParcelableExtra<ResultReceiver>("finisher")?.send(1,Bundle()) //finish calling activity
        finish()
        startActivity<LauncherActivity> { flags = Intent.FLAG_ACTIVITY_CLEAR_TOP }
//        startActivity(Intent(this,LauncherActivity::class.java).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//        startActivity(navigator.newHomeIntent(Intent.FLAG_ACTIVITY_CLEAR_TOP));
//        setResult(RESULT_OK);
//        finish();

//        showLoading(false)
    }

    fun handleError(throwable: Throwable){
        Timber.d(throwable,"Error in login")
        showLoading(false)
        val errorText =
                when (throwable) {
                    is IOException -> getString(R.string.mobile_no_internet_title)
                    is HttpException -> when(throwable.code()) {
                        409, //FIXME: facebook logins that had no password. waiting for error text
                        401 -> getString(R.string.invalid_credentials)
                        else -> getString(R.string.mobile_error_text)
                    }
                    else -> getString(R.string.mobile_error_text)
                }

        Snackbar.make(submitBtn, errorText, Snackbar.LENGTH_LONG).show()

    }

//    @OnClick(R.id.fbLoginButton)
//    private fun loginByFacebook() {
//        analyticsTracker.trackSignInStart(AnalyticsProperties.SIGN_TYPE_FACEBOOK);
//        loginPreference.set(LoginHttpService.TYPE_FACEBOOK);
//
//        SocialLoginManager.getInstance(this)
//                .facebook()
//                .login()
//                .flatMap {
//                    Me me = Me(it.userId, it.profile.name, it.profile.email, it.photoUrl, LoginHttpService.TYPE_FACEBOOK, it.accessToken);
//                    loginService.login(me).toObservable().compose(RxComposers.<User>applyIo())
//                }
//                .subscribe(
//                        { userSignIn() },
//                        {handleError(it) }
//                );
//    }
//
//    @OnClick(R.id.googleLoginButton)
//    private fun loginByGoogle() {
//        analyticsTracker.trackSignInStart(AnalyticsProperties.SIGN_TYPE_GOOGLE);
//        loginPreference.set(LoginHttpService.TYPE_GOOGLE);
//        SocialLoginManager.getInstance(this)
//                .google(getString(R.string.default_web_client_id))
//                .login()
//                .flatMap {
//                    Me me = Me(it.userId, it.profile.name, it.profile.email, it.photoUrl, LoginHttpService.TYPE_GOOGLE, it.accessToken);
//                    loginService.login(me).toObservable().compose(RxComposers.<User>applyIo())
//                }
//                .subscribe(
//                        { userSignIn() },
//                        {handleError(it) }
//                );
//    }



}