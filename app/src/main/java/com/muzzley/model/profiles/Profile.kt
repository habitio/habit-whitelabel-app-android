package com.muzzley.model.profiles

import com.muzzley.model.profiles.DiscoveryRequiredCapability

/**
 * Created by rmgoncalo on 4/29/14.
 */
class Profile(

    var id: String,
    var name: String? = null,
    var provider: String? = null,
    var photoUrl: String? = null,
    var photoUrlSquared: String? = null,
    var overlay: String? = null,
    var resourceUrl: String? = null,
    var authorizationUrl: String? = null,
    var openOauthInBrowser: Boolean? = null,
    var requiredCapability: DiscoveryRequiredCapability? = null,
    var termsUrl: String? = null,
    var occurrences: Int = -1,
    var recipe: String? = null
)
