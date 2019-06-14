package com.muzzley.model.profiles

import java.io.Serializable

data class UrlRequest (
        val url: String,
        val headers: Map<String, String>? = null
) : Serializable
