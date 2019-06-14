package com.muzzley.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.muzzley.App
import com.muzzley.Constants
import com.muzzley.Navigator
import com.muzzley.R
import com.muzzley.app.userprofile.AboutActivity
import com.muzzley.app.userprofile.FeedbackActivity
import com.muzzley.app.userprofile.SettingsActivity
import com.muzzley.app.userprofile.UserVM
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.picasso.BlurTransform
import com.muzzley.util.picasso.CircleProfileEditBorderTransform
import com.muzzley.util.startActivity
import com.muzzley.util.ui.ViewModel
import com.muzzley.util.ui.ViewModelAdapter
import com.muzzley.util.ui.toUri
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_user_profile2.*
import kotlinx.android.synthetic.main.layout_user_profile_data.*
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by ruigoncalo on 25/08/15.
 */
typealias X = (UserVM) -> Unit
class UserFragment : Fragment() {

    init {
        Timber.d("constructor called")
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            UserFragment()
    }

    @Inject lateinit var preferencesRepository: PreferencesRepository
    @Inject lateinit var signUserController: SignUserController
    @Inject lateinit var navigator: Navigator

    //    var userListener: UserListener? = null
    lateinit var viewModelAdapter: ViewModelAdapter<ViewModel>

//    fun registerUserListener(userListener: UserListener) {
//        this.userListener = userListener
//    }
//
//    fun unregisterUserListener() {
//        this.userListener = null
//    }

    override
    fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreate(savedInstanceState)
        Timber.d("onCreateView called")
        return inflater.inflate(R.layout.fragment_user_profile2, container, false)
    }


    override
    fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Timber.d("onActivityCreated called ${savedInstanceState != null}")
        App.appComponent.inject(this)
        viewModelAdapter = ViewModelAdapter(activity)
        recyclerView.apply{
            layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL,false)
            setHasFixedSize(false)
            adapter = viewModelAdapter
        }

        setProfileImage()

        val data = mutableListOf(
                section(getString(R.string.mobile_user_profile_header_support)),
                cell(R.string.mobile_feedback_title,R.drawable.icon_support,FeedbackActivity::class.java)
        )
        if (getString(R.string.faq_url).isNotEmpty()) {
            data.add(
                    cell(R.string.mobile_faq,R.drawable.icon_faq,{ _: UserVM ->
                        startActivity<WebViewActivity>{
                            putExtra(WebViewActivity.EXTRA_URL,getString(R.string.faq_url))
                        }
                    })
            )
        }

        data.add(section(getString(R.string.mobile_settings)))
        data.add(cell(R.string.mobile_settings, R.drawable.icon_settings, SettingsActivity::class.java))

        val social= listOf(
                listOf(R.string.mobile_facebook, R.drawable.icon_facebook, R.string.profile_social_facebook),
                listOf(R.string.mobile_twitter, R.drawable.icon_twitter, R.string.profile_social_twitter),
                listOf(R.string.mobile_blog, R.drawable.icon_blog, R.string.profile_social_blog)
        ).filter { !getString(it[2]).isNullOrBlank()} // non-empty urls

        if (social.isNotEmpty()) {
            data.add(section(getString(R.string.mobile_user_profile_header_social)))
            data.addAll(social.map { cell(it[0],it[1] ,it[2])})
        }
        data.addAll(listOf(
                section(getString(R.string.app_name)),
                cell(R.string.mobile_about, R.drawable.icon_about, AboutActivity::class.java),
                cell(R.string.mobile_logout, R.drawable.icon_logout, { _:UserVM ->  logout()})
        ))

        viewModelAdapter.setData(data)

    }

    fun section(label: String) =
        UserVM(layout= R.layout.section, label= label)

    fun cell(@StringRes label: Int , icon: Int =0, action: Any) =
        UserVM(layout= R.layout.cell, label = getString(label), icon = icon, click = onAction(action))


    fun onAction(action: Any) =
        when (action){
            is Int ->  { _: UserVM -> startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(action) )))}
            is Class<*> -> { _: UserVM -> startActivity(Intent(activity, action))}
//            String -> return {startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(action as String)))}
//            Closure -> action as Closure
//            else -> {startActivity(Intent(activity, action as Class<*>))}
            else -> action as X
        }

    fun setProfileImage(){
        profileName.setText(preferencesRepository.user!!.name)

        Picasso.get()
                .load(preferencesRepository.user?.photoUrl.toUri())
//                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
//                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .fit()
                .centerCrop()
                .transform(BlurTransform(context))
                .into(profileContainerBackgroundImage)

        Picasso.get()
                .load(preferencesRepository.user?.photoUrl.toUri())
                .placeholder(R.drawable.avatar_placeholder)
                .error(R.drawable.avatar_placeholder)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .transform(CircleProfileEditBorderTransform(context, 5, false))
                .into(profileImage)

    }
    fun logout() {
        signUserController.onSignOut()
        startActivity(navigator.newGetStartedIntent(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        activity?.finish()
    }
//    private Int getEditModeTargetColor(edit: Boolean) {
//        return getColor(edit ? R.color.profile_placeholder_edit_mode : R.color.profile_placeholder_username_text )
//    }
//
//
//    private fun hideProfileEditOptions() {
//        val backgroundParams: RelativeLayout.LayoutParams = (RelativeLayout.LayoutParams) profileContainerBackgroundImage.getLayoutParams()
//        backgroundParams.height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 239, getResources().getDisplayMetrics())
//        profileContainerBackgroundImage.setLayoutParams(backgroundParams)
//        profileContainerBackgroundColor.setLayoutParams(backgroundParams)
//        setProfileContainerBackground()
//
//    }
//
//    private fun launchNewUserPhotoActivity() {
//        val newPhoto: Intent = Intent(mContext == null ? getContext() : mContext, NewUserPhotoActivity.class)
//        newPhoto.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // TODO: Review this flag
//        startActivityForResult(newPhoto, Constants.REQUEST_PROFILE_NEW_PHOTO)
//    }
//
//
//    private fun setProfileNameColor(hasFocus: Boolean) {
//        if (editModeEnabled) {
//            profileName.setTextColor(getColor(hasFocus ? R.color.black : R.color.profile_placeholder_edit_mode))
//        }
//    }
//
//    private Int getColor(color: Int) {
//        return ContextCompat.getColor(mContext, color)
//    }


}
