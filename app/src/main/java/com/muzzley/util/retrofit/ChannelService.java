package com.muzzley.util.retrofit;

import com.muzzley.model.VersionSupportResponse;
import com.muzzley.model.channels.Channel;
import com.muzzley.model.productDetails.Location;
import com.muzzley.model.productDetails.ProductDetails;
import com.muzzley.model.profiles.Bundles;
import com.muzzley.model.profiles.ChannelTemplates;
import com.muzzley.model.profiles.RecipeMeta;
import com.muzzley.model.profiles.RecipeResult;

import java.util.List;
import java.util.Map;

import io.reactivex.Completable;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import io.reactivex.Observable;

/**
 * Created by ruigoncalo on 09/06/14.
 *
 * @see: UserService
 *
 * Any service that has @Path("user_id") String userId as parameter should be moved to UserService and remove such dependency
 */

public interface ChannelService {

//    @GET(Endpoints.CHANNEL_TEMPLATES)
//    Observable<ChannelTemplates> getChannelTemplates(@Path("appClientId") String appClientId);
    @GET(Endpoints.CHANNEL_TEMPLATES)
    Observable<ChannelTemplates> getChannelTemplates();

//    @Deprecated
//    @Headers("muz-capabilities: discovery-webview=1, discovery-recipe=2")
//    @GET(Endpoints.PROFILES)
//    Observable<ProfilesData> getProfiles();
//
//    @GET(Endpoints.PROFILE)
//    Observable<Profile> getProfile(@Path("profile_id") String profileId);

    @GET(Endpoints.SERVICE_BUNDLES)
    Observable<Bundles> getServiceBundles(); //FIXME: return real object

    @Deprecated
    @GET(Endpoints.PROFILE_AUTHORIZATION)
    Observable<String> getProfileAuthorization(@Path("profile_id") String profileId);

    @GET(Endpoints.PROFILE_AUTHORIZATION)
    Observable<Response<Void>> getProfileAuthorization3(@Path("profile_id") String profileId);

    @GET(Endpoints.TEMPLATE_AUTHORIZATION)
    Observable<Response<Void>> getTemplateAuthorization(@Path(value="path", encoded = true) String relativePath);

    @Deprecated
    @GET(Endpoints.SERVICE_BUNDLE_AUTHORIZATION)
    Observable<String> getServiceBundleAuthorization(@Path("bundle_id") String bundleId);

    @GET(Endpoints.SERVICE_BUNDLE_AUTHORIZATION)
    Observable<Response<Void>> getServiceBundleAuthorization3(@Path("bundle_id") String bundleId);

    @Headers("X-No-Redirection: true")
    @POST(Endpoints.SERVICE_SUBSCRIBE)
    Completable serviceSubscribe(@Path("service_id") String serviceId, @Body Map<?,?> body);

    @GET(Endpoints.PROFILE_CHANNELS)
    Observable<List<Channel>> getProfileChannels(@Path("profile") String profile);

    @GET(Endpoints.VERSION_SUPPORT)
    Observable<VersionSupportResponse> checkVersionSupport(@Path("version") String version);

    @POST(Endpoints.STORE_PRODUCT)
    Observable<ProductDetails> getProductDetail(@Path(value="product_id", encoded = true) String productId, @Body Location body);

    @POST(Endpoints.RECIPE_META)
    Observable<RecipeResult> getRecipeMeta(@Path("recipe_id") String recipeId);

    @POST(Endpoints.RECIPE_EXECUTE)
    Observable<RecipeResult> executeRecipe(@Path(value="entry_point_url", encoded = true) String entryPointUrl,
                                           @HeaderMap Map<String, Object> headers,
                                           @Body Map<String, Object> payload);

}
