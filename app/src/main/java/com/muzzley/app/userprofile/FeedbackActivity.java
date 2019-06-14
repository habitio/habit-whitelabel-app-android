package com.muzzley.app.userprofile;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.muzzley.App;
import com.muzzley.R;
import com.muzzley.app.analytics.AnalyticsEvents;
import com.muzzley.app.analytics.AnalyticsTracker;
import com.muzzley.app.analytics.EventStatus;
import com.muzzley.model.ProfileFeedback;
import com.muzzley.model.ProfileFeedbackAnswer;
import com.muzzley.util.FeedbackMessages;
import com.muzzley.util.Utils;
import com.muzzley.util.retrofit.UserService;
import com.muzzley.util.rx.RxComposers;
import com.muzzley.util.ui.ProgDialog;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;
import io.reactivex.annotations.NonNull;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import timber.log.Timber;

/**
 * Created by Paulo on 2/15/2016.
 */
public class FeedbackActivity extends AppCompatActivity implements View.OnClickListener {
    @Inject AnalyticsTracker analyticsTracker;
    @Inject UserService userService;

    @BindView(R.id.placeholder_feedback_container) RelativeLayout mFeedbackContainer;
    @BindView(R.id.placeholder_thoughts_label) TextView mThoughtsLabel;
    @BindView(R.id.checked_thoughts_first_option) CheckedTextView mFeedbackFirstOption;
    @BindView(R.id.checked_thoughts_second_option) CheckedTextView mFeedbackSecondOption;
    @BindView(R.id.checked_thoughts_third_option) CheckedTextView mFeedbackThirdOption;
    @BindView(R.id.checked_thoughts_fourth_option) CheckedTextView mFeedbackFourthOption;
    @BindView(R.id.placeholder_thoughts_edit) EditText mThoughtsEdit;
    @BindView(R.id.placeholder_thank_you_container) RelativeLayout mThankYouContainer;
//    @BindView(R.id.pb_loading_feedback) ProgressBar mLoadingFeedback;
    @BindView(R.id.btn_feedback_submit) Button mFeedbackSubmit;
    @BindView(R.id.placeholder_thank_you_label) TextView mThankYouLabel;
    @BindView(R.id.placeholder_thank_you_message) TextView mThankYouMessage;
    @BindView(R.id.view_flipper) ViewFlipper viewFlipper;
    private boolean isSubmitted = false, isOptionSelected = false;
    private List<CheckedTextView> surveyOptions;
    private int lastOptionSelected = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.appComponent.inject(this);
        setContentView(R.layout.activity_user_profile_feedback);
        ButterKnife.bind(this);
        configActionBar();
        setupQuestionOptions();
        setListeners();
        mFeedbackSubmit.setEnabled(false);
        mThoughtsEdit.clearFocus();

