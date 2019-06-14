package com.muzzley.app.profiles

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.muzzley.model.profiles.Profile
import com.muzzley.model.profiles.ProfilesData
import com.muzzley.model.Subscription
import com.muzzley.model.profiles.ChannelTemplates.ChannelTemplate
import com.muzzley.model.profiles.UrlRequest
import com.muzzley.services.PreferencesRepository
import com.muzzley.util.RepositoryException
import com.muzzley.util.retrofit.ChannelService
import com.muzzley.util.retrofit.UserService
import io.reactivex.Observable
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber
import javax.inject.Inject

class ProfilesInteractor

    @Inject constructor(
            val channelService: ChannelService,
            val userService: UserService,
            val preferencesRepository: PreferencesRepository,
            val gson: Gson
    )
    {

    fun getProfiles(): Observable<ProfilesData> =
        channelService.getChannelTemplates()
            .map {
                val profs = it.elements.fold(arrayListOf<Profile>()) { acc, el ->
                    if ( el.template.additionable ) {
                        val prof = toProfile(el.template)
                        prof.recipe = el.recipe_id
                        prof.overlay = el.overlay
                        acc.add(prof)
                    }
                    acc
                }
                ProfilesData(profs)
            }

    fun toProfile(channelTemplate: ChannelTemplate): Profile =
            Profile(
                    id = channelTemplate.id,
                    name = channelTemplate.name,
                    photoUrlSquared = channelTemplate.icon,
                    photoUrl = channelTemplate.image,
                    requiredCapability = channelTemplate.required_capability,
                    recipe = channelTemplate.recipe
            )

    fun getProfile(id: String): Observable<Profile> =
        // channelService.getProfile(id) //FIXME: not yet working
//        channelService.getProfiles().map { it.profiles.find { it.id == id}}
        getProfiles().map { it.profiles.find { it.id == id}}
//        jsonBlob.profiles.map { it.profiles.find { it.id == id}}

    fun parseResponseHeaders(response: Response<Void> ): UrlRequest {
        Timber.d("Got response")

        val forwardPrefix = "x-webview-"

        val headers: Map<String,String> = response.headers().names().filter {
            it.toLowerCase().startsWith(forwardPrefix)
        }.fold(mutableMapOf()) { acc, h ->
            acc[h.substring(forwardPrefix.length)] = response.headers().get(h) ?: ""
            acc
        }
        val urlRequest = UrlRequest(url= response.headers().get("Location") ?: error("No Location header found"), headers= headers)
        Timber.d("returning urlRequest: $urlRequest")
//        if (urlRequest.url.isNullOrEmpty()) {
//            throw RuntimeException("No valid url. response.code(): ${response.code()}")
//        }
        return urlRequest
    }

    fun ignoreRedirectError(throwable: Throwable ): Response<Void>  {
        Timber.d("going to process error: ${throwable}")
        if (throwable is HttpException && throwable.code() in listOf(303, 307)) {
            Timber.d("going to return response")
            return throwable.response() as Response<Void>
        } else {
            Timber.d("going to throw")
            throw throwable
        }
    }

        val ire = { throwable: Throwable ->
            Timber.d("going to process error: ${throwable}")
            if (throwable is HttpException && throwable.code() in listOf(303, 307)) {
                Timber.d("going to return response")
                throwable.response() as Response<Void>
            } else {
                Timber.d("going to throw")
                throw throwable
            }
        }


        fun getProfileAuthorizationUrlRequest(profileId: String): Observable<UrlRequest> =
            channelService.getProfileAuthorization3(profileId)
                .onErrorReturn(this::ignoreRedirectError)
                .map(this::parseResponseHeaders)

        //FIXME: make this more generic and not tied to Subscriptions
        //FIXME: should we do this with a converter factory for every request and not just this one?
        fun mapError(t: Throwable) =
            when (t) {
                is HttpException -> {

                    val code = try {
                        val json = gson.fromJson(t.response().errorBody()!!.string(), JsonElement::class.java)
                        if (json.isJsonObject && json.asJsonObject.has("code")) {
                            json.asJsonObject.get("code").asInt
                        } else
                            0
                    } catch (e: Exception) {
                        Timber.d(e, "Error parsing error")
                        0
                    }
                    Observable.error<Subscription>(RepositoryException(t, code))

                }
                else -> Observable.error<Subscription>(t)
            }


    fun postSubscription(subscription: Subscription): Observable<Subscription>  =
        userService.postSubscriptions(subscription)
                .map {
                        //returning a "subscription" but with muzzley channel ids
                        Subscription(
                                subscription.profile,
                                it.map {Subscription.Channel(it["id"] as String,null,null)}
                        )
                    }
//                .onErrorResumeNext(mapError()) //FIXME: should we do this with a converter factory for every request and not just this one?
                .onErrorResumeNext(this::mapError)


    fun getRecipeAuthorizationUrlRequest(relativePath: String ): Observable<UrlRequest> =
        channelService.getTemplateAuthorization(preferencesRepository.authorization?.endpoints?.http + relativePath)
                .onErrorReturn(this::ignoreRedirectError)
                .map(this::parseResponseHeaders)


}