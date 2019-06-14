package com.muzzley.app.analytics;

import android.content.Context;
import android.util.Log;

import com.muzzley.model.cards.Action;
import com.muzzley.model.cards.Card;
import com.muzzley.services.PreferencesRepository;

import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class AnalyticsTracker {

    class AnalyticsService {
        void track(String tag, Map<String,Object> event){
            //dummy
        }
    }

    AnalyticsService analyticsService = new AnalyticsService();
//    @Inject AnalyticsService analyticsService;
    @Inject PreferencesRepository preferencesRepository;

    private Context mContext;

    @Inject
    public AnalyticsTracker(Context context) {
        mContext = context;
    }

    public void trackSimpleEvent(String tag) {
        analyticsService.track(tag,null);
    }

    public void trackAppStart() {
        com.muzzley.model.User user = preferencesRepository.getUser();

//        if (user != null) {
//            analyticsService.setUser(new com.muzzley.app.analytics.User(
//                    user.getId(),
//                    user.getName(),
//                    user.getEmail()
//            ));
//        }

        analyticsService.track(AnalyticsEvents.APP_START_EVENT,null);
    }

    public void trackAppExit() {
        analyticsService.track(AnalyticsEvents.APP_EXIT_EVENT,null);
    }

    public void trackSignUpStart(String platform) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.AUTH_PLATFORM_PROPERTY, platform);

        analyticsService.track(AnalyticsEvents.SIGN_UP_START_EVENT, event.getAsMap());
    }

    public void trackSignUpFinish(String email, String name, String platform, EventStatus status, String detail) {
//        if (status == EventStatus.Success) {
//            analyticsService.setUser(
//                    new User(
//                            preferencesRepository.getUser().getId(),
//                            preferencesRepository.getUser().getName(),
//                            preferencesRepository.getUser().getEmail()
//                    )
//            );
//        }

        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.USER_EMAIL_PROPERTY, email)
                .addProperty(AnalyticsProperties.USER_NAME_PROPERTY, name)
                .addProperty(AnalyticsProperties.AUTH_PLATFORM_PROPERTY, platform)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(AnalyticsEvents.SIGN_UP_FINISH_EVENT, event.getAsMap());
    }

    public void trackSignInStart(String platform) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.AUTH_PLATFORM_PROPERTY, platform);

        analyticsService.track(AnalyticsEvents.SIGN_IN_START_EVENT, event.getAsMap());
    }

    public void trackSignInFinish(String email, String name, String platform,
                                  EventStatus status, String detail) {

//        if (status == EventStatus.Success) {
//            analyticsService.setUser( new com.muzzley.app.analytics.User(
//                    preferencesRepository.getUser().getId(),
//                    preferencesRepository.getUser().getName(),
//                    preferencesRepository.getUser().getEmail()
//                    )
//            );
//        }

        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.USER_EMAIL_PROPERTY, email)
                .addProperty(AnalyticsProperties.USER_NAME_PROPERTY, name)
                .addProperty(AnalyticsProperties.AUTH_PLATFORM_PROPERTY, platform)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(AnalyticsEvents.SIGN_IN_FINISH_EVENT, event.getAsMap());
    }

    public void trackSignOut(String email) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.USER_EMAIL_PROPERTY, email);

        analyticsService.track(AnalyticsEvents.SIGN_OUT_EVENT, event.getAsMap());
//        analyticsService.reset();

        // Reset common properties
