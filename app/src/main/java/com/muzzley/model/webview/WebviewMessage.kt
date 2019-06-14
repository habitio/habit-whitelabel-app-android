package com.muzzley.model.webview

import com.google.gson.JsonElement

class WebviewMessage (

    val cid: String? = null,
    val rcid: String? = null,
    val data: Data? = null
){

    class Data (

        val h: H? = null,
        val a: String? = null,
        val m: String? = null,
        val s: Boolean? = null,
        val d: D? = null
    ){

        class H (

            val ch: String? = null
        )

        class D (

            val type: String? = null,
            val value: JsonElement? = null,
            val componentId: String? = null,
            val io: String? = null,
            val profile: String? = null,
            val channel: String? = null,
            val component: String? = null,
            val property: String? = null,
            val data: JsonElement? = null,
            val ruleUnit: JsonElement? = null
        )
    }
}
