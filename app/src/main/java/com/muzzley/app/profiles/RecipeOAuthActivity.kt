package com.muzzley.app.profiles

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.muzzley.App
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.LauncherActivity
import com.muzzley.app.recipes.Action
import com.muzzley.app.recipes.RecipeApiMiddleware
import com.muzzley.app.recipes.State
import com.muzzley.app.recipes.StateMachineStore
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.rx.RxDialogs
import com.muzzley.util.startActivity
import com.muzzley.util.ui.ProgDialog
import com.muzzley.util.ui.ProgDialog.Companion.getLoader
import com.muzzley.util.ui.visible
import com.neura.wtf.it
import com.neura.wtf.x
import io.reactivex.functions.Predicate
import kotlinx.android.synthetic.main.oauth.*
import timber.log.Timber
import javax.inject.Inject


class RecipeOAuthActivity : AppCompatActivity() {

    @Inject lateinit var recipeNavigator: RecipeNavigator
    @Inject lateinit var recipeInteractor: RecipeInteractor2
    @Inject lateinit var recipePresenter: RecipePresenter

    @Inject lateinit var channelService: ChannelService
    @Inject lateinit var profilesInteractor: ProfilesInteractor
    @Inject lateinit var navigator: Navigator


    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.d("intent: $intent")

        if (intent.action == Intent.ACTION_VIEW && intent.data?.path?.contains("""/v3/channel-templates/.*/retrieve-devices-list.*""".toRegex()) == true) {
            App.appComponent.inject(this)
            setContentView(R.layout.oauth)
            recipePresenter.setActivity(this)

            (recipeNavigator.state as? State.FetchingResult)?.let { initState ->
                StateMachineStore(initState).let { sm ->
                    RecipeApiMiddleware(sm,channelService,recipeInteractor,profilesInteractor,recipeNavigator,this).apply { run() }
                    //presenter stuff
                    val recipeView = this
                    sm.listenState().subscribe(
                            { state ->
                                when(state) {
                                    is State.Finished -> startActivity(navigator.newTilesWithRefresh())
                                    is State.Step -> {
                                        recipeView.showLoading(false)
                                        when (state.action.recipeState.action) {
                                            null -> sm.onAction(Action.OnResult(state.action.recipeState)) // empty result
//                                            RecipeAction.oauth -> {
//                                                if (state.action.recipeState.result?.response?.payload?.urlRequest == null &&
//                                                        state.action.recipeState.result?.response?.payload?.authorization_url != null) {
//                                                    sm.onAction(Action.QueryAction(state.action.recipeState))
//                                                } else {
//                                                    recipeNavigator.navigateTo(recipeView,state)
//                                                }
//                                            }
                                            RecipeAction.show_info,
                                            RecipeAction.oauth, // maybe this one isn't true
                                            RecipeAction.list_devices,
                                            RecipeAction.udp -> {
                                                finish()
                                                recipeNavigator.navigateTo(recipeView,state)
                                            }
                                            else -> sm.onAction(Action.QueryAction(state.action.recipeState))
                                        }

                                    }
                                    is State.FetchingResult -> recipeView?.showLoading(true)
                                    is State.Result -> recipeView?.showLoading(false)
                                    is State.SendingRequest -> recipeView?.showLoading(true)
                                }
                            },
                            Timber::e
                    )
                    sm.onAction(Action.OnResult(initState.action.recipeState)) //todo: do this last


                }
            }


//            if (recipeNavigator.recipeState?.result?.next_step == null) {
//                finish()
//            } else {
//                nextStep()
//            }
//            return
        } else {

            startActivity<LauncherActivity>()
            finish()
            return
            //TODO: I think this is deprecated and should be deleted


            // old flow when oauth is open in the webview istself
            setContentView(R.layout.oauth)
            recipePresenter.setActivity(this)


            val payload = recipeNavigator.recipeState?.result?.response?.payload

            recipeNavigator.recipeState?.result?.response?.payload?.urlRequest?.let { urlRequest ->
                val predicate = Predicate<String> { string ->
                    //            val uri = Uri.parse(string);
                    Timber.d("Url=$string, package:${this.packageName}");
                    if (!string.startsWith("http")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(string)))
                    }
                    false
//            return (this.getPackageName().endsWith(".dev") || //bypass host validation for dev version
//                    [getString(R.string.api_host),
//                     "channel-api.office.muzzley.com",
//                     "channels.muzzley.com",
//                     "platform.muzzley.com"].any { uri.host.endsWith(it)} ) &&
//                    ("true" == uri.getQueryParameter("success") || ["retrieve-devices-list", "services"].any { uri.path.endsWith(it)})
                }
                oauthView.loadUrlRequest(urlRequest, predicate)
                        .take(1)
                        .subscribe(
                                {
                                    if (recipeNavigator.recipeState?.result?.next_step == null) {
                                        finish()
                                    } else {
                                        nextStep()
                                    }
                                }, this::showError
                        )

            } ?: showError(RuntimeException("No url request !"))
        }
    }

    fun showError(t: Throwable) {
        Timber.e(t,"Error loading request")
        FeedbackMessages.showError(oauthView)
    }

//    val dialog = ProgDialog.getLoader(this)
    fun showLoading(show: Boolean) {
        progress_bar.visible(show)
//        dialog.accept(show)
    }

    fun nextStep() {
        recipeNavigator.recipeState?.let {
            recipeInteractor.nextStep(it)
                    .subscribe(
                            {
                                finish()
                                recipeNavigator.navigateTo(this,it)
                            }
                    ) {
                        RxDialogs.confirm(context = this, message = getString(R.string.mobile_error_text), negative = getString(android.R.string.cancel))
                                .subscribe {
                                    if (it) {
                                        nextStep()
                                    } else {
                                        finish()
                                    }
                                }
                    }
        } ?: showError(RuntimeException("No recipe found"))
    }

    override
    fun onOptionsItemSelected(item: MenuItem): Boolean =
        recipePresenter.onOptionsItemSelected(item)

}
