package com.muzzley.app.cards;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.core.graphics.ColorUtils;

import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.cards.Card;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Stage;
import com.muzzley.util.Time;
import com.muzzley.util.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by caan on 25-09-2015.
 */
public class StageContainer extends LinearLayout implements Container<Stage>{

    private Stage stage;
//    static Typeface typeface;

    public StageContainer(Context context) {
        super(context);
        init(context);
    }

    public StageContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StageContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        //FIXME: is this the best place ? should we inflate instead ?
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        setOrientation(LinearLayout.VERTICAL);
    }

    public void setContainerData(Stage stage){
        this.stage = stage;
        final CardContainer cc = CardContainer.from(StageContainer.this);
        Card card = cc.card;


        LayoutInflater inflater = LayoutInflater.from(getContext());
        View stageHeader = inflater.inflate(R.layout.adapter_item_stage_header, this, false);
        addView(stageHeader);
        load(stageHeader,R.id.image,stage.graphics.image);
        load(stageHeader,R.id.icon,stage.graphics.icon);

        ImageView feedbackIv = (ImageView) stageHeader.findViewById(R.id.feedbackIv);
        TextView timestamp = (TextView) stageHeader.findViewById(R.id.timestamp);
        TextView title = (TextView) stageHeader.findViewById(R.id.title);
        TextView stageTitle = (TextView) stageHeader.findViewById(R.id.stage_title);

        int titleColor = Color.parseColor("#" + card.colors.main.title);
        int textColor = Color.parseColor("#" + card.colors.main.text);
        int stageTitleColor = ColorUtils.setAlphaComponent(textColor, 180); //70%

        if (card.title != null) {
            String sep = "";
            if (stage.text != null && stage.text.title != null) {
                sep += ": ";
            }
            title.setText(card.title+sep);
            title.setTextColor(titleColor);
        } else {
            title.setVisibility(GONE);
        }
        Date timestampDate = card.updated;
        if (timestampDate == null) {
            timestampDate = card.created;
        }
        if (timestampDate != null) {
            timestamp.setVisibility(VISIBLE);
            timestamp.setTextColor(titleColor);
            boolean is24 = cc.preferencesRepository.getPreferences() == null || cc.preferencesRepository.getPreferences().is24hours();
            timestamp.setText(Time.getLocalizedTimestamp(timestampDate, getResources().getConfiguration().locale, is24));
        } else {
            timestamp.setVisibility(GONE);
        }
        stageTitle.setTextColor(stageTitleColor);
        if (stage.text != null) {
            stageTitle.setText(stage.text.title);
        }

        Drawable drawable = feedbackIv.getDrawable().mutate();
        drawable.mutate().setColorFilter(textColor, PorterDuff.Mode.SRC_IN);

        ((TextContainer) stageHeader.findViewById(R.id.text)).setContainerData(stage.text);

        HashMap<String, String> feedbackMap = new HashMap<>();
        feedbackMap.put("askMeLater",getContext().getString(R.string.mobile_askmelater));
        feedbackMap.put("dontShowThis",getContext().getString(R.string.mobile_dontshowthis));
        feedbackMap.put("keepShowingThis",getContext().getString(R.string.mobile_keepshowingthis));

        final ArrayList<String> options = new ArrayList<>();
        final ArrayList<String> keys = new ArrayList<>();
        for (String feedback : card.feedback) {
            String string = feedbackMap.get(feedback);
            if (string != null) {
                options.add(string);
                keys.add(feedback);
            }
        }

        if (options.size() > 0) {
            feedbackIv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new AlertDialog.Builder(getContext())
                            .setItems(options.toArray(new String[]{}), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    cc.feedback(keys.get(i));
                                }
                            }).create().show();

                }
            });
        } else {
            feedbackIv.setVisibility(INVISIBLE);
        }

        if (stage.fields != null)
        for(Field field: stage.fields) {

            //FIXME: perhaps we should move textview to each layout
            if (field.label != null) {
                TextView label = (TextView) inflater.inflate(R.layout.adapter_item_field_label, this, false);
                label.setText(field.label);
                addView(label);
            }


            int layout = getResources().getIdentifier("adapter_item_field_"+field.type.replace('-','_'),"layout",getContext().getPackageName());
            if (layout > 0) {
                View v = inflater.inflate(layout, this, false);
                addView(v);
                ((Container) v).setContainerData(field);
            }
        }
        if (stage.actions != null && !stage.actions.isEmpty()) {
//            ActionsContainer ac = new ActionsContainer(getContext());
            ActionsContainer ac = (ActionsContainer) inflater.inflate(R.layout.cards_action_container, this, false);
            addView(ac);
            ac.setContainerData(stage.actions);
        } else {
            View view = new View(getContext());
            view.setMinimumHeight(Utils.px(getContext(),40));
            addView(view);
        }
    }

    void load(View v, int id, String field) {
        ImageView image = (ImageView) v.findViewById(id);
        if (!TextUtils.isEmpty(field)) {
            Picasso.get()
                    .load(field)
//                    .fit().centerCrop()
//                    .error(R.drawable.placeholder_muzzley)
                    .into(image);
        } else {
            image.setVisibility(GONE);
        }

    }

    public Stage getStage() {
        return stage;
    }

}
