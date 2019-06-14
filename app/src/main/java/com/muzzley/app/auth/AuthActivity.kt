package com.muzzley.app.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.transition.Scene
import androidx.transition.TransitionManager
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.view.RxView
import com.muzzley.App
import com.muzzley.R
import com.muzzley.app.ProfilesActivity
import com.muzzley.app.profiles.BundleFlow
import com.muzzley.app.profiles.RecipeInteractor
import com.muzzley.app.recipes.RecipeView
import com.muzzley.model.profiles.UrlRequest
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.plusAssign
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.ui.OAuthView2
import com.muzzley.util.ui.toast
import com.muzzley.util.ui.visible
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Predicate
import kotlinx.android.synthetic.main.activity_auth.*
import timber.log.Timber
import javax.inject.Inject
import com.muzzley.util.ui.KeyboardManager
import io.reactivex.disposables.CompositeDisposable
import java.util.concurrent.TimeUnit


class AuthActivity : AppCompatActivity(), RecipeView {

    companion object {
        const val LOCAL_DISCOVERY_PREFIX = "muzdiscovery";
        const val HTTP_PREFIX = "http"
    }

    @Inject lateinit var recipeInteractor: RecipeInteractor
    @Inject lateinit var bundleFlow: BundleFlow
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private var urlRequest: UrlRequest? = null
    private var withFooter = true
    private val disposable = CompositeDisposable()

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this);
        setContentView(R.layout.activity_auth)

        supportActionBar?.apply {
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }

        urlRequest = intent.getSerializableExtra(ProfilesActivity.URL_REQUEST) as? UrlRequest
        if (urlRequest == null) {
            val url = intent.getStringExtra(ProfilesActivity.EXTRA_LOCATION) ?: bundleFlow.bundle?.authorizationUrl
            if (url != null) {
                urlRequest = UrlRequest(url = url)
            }
        }

        //FIXME: legacy stuff
//        if (urlRequest == null) {
//            urlRequest = UrlRequest(url = getIntent().getStringExtra(ProfilesActivity.EXTRA_LOCATION) ?: bundleFlow.bundle?.authorizationUrl)
//        }
        withFooter = getIntent().getBooleanExtra(ProfilesActivity.WITH_FOOTER,true)
        Timber.d("withFooter: $withFooter")

        if (withFooter && !bundleFlow?.bundle?.shopUrl.isNullOrBlank()) {
            val scenes = arrayOf(R.layout.services_footer_collapsed,R.layout.services_footer_expanded).map {
                Scene.getSceneForLayout(sceneRoot,it,this)
            }
            scenes.forEachIndexed { idx : Int, scene: Scene ->

                scene.setEnterAction {
                    val shopDesc = sceneRoot.findViewById<TextView>(R.id.shop)
                    shopDesc?.text = bundleFlow.bundle?.shopDescription
                    sceneRoot.findViewById<View>(R.id.collapse).setOnClickListener {
                        shopDesc?.visibility = View.INVISIBLE
                        TransitionManager.go(scenes[(idx+1) % scenes.size])
                    }
                    sceneRoot.findViewById<TextView>(R.id.label).text = getString(R.string.mobile_activation_serial_dont_have,bundleFlow.bundle?.name)
                    sceneRoot.findViewById<View>(R.id.button).setOnClickListener {
                        try {
                            startActivity(Intent(Intent.ACTION_VIEW).setData(Uri.parse(bundleFlow.bundle?.shopUrl)))
                        } catch (e: Exception) {
                            Timber.e(e,"Error sending to shopUrl")
                        }
                    }
                }
            }

            scenes[1].enter()

            disposable += KeyboardManager().showing(container)
                    .filter{ it }
                    .timeout(5,TimeUnit.SECONDS)
                    .materialize()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { TransitionManager.go(scenes[0]) }

        }

        urlRequest?.let {
            start(it)
        } ?: toast("no url !")
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.clear()
    }

    override
    fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home ->{ finish() ; true }
            else -> super.onOptionsItemSelected(item)
        }

    fun start(urlRequest: UrlRequest) {
        if (urlRequest.url.startsWith(LOCAL_DISCOVERY_PREFIX)) {
            recipeInteractor.view = this
            recipeInteractor.startRecipe(urlRequest.url.replaceFirst(LOCAL_DISCOVERY_PREFIX, HTTP_PREFIX))
        } else {
            //FIXME: handle open in browser
            disposable += getOAuth(urlRequest)
                    .subscribe(
                    { recipeSuccess() },
                    { recipeError(it)}
            )
        }

    }

    fun getOAuth(urlRequest: UrlRequest): Observable<String> =
        Observable.defer{
            container.removeAllViews()
            layoutInflater.inflate(R.layout.oauth,container)
            val predicate  = Predicate<String>{ string : String ->
                val uri = Uri.parse(string);
                Timber.d("Url=$string, package:${this.getPackageName()}");
//                (this.getPackageName().endsWith(".dev") || //bypass host validation for dev version
//                arrayOf(getString(R.string.api_host), "platform.muzzley.com").any { uri.host.endsWith(it)} ) &&
                arrayOf(Uri.parse(preferencesRepository.authorization!!.endpoints!!.http!!).host, "platform.muzzley.com").any { uri.host.endsWith(it)}  &&
                    ("true" == uri.getQueryParameter("success") || arrayOf("retrieve-devices-list", "services").any { uri.path.endsWith(it)})
            }
            container.findViewById<OAuthView2>(R.id.oauthView).loadUrlRequest(urlRequest,predicate)
        }.subscribeOn(AndroidSchedulers.mainThread())


    override
    fun getAuthenticationCode(): Observable<String> =
        Observable.defer{
            container.removeAllViews()
            layoutInflater.inflate(R.layout.activation_code,container)
            container.findViewById<TextView>(R.id.label).text = getString(R.string.mobile_activation_serial_already_have,bundleFlow.bundle?.name)
            val et = findViewById<EditText>(R.id.edit)
            RxView.clicks(findViewById(R.id.submit))
                    .map { et.text.toString() }
//                    .filter { it.length() > 0}
                    .filter{ it.isNotEmpty() }
                    .take(1)
        }.subscribeOn(AndroidSchedulers.mainThread())

    override
    fun recipeSuccess() {
        setResult(RESULT_OK)
        finish()
        //FIXME: should we set it before it is successful ?
//        bundleFlow.setCurrBundleState(BundleFlow.BundleState.auth)
//        BundleNavigator.navigateTo(this,bundleFlow.nextBundleState)
//        startActivity(SummaryActivity)
    }

    override
    fun recipeError(throwable: Throwable) {

        Timber.d(throwable, "Error processing steps")

        disposable += RxDialogs.confirm(this
                ,getString(R.string.mobile_error_title)
                ,getString(R.string.mobile_error_text)
                ,getString(R.string.mobile_retry)
                ,getString(android.R.string.cancel))
        .subscribe {
            if (it) {
                start(urlRequest!!)
            } else {
                finish()
            }
        }
    }

    override
    fun showLoading(b: Boolean) {
        progress.visible(b)
    }

    override
    fun showTotalSteps(totalSteps: Int) {

    }

    override
    fun showStepNumber(stepNo: Int) {

    }

    override
    fun showStepTitle(title: String) {

    }
}