package com.muzzley.app.workers

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import android.webkit.ConsoleMessage
import android.webkit.GeolocationPermissions
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.R
import com.muzzley.app.shortcuts.ShortcutsActivity
import com.muzzley.app.tiles.ModelsStore
import com.muzzley.model.workers.Contact
import com.muzzley.model.workers.ContactList
import com.muzzley.model.channels.Channel
import com.muzzley.model.tiles.Tile
import com.muzzley.services.LocationInteractor
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.*
import com.muzzley.util.webview.WebViewClient
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.Observable
import kotlinx.android.synthetic.main.activity_worker_webview.*
import timber.log.Timber
import javax.inject.Inject


class WorkerWebviewActivity : AppCompatActivity() , Webview2WorkersBridge.Workable{

    @Inject lateinit var locationInteractor: LocationInteractor
    @Inject lateinit var modelsStore: ModelsStore
    @Inject lateinit var preferencesRepository: PreferencesRepository

    private var workerType: String? = null
    private var actionBarText: String? = null
    var tiles: List<Tile>? = null
    var channels: List<Channel>? = null

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)
        if (modelsStore.modelsStates == null) {
            finish()
            return
        }
        setContentView(R.layout.activity_worker_webview)

        actionBarText = intent.getStringExtra(Constants.EXTRA_DEVICE_PICKER_ACTIONBAR_TEXT)
        workerType = intent.getStringExtra(Constants.EXTRA_DEVICE_PICKER_DEVICE_SEARCH_TYPE)
        tiles = intent.getObjectExtra(Constants.EXTRA_DEVICE_PICKER_WEBVIEW_TILE)
        channels= intent.getObjectExtra(Constants.EXTRA_DEVICE_PICKER_WEBVIEW_CHANNEL)

        configActionBar()
        startWebView()
    }

    private fun startWebView() {
        val par = mapOf(
//                url: getResources().getString(R.string.workers_url_v2),
//                options: [
                        "type" to workerType,
                        "version" to "2.1",
                        "apiVersion" to "v3",
                        "tiles" to tiles,
                        "channels" to channels,
                        "capabilities" to preferencesRepository.muzzCapabilities,
//                        "capabilities" to muzzCapabilitiesPreference.asJson,
//                        "preferences" to gson.toJsonTree(preferencesRepository.preferences),
                        "preferences" to preferencesRepository.preferences,
//                        "rules" to gson.fromJson("[]", JsonElement.class) //TODO add rules when editing agent
                        "rules" to emptyList<String>()
        )
//        ]
        loadWebView(par.toJsonString())
    }


    fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
        locationInteractor.requestLocation(this)
                .subscribe({ locationUsable ->
                        Timber.d("Location active: $locationUsable")
                        callback.invoke(origin,locationUsable,false)
                }, {
                        Timber.e(it,"Error getting permission or location")
                        callback.invoke(origin,false,false)
                })
    }

    private fun loadWebView(params: String) {

        val webChromeClient = object: WebChromeClient() {
            override
            fun onGeolocationPermissionsShowPrompt(origin: String, callback: GeolocationPermissions.Callback) {
                this@WorkerWebviewActivity.onGeolocationPermissionsShowPrompt(origin,callback)
            }
            override
            fun onConsoleMessage(cm: ConsoleMessage): Boolean {
                Timber.d(cm.sourceId() + " [" + cm.lineNumber() + "] " + cm.message())
                return true
            }

            override fun onConsoleMessage(message: String, lineNumber: Int, sourceId: String) {
                onConsoleMessage(ConsoleMessage(message, sourceId, lineNumber, ConsoleMessage.MessageLevel.DEBUG))
            }

        }

        val bridge = Webview2WorkersBridge(webview, params, modelsStore, this)
        webview.apply {
            settings.apply {
                setSupportZoom(false)
                javaScriptEnabled = true
                allowFileAccess = true
                cacheMode = WebSettings.LOAD_DEFAULT
                setGeolocationEnabled(true)
            }
            isLongClickable = false
            isHapticFeedbackEnabled = false
            isVerticalScrollBarEnabled = false
            isVerticalFadingEdgeEnabled = false
            webViewClient = WebViewClient(this@WorkerWebviewActivity, progressBar)
            setWebChromeClient(webChromeClient)
            addJavascriptInterface(bridge,"android")

            loadUrl(getResources().getString(R.string.workers_url))
        }


    }

    override
    fun onRuleSaved(rule: String) {
        Timber.d("New rule $rule")
        finishActivity(rule)
    }

    override
    fun onGetContacts(): Observable<Any> =
        RxPermissions(this).request(Manifest.permission.READ_CONTACTS)
        .flatMap {
            if (!it) {
                Observable.error(RuntimeException("Permission not granted"))
            } else {
                Observable.just(getNewContactList())
            }
        }


    private fun finishActivity(result: String) {
        if(intent != null) {
            if(DevicePickerActivity.ACTION_SHORTCUT_CREATE == intent.action) {
                startActivityForResult<RulesBuilder>(ShortcutsActivity.SHORTCUT_CREATE_CODE) {
                    putExtra(RulesBuilder.EXTRA_TYPE, RulesBuilder.TYPE_SHORTCUT)
                    putExtra(Constants.DEVICE_PICKER_ID_INTENT_RESULT, result)
                }
                return
            }
        }
        setResult(RESULT_OK, Intent().putExtra(Constants.DEVICE_PICKER_ID_INTENT_RESULT, result))
        finish()
    }

    override
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (resultCode == RESULT_OK) {
            when (requestCode) {
                ShortcutsActivity.SHORTCUT_CREATE_CODE -> {
                        setResult(RESULT_OK, data)
                        finish ()
                    }
            }
        }
    }

    private fun configActionBar() {
        supportActionBar?.apply {
            title = actionBarText
            setDisplayHomeAsUpEnabled(true)
            setHomeButtonEnabled(true)
        }
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    fun getNewContactList(): ContactList {
        Timber.e("getting contact list")
        val nameToNumber: MutableList<Pair<String,String>> = mutableListOf()

        contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI)?.use { phones ->

            val nameColumn = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneColumn = phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (phones.moveToNext()) {
                Timber.e("hasNext")
                val name: String = phones.getString(nameColumn)
                val phoneNumber: String = phones.getString(phoneColumn).replace("""\s+""".toRegex(), "")
                nameToNumber.add(name to phoneNumber)
            }
        }
        Timber.e("returning contact list")
        return ContactList(nameToNumber.groupBy({ it.first }, { it.second }).map { Contact(it.key, it.value) })
    }

}


