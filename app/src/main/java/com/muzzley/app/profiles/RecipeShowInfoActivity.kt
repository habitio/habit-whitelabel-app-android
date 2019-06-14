package com.muzzley.app.profiles

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.muzzley.App
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.model.profiles.Process
import com.muzzley.model.profiles.RecipeAction
import com.muzzley.util.FeedbackMessages
import com.muzzley.util.ui.loadUrl
import kotlinx.android.synthetic.main.activity_summary.*
import javax.inject.Inject

class RecipeShowInfoActivity : AppCompatActivity() {

    @Inject lateinit var recipeInteractor2: RecipeInteractor2 
    @Inject lateinit var profilesInteractor: ProfilesInteractor 
    @Inject lateinit var recipeNavigator: RecipeNavigator 
    @Inject lateinit var navigator: Navigator 
    @Inject lateinit var recipePresenter: RecipePresenter 

    override
    fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        App.appComponent.inject(this)

        val recipeState = recipeNavigator.recipeState
        val payload = recipeState?.result?.response?.payload

        setContentView(R.layout.activity_summary)

        recipePresenter.setActivity(this)

        ttitle.setText(payload?.title)
        body.setText(payload?.message)

        top_url.loadUrl(payload?.top_image_url)
        bot_url.loadUrl(payload?.bottom_image_url)

        button.setOnClickListener {
            if (recipeState?.result?.next_step != null) {
                recipeInteractor2.nextStep(recipeState)
                        .subscribe(
                        { nextState ->
                            if (nextState.action == RecipeAction.oauth && nextState.result?.response?.payload?.urlRequest == null) {
                                //FIXME: review empty default
                                profilesInteractor.getRecipeAuthorizationUrlRequest(nextState?.result?.response?.payload?.authorization_url ?: "")
                                        .subscribe(
                                            {
                                                nextState.result?.response?.payload?.urlRequest = it
                                                recipeNavigator.navigateTo(this, nextState)
                                            },
                                            this::showError
                                        )

                            } else {
                                recipeNavigator.navigateTo(this, nextState)
                            }
                        } ,
                        this::showError
                )
            } else {
                if (recipeState?.process == Process.mz_wl_add_device_process) {
                    startActivity(navigator.newTilesWithRefresh())
                } else {
                    finish()
                }
            }
//            startActivity(navigator.newTilesWithRefresh())
        }
    }

    fun showError(throwable: Throwable ) {
        FeedbackMessages.showError(button)
    }

    override
    fun onOptionsItemSelected(item: MenuItem ): Boolean =
        recipePresenter.onOptionsItemSelected(item)

}