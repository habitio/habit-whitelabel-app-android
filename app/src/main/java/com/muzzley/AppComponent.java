package com.muzzley;

import com.muzzley.app.ActivityTracker;
import com.muzzley.app.HomeActivity;
import com.muzzley.app.InterfaceActivity;
import com.muzzley.app.InterfaceSettingsActivity;
import com.muzzley.app.LauncherActivity;
import com.muzzley.app.ProfilesActivity;
import com.muzzley.app.UserFragment;
import com.muzzley.app.WebViewActivity;
import com.muzzley.app.analytics.TrackEvent;
import com.muzzley.app.auth.AuthActivity;
import com.muzzley.app.cards.CardContainer;
import com.muzzley.app.cards.CardFragment;
import com.muzzley.app.cards.CardsController;
import com.muzzley.app.cards.DevicePickActivityCards;
import com.muzzley.app.cards.FieldTimeContainer;
import com.muzzley.app.cards.productdetails.ProductDetailsActivity;
import com.muzzley.app.cards.productdetails.ProductDetailsAdapter;
//import com.muzzley.app.connectivity.ConnectivityReceiver;
import com.muzzley.app.location.LocationService;
import com.muzzley.app.login.GetStartedActivity;
import com.muzzley.app.login.LoginActivity;
import com.muzzley.app.login.ResetPasswordActivity;
import com.muzzley.app.login.SignUpActivity;
import com.muzzley.app.notifications.AzureNotificationsHandler;
import com.muzzley.app.notifications.MyFirebaseMessagingService;
import com.muzzley.app.profiles.BundleCardsActivity;
import com.muzzley.app.profiles.RecipeCardsActivity;
import com.muzzley.app.profiles.RecipeOAuthActivity;
import com.muzzley.app.profiles.RecipeShowInfoActivity;
import com.muzzley.app.profiles.SummaryActivity;
import com.muzzley.app.profiles.TutorialActivity;
import com.muzzley.app.receivers.BootReceiver;
import com.muzzley.app.receivers.CallReceiver;
import com.muzzley.app.receivers.LocationReceiver2;
import com.muzzley.app.receivers.SmsReceiver;
import com.muzzley.app.receivers.UpdateReceiver;
import com.muzzley.app.shortcuts.ShortcutWidgetProvider;
import com.muzzley.app.shortcuts.ShortcutsActivity;
import com.muzzley.app.shortcuts.ShortcutsActivity2;
import com.muzzley.app.tiles.EditGroupActivity;
import com.muzzley.app.tiles.TileControlHolder;
import com.muzzley.app.tiles.TileGroupsFragment;
import com.muzzley.app.tiles.TilesController;
import com.muzzley.app.tiles.TilesFragment;
import com.muzzley.app.tiles.TilesHolder;
import com.muzzley.app.userprofile.AboutActivity;
import com.muzzley.app.userprofile.FeedbackActivity;
import com.muzzley.app.userprofile.NewUserPhotoActivity;
import com.muzzley.app.userprofile.PlacesActivity;
import com.muzzley.app.userprofile.SettingsActivity;
import com.muzzley.app.workers.DevicePickerActivity;
import com.muzzley.app.workers.RulesBuilder;
import com.muzzley.app.workers.WorkerWebviewActivity;
import com.muzzley.app.workers.WorkersController;
import com.muzzley.app.workers.WorkersFragment;
import com.muzzley.services.CustomServicesModule;
import com.muzzley.services.ExternalServicesModule;
import com.muzzley.util.ui.ijk.VideoFrame;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by caan on 15-05-2017.
 */
@Singleton
@Component(
        modules = {
                AppModule.class,
                CustomServicesModule.class,
                ExternalServicesModule.class,
        }
)
public interface AppComponent {
    void inject(App app);
    void inject(MyFirebaseMessagingService m);
    void inject(PlacesActivity placesActivity);
    void inject(AzureNotificationsHandler azureNotificationsHandler);
    void inject(ProfilesActivity profilesActivity);
    void inject(TutorialActivity tutorialActivity);
    void inject(BundleCardsActivity bundleCardsActivity);
    void inject(SummaryActivity summaryActivity);
    void inject(AuthActivity authActivity);
    void inject(InterfaceActivity interfaceActivity);
    void inject(InterfaceSettingsActivity interfaceSettingsActivity);
    void inject(BundleCardsActivity.CardRecipeView cardRecipeView);
    void inject(LocationReceiver2 i);
//    void inject(ConnectivityInteractor.Helper helper);
    void inject(LocationService locationService);
    void inject(UpdateReceiver i);
    void inject(CallReceiver i);
    void inject(SmsReceiver i);
    void inject(BootReceiver i);
    void inject(ActivityTracker i);
    void inject(NewUserPhotoActivity i);
    void inject(AboutActivity i);
    void inject(ExternalServicesModule i);
    void inject(CustomServicesModule i);
    void inject(TilesFragment i);
    void inject(TilesController i);
    void inject(TilesHolder i);
    void inject(TileControlHolder i);
    void inject(EditGroupActivity i);
    void inject(TileGroupsFragment i);
    void inject(GetStartedActivity i);
    void inject(LoginActivity i);
    void inject(ResetPasswordActivity i);
    void inject(SignUpActivity i);
    void inject(HomeActivity i);
    void inject(CardFragment i);
    void inject(DevicePickActivityCards i);
    void inject(CardContainer i);
    void inject(CardsController i);
    void inject(FieldTimeContainer i);
    void inject(UserFragment i);
    void inject(SettingsActivity i);
    void inject(DevicePickerActivity i);
    void inject(WorkersFragment i);
    void inject(WorkersController i);
    void inject(WorkerWebviewActivity i);
    void inject(ShortcutWidgetProvider i);
    void inject(ShortcutsActivity i);
    void inject(ShortcutsActivity2 i);
    void inject(LauncherActivity i);
    void inject(RulesBuilder i);
    void inject(ProductDetailsActivity i);
    void inject(ProductDetailsAdapter i);
    void inject(TrackEvent i);
    void inject(WebViewActivity i);
    void inject(FeedbackActivity feedbackActivity);
    void inject(VideoFrame videoFrame);
    void inject(RecipeShowInfoActivity showInfoActivity);
    void inject(RecipeOAuthActivity recipeOAuth);
    void inject(RecipeCardsActivity recipeCardsActivity);
//    void inject(ScheduleInteractor.ConnectionWorker connectionWorker);
//    void inject(ScheduleInteractor.Helper helper);
//    void inject(ConnectivityReceiver connectivityReceiver);
}
