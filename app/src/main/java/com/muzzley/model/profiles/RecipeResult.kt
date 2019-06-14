package com.muzzley.model.profiles

class RecipeResult  (

    var code: Int? = null,
//    var next_step: NextStep? = null,
    var response: Response? = null
    ): RecipeMeta()
{

    class Response {

        var payload: Payload? = null
    }

    class Payload {

        var action: RecipeAction? = null
        var info_url: String? = null
        var authorization_url: String? = null
        var url: String? = null
        var urlRequest: UrlRequest? = null
        var cancel_type: RecipeCancelType? = null
        var title: String? = null
        var message: String? = null
        var top_image_url: String? = null
        var bottom_image_url: String? = null
        var cancel_local_key: String? = null
        var next_local_key: String? = null
        var navigation_bar_title: String? = null
//v3
        var action_version: String? = null
        var broadcast: Boolean? = null
        var channeltemplate_id: String? = null
        var client_id: String? = null
        var data: String? = null
        var data_converter: String? = null
        var expect_response: Boolean? = null
        var `interface`: String? = null
        var ip: Boolean? = null
        var owner_id: String? = null
        var port: Int? = null
        var prefixLength: Boolean? = null
        var process: String? = null
        var response_type: String? = null
        var ttl: Int? = null
        var devices: List<Any>? = null
        var received_format: String? = null
    }
}
