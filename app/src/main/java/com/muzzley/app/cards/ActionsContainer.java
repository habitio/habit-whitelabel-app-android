package com.muzzley.app.cards;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AlertDialog;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.muzzley.R;
import com.muzzley.model.cards.Action;
import com.muzzley.model.cards.Card;

import java.util.List;

import timber.log.Timber;

/**
 * Created by caan on 28-09-2015.
 */
public class ActionsContainer extends ViewFlipper implements Container<List<Action>> {

    LinearLayout actionsLayout;
    TextView working;

    public enum State { ACTION, WORKING, SUCCESS, ERROR }; //TODO: RETRY

    public ActionsContainer(Context context) {
        super(context);
    }

    public ActionsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        actionsLayout = (LinearLayout) getChildAt(0);
        working = (TextView) findViewById(R.id.working);
    }

    void showState(final State state) {
        setDisplayedChild(state.ordinal());
    }

    @Override
    public void setContainerData(List<Action> action) {
        setBackgroundColor(Color.parseColor("#"+getCardContainer().card.colors.actionBar.background));

        for (Action a : action) {
            setAction(a);
        }
    }

    private void setAction(Action action) {
        TextView tv = (TextView) LayoutInflater.from(getContext()).inflate(R.layout.adapter_item_action_button, this, false);

        Card card = getCardContainer().card;
        int textColor = Color.parseColor("#" + card.colors.actionBar.text);
        int textColorDisabled = ColorUtils.setAlphaComponent(textColor, 75); //30%
        ColorStateList actionTextColor = new ColorStateList(
                new int[][]{new int[]{-android.R.attr.state_enabled}, new int[]{}},
                new int[]{textColorDisabled, textColor}
        );

        int background = Color.parseColor("#" + card.colors.actionBar.background);
        int backgroundPressed = ColorUtils.setAlphaComponent(textColor, 25); //10% textColor ? srsly ?
        StateListDrawable actionBackgroundColor = new StateListDrawable();
        actionBackgroundColor.addState(new int[]{android.R.attr.state_pressed},new ColorDrawable(backgroundPressed));
        actionBackgroundColor.addState(new int[]{},new ColorDrawable(background));

        tv.setText(action.label);
        tv.setTextColor(actionTextColor);
        tv.setBackgroundDrawable(actionBackgroundColor);
        working.setTextColor(textColor);

        switch (action.role) {
            case "primary": tv.setTypeface(tv.getTypeface(),Typeface.BOLD); break;
            case "secondary" : tv.setTypeface(tv.getTypeface(),Typeface.NORMAL); break;
            case "aside":
                tv.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
                tv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                break;
        }
        if (action.icon != null) {
            Timber.d("got icon "+action.icon);
            switch (action.icon) {
                case "info":
                    Drawable drawable = ContextCompat.getDrawable(getContext(), R.drawable.info).mutate();
                    DrawableCompat.setTint(drawable,textColor);
                    tv.setCompoundDrawablesWithIntrinsicBounds(null,null, drawable,null); break;
//                    tv.setCompoundDrawablesWithIntrinsicBounds(0,0,R.drawable.info,0); break;
            }
        }

        actionsLayout.addView(tv);

        if (action.type != null) {
            switch (action.type){
                case "gotoStage": gotoStage(action,tv); break;
                case "reply": reply(action, tv); break;
                case "browse": browse(action, tv); break;
                case "done": done(action, tv); break;
                case "dismiss": reply(action, tv); break; //Triggers same response as reply
                case "showInfo": showInfo(action, tv); break;
//            case "navigate": break;
//            case "createWorker": break;
                default:
                    Timber.d("TODO action.type:"+action.type);
//                    Toast.makeText(getContext(), "TODO action.type:"+action.type, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void browse(final Action action, TextView tv) {
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCardContainer().notifyOnClick(action);
                getCardContainer().pubMqtt(action);
                getCardContainer().browse(action);
            }
        });
    }

    private void gotoStage(final Action action, TextView tv) {
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCardContainer().notifyOnClick(action);
                getCardContainer().pubMqtt(action);
//                getCardContainer().gotoStage(action.args.nStage);
                getCardContainer().gotoStage(action);
            }
        });
    }

    private CardContainer getCardContainer() {
        return CardContainer.from(this);
    }
    private StageContainer getStageContainer() {
        return (StageContainer) getParent();//FIXME: this does not look too robust
    }

    private void reply(final Action action, TextView tv) {

        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showState(State.WORKING);
                getCardContainer().notifyOnClick(action);
                getCardContainer().pubMqtt(action);
                getCardContainer().reply(action,ActionsContainer.this);
            }
        });
    }
    private void done(final Action action, TextView tv) {

        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showState(State.WORKING);
                getCardContainer().notifyOnClick(action);
                getCardContainer().pubMqtt(action);
                getCardContainer().done(action,getStageContainer().getStage(),ActionsContainer.this);
            }
        });
    }

    private void showInfo(final Action action, TextView tv) {
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                getCardContainer().notifyOnClick(action);
                getCardContainer().pubMqtt(action);
                new AlertDialog.Builder(getContext())
                        .setMessage(action.args.infoText)
                        .create()
                        .show();
            }
        });
    }

}
