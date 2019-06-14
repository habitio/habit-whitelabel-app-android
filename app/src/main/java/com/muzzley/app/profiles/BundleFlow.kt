package com.muzzley.app.profiles

import com.muzzley.model.profiles.Profile
import com.muzzley.model.profiles.Bundle
import com.muzzley.util.isNotNullOrEmpty
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BundleFlow
    @Inject constructor() {

    var currBundleState: BundleState? = null
    var bundle: Bundle? = null
    var profile: Profile? = null
    var position: Int? = null

//    fun getNextBundleState(): BundleState =
    val nextBundleState: BundleState
        get() =
        bundle?.let {
            // fallthrough switch/when hack
            // https://stackoverflow.com/questions/30832215/when-statement-vs-java-switch-statement

//            fun f5() = BundleState.tilesrefresh
            fun f4() = if (it.summary!= null) BundleState.summary else null
            fun f3() = if (it.profiles.isNotNullOrEmpty()) BundleState.profiles else f4()
            fun f2() = if (it.tutorial != null) BundleState.tutorial else f3()
            fun f1() = if (!it.authorizationUrl.isNullOrEmpty()) BundleState.auth else f2()

            when (currBundleState) {
                null -> f1()
                BundleState.auth -> f2()
                BundleState.tutorial -> f3()
                BundleState.profiles -> f4()
                else -> null
//                BundleState.summary ->f5()
//                BundleState.tilesrefresh -> f5() // should not happen

            }
        } ?: BundleState.tilesrefresh

    //TODO: should we set it also ?
//    fun getNextBundleState(): BundleState  {
//        when (currBundleState) {
//            null ->
//                if (!bundle?.authorizationUrl.isNullOrEmpty()) {
//                    return BundleState.auth
//                }
//            case BundleState.auth:
//                if (bundle.tutorial) {
//                    return BundleState.tutorial
//                }
//            case BundleState.tutorial :
//                if (bundle.profiles) {
//                    return BundleState.profiles
//                }
//            case BundleState.profiles :
//                if (bundle.summary) {
//                    return BundleState.summary
//                }
//            case BundleState.summary:
//                return BundleState.tilesrefresh
//
//        }

//    }
    enum class BundleState {
        auth, tutorial, profiles, summary, tilesrefresh
    }

}