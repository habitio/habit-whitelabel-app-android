package com.muzzley.model.profiles

import java.io.Serializable

class Bundle(

        var id: String? = null,
        var name: String? = null,
        var authorizationUrl: String? = null,
        var shopUrl: String? = null,
        var shopDescription: String? = null,
        var failedAuthenticationUrl: String? = null,
        var photoUrlSquared: String? = null,
        var profiles: List<Profile>? = null,
        var tutorial: Tutorial? = null,
        var defaultUsecases: List<Map<*, *>>? = null,
        var summary: Summary? = null,
        var openOauthInBrowser: Boolean? = null,
        var requiredCapability: DiscoveryRequiredCapability? = null
): Serializable