        analyticsTracker.trackSimpleEvent(AnalyticsEvents.FEEDBACK_START_EVENT);
    }

    private void setupQuestionOptions() {
        surveyOptions = new LinkedList<>();
        surveyOptions.add(mFeedbackFirstOption);
        surveyOptions.add(mFeedbackSecondOption);
        surveyOptions.add(mFeedbackThirdOption);
        surveyOptions.add(mFeedbackFourthOption);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checked_thoughts_first_option:
            case R.id.checked_thoughts_second_option:
            case R.id.checked_thoughts_third_option:
            case R.id.checked_thoughts_fourth_option:
                if (lastOptionSelected == v.getId()) {
                    resetOptionSelection();
                    lastOptionSelected = -1;
                    isOptionSelected = false;
                } else {
                    handleFeedbackOptionSelection(v.getId());
                }
                updateSubmitStatus();
                break;
            case R.id.btn_feedback_submit:
                if (isSubmitted) {
                    finish();
                } else {
                    postUserFeedback(compileFeedbackForBackend());
                }
                break;
            case R.id.placeholder_feedback_container:
                Utils.hideSoftKeyboard(this, mFeedbackContainer);
                break;
            default:
                FeedbackMessages.showMessage(v, getString(R.string.mobile_error_text));
                break;
        }
    }

    @OnClick(R.id.button_done)
    void done(){
        finish();
    }

    private void configActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.mobile_feedback_title);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    private void setListeners() {
        mThoughtsEdit.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // do nothing ...
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                // do nothing ...
            }

            @Override public void afterTextChanged(Editable s) {
                updateSubmitStatus();
            }
        });
        mFeedbackFirstOption.setOnClickListener(this);
        mFeedbackSecondOption.setOnClickListener(this);
        mFeedbackThirdOption.setOnClickListener(this);
        mFeedbackFourthOption.setOnClickListener(this);
        mFeedbackSubmit.setOnClickListener(this);
        mFeedbackContainer.setOnClickListener(this);
    }

    private ProfileFeedback compileFeedbackForBackend() {
        List<ProfileFeedbackAnswer> answers = new LinkedList<>();

        List<String> answer = new LinkedList<>();
        String selectedOptionLabel = getFeedbackString();
        if (!"".equals(selectedOptionLabel)) {
            answer.add(selectedOptionLabel);
            ProfileFeedbackAnswer answerSelection = new ProfileFeedbackAnswer("when-use-muzzley-option", answer);
            answers.add(answerSelection);
        }

        answer = new LinkedList<>();
        if (!"".equals(mThoughtsEdit.getText().toString())) {
            answer.add(mThoughtsEdit.getText().toString());
            ProfileFeedbackAnswer answerFreeText = new ProfileFeedbackAnswer("when-use-muzzley-comment", answer);
            answers.add(answerFreeText);
        }

        return new ProfileFeedback("general-survey-201602", answers);
    }

    @NonNull
    private String getFeedbackString() {
        switch (lastOptionSelected) {
            case R.id.checked_thoughts_first_option:
                return "works according needs";
            case R.id.checked_thoughts_second_option:
                return "features not working";
            case R.id.checked_thoughts_third_option:
                return "not intuitive";
            case R.id.checked_thoughts_fourth_option:
                return "more features";
            default:
                return "";
        }
    }

    private void handleFeedbackOptionSelection(int option) {
        CheckedTextView v = (CheckedTextView) findViewById(option);
        if (v != null) {
            resetOptionSelection();
            isOptionSelected = true;
            switch (option) {
                case R.id.checked_thoughts_first_option:
                case R.id.checked_thoughts_second_option:
                case R.id.checked_thoughts_third_option:
                case R.id.checked_thoughts_fourth_option:
                    v.setSelected(true);
//                    v.setBackgroundResource(R.drawable.button_square_grey_profile);
//                    setCardElevation(v, 0);
                    lastOptionSelected = option;
                    break;
                default:
                    lastOptionSelected = -1;
                    isOptionSelected = false;
                    break;
            }
        }
    }

    private void resetOptionSelection() {
        for (CheckedTextView v : surveyOptions) {
            v.setSelected(false);
        }
        lastOptionSelected = -1;
    }

    private void updateSubmitStatus() {
        mFeedbackSubmit.setEnabled(isOptionSelected || !mThoughtsEdit.getText().toString().isEmpty());
    }


    private void showThankYouMessage() {
        viewFlipper.setDisplayedChild(1);
    }


    public void postUserFeedback(ProfileFeedback feedback) {
        userService.postUserSurvey(feedback)
                .compose(RxComposers.applyIoRefreshCompletable(ProgDialog.getLoader(this)))
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        Timber.d("Success sending feedback data to backend!");
                        showThankYouMessage();

                        if (lastOptionSelected != -1)
                            analyticsTracker.trackFeedbackFinish("when-use-muzzley-option", getFeedbackString(), EventStatus.Success, "Success"); }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        Timber.e(throwable, "Error sending feedback");
//                        showFeedbackSending();
                        FeedbackMessages.showMessage(mFeedbackSubmit, getString(R.string.mobile_error_text));
                        analyticsTracker.trackFeedbackFinish("when-use-muzzley-option", getFeedbackString(), EventStatus.Error, throwable.getMessage());
                    }
                });
    }
}
