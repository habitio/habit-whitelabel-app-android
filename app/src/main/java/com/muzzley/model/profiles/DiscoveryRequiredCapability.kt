package com.muzzley.model.profiles

import com.google.gson.annotations.SerializedName

enum class DiscoveryRequiredCapability {
    @SerializedName("discovery-webview") discovery_webview,
    @SerializedName("discovery-recipe") discovery_recipe
}