//        JSONObject json = new JSONObject();
//        try {
//            json.put(AnalyticsProperties.APPLICATION_PROPERTY, mContext.getString(R.string.analytics_application_name));
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        analyticsService.registerSuperProperties(json);
    }

    public void trackShortcutAction(String tag, String shortcutId, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.SHORTCUT_ID_PROPERTY, shortcutId)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackShortcutAction(String tag, String shortcutId) {
        TrackEvent event = new TrackEvent(mContext);

        event.addProperty(AnalyticsProperties.SHORTCUT_ID_PROPERTY, shortcutId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackShortcutExecute( String shortcutId, String where, String source) {
        source = Character.toUpperCase(source.charAt(0))+source.substring(1).toLowerCase();
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.SHORTCUT_ID_PROPERTY, shortcutId)
                .addProperty(AnalyticsProperties.WHERE_PROPERTY, where)
                .addProperty(AnalyticsProperties.SOURCE_PROPERTY, source);

        analyticsService.track(AnalyticsEvents.EXECUTE_SHORTCUT_EVENT, event.getAsMap());
    }

    public void trackRoutineAction(String tag) {
        analyticsService.track(tag,null);
    }

    public void trackRoutineAction(String tag, String routineId) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.ROUTINE_ID_PROPERTY, routineId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRoutineAction(String tag, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRoutineAction(String tag, String routineId, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.ROUTINE_ID_PROPERTY, routineId)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRuleAdd(String tag, String profileId, String deviceName) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.DEVICE_NAME_PROPERTY, deviceName)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRuleAdd(String tag, String routineId, String profileId, String deviceName) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.ROUTINE_ID_PROPERTY, routineId)
                .addProperty(AnalyticsProperties.DEVICE_NAME_PROPERTY, deviceName)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRuleAddOnEdit(String tag, String editableProperty, String editableId) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(editableProperty, editableId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRuleDelete(String tag, String profileId, String deviceName) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.DEVICE_NAME_PROPERTY, deviceName)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRuleDelete(String tag, String idPropertyName, String id, String profileId, String deviceName) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(idPropertyName, id)
                .addProperty(AnalyticsProperties.DEVICE_NAME_PROPERTY, deviceName)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackRoutineExecute(String routineId, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.SOURCE_PROPERTY, "Manual")
                .addProperty(AnalyticsProperties.WHERE_PROPERTY, "App")
                .addProperty(AnalyticsProperties.ROUTINE_ID_PROPERTY, routineId)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(AnalyticsEvents.EXECUTE_ROUTINE_EVENT, event.getAsMap());
    }

    public void trackDeviceAction(String tag, String profileId) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackDeviceAction(String tag, String profileId, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(tag, event.getAsMap());
    }

    public void trackForgotPasswordStart(String email) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.USER_EMAIL_PROPERTY, email);

        analyticsService.track(AnalyticsEvents.FORGOT_PASSWORD_START_EVENT, event.getAsMap());
    }

    public void trackForgotPasswordFinish(String email, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.USER_EMAIL_PROPERTY, email)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(AnalyticsEvents.FORGOT_PASSWORD_REQUEST_FINISH_EVENT, event.getAsMap());
    }

    public void trackNavigateTo(String screen) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.SCREEN_PROPERTY, screen);

        analyticsService.track(AnalyticsEvents.NAVIGATE_TO_EVENT, event.getAsMap());
    }

    public void trackDeviceInteraction(String profileId, String interactionProperty, int groupCount,
                                       String interactionView, String interactionSource,
                                       EventStatus status, String detail)
    {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.PROFILE_ID_PROPERTY, profileId)
                .addProperty(AnalyticsProperties.INTERACTION_PROPERTY_PROPERTY, interactionProperty)
                .addProperty(AnalyticsProperties.INTERACTION_VIEW_PROPERTY, interactionView)
                .addProperty(AnalyticsProperties.INTERACTION_SOURCE_PROPERTY, interactionSource)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status)
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        if(groupCount > 0) {
            event.addProperty(AnalyticsProperties.GROUP_COUNT_PROPERTY, groupCount);
        }

        analyticsService.track(AnalyticsEvents.MANUAL_INTERACTION_EVENT, event.getAsMap());
    }

    public void trackFeedbackFinish(String questionId, String optionId, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.QUESTION_ID_PROPERTY, questionId)
                .addProperty(AnalyticsProperties.OPTION_ID_PROPERTY, optionId)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status.toString())
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(AnalyticsEvents.FEEDBACK_FINISH_EVENT, event.getAsMap());
    }

    public void trackGroupAction(String tag, EventStatus status, String detail) {
        TrackEvent event = new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY, status)
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY, detail);

        analyticsService.track(tag, event.getAsMap());
    }

    private TrackEvent getSuggestionEvent(Card card) {
        return new TrackEvent(mContext)
                .addProperty(AnalyticsProperties.SUGGESTION_ID, card.id)
                .addProperty(AnalyticsProperties.SUGGESTION_CLASS, card._class)
                .addProperty(AnalyticsProperties.SUGGESTION_TYPE, card.type);
    }

    public void trackSuggestionView(Card card) {
        analyticsService.track(AnalyticsEvents.SUGGESTION_VIEW, getSuggestionEvent(card).getAsMap());
    }
    public void trackSuggestionUserEngage(Card card,Action action) {
        analyticsService.track(AnalyticsEvents.SUGGESTION_USER_ENGAGE, getSuggestionEvent(card)
                .addProperty(AnalyticsProperties.SUGGESTION_STAGE_ID,card.interaction.destStage) //TODO: check if this is best field
                .addProperty(AnalyticsProperties.SUGGESTION_ACTION_TYPE,action.type)
                .getAsMap()
        );
    }
    public void trackSuggestionFinish(Card card,Action action, boolean successStatus, String detail) {
        analyticsService.track(AnalyticsEvents.SUGGESTION_FINISH, getSuggestionEvent(card)
                .addProperty(AnalyticsProperties.SUGGESTION_ACTION_TYPE,action.type)
                .addProperty(AnalyticsProperties.STATUS_PROPERTY,successStatus ? "Success" : "Error")
                .addProperty(AnalyticsProperties.DETAIL_PROPERTY,successStatus ? "Success" : detail == null ? "Unknown error" : detail)
                .getAsMap()
        );
    }
    public void trackSuggestionHide(Card card,String feedbackId) {
        analyticsService.track(AnalyticsEvents.SUGGESTION_HIDE, getSuggestionEvent(card)
                .addProperty(AnalyticsProperties.SUGGESTION_VALUE,feedbackId)
                .getAsMap()
        );
    }

    public void trackThrowable(Throwable throwable) {
        analyticsService.track(AnalyticsEvents.EXCEPTION,
                new TrackEvent(mContext)
                        .addProperty(AnalyticsProperties.EXCEPTION_CLASS,throwable.getClass().getCanonicalName())
                        .addProperty(AnalyticsProperties.EXCEPTION_MESSAGE,throwable.getMessage())
                        .addProperty(AnalyticsProperties.EXCEPTION_CLASS, Log.getStackTraceString(throwable))
                .getAsMap()
        );
    }



}
