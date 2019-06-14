package com.muzzley.app.cards;

import android.app.TimePickerDialog;
import android.content.Context;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.muzzley.App;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;
import com.muzzley.services.PreferencesRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by caan on 28-09-2015.
 */
public class FieldTimeContainer extends LinearLayout implements Container<Field> {

    @Inject PreferencesRepository preferencesRepository;

    public FieldTimeContainer(Context context) {
        super(context);
    }

    public FieldTimeContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FieldTimeContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
//        ButterKnife.bind(this, this);
        App.appComponent.inject(this);
    }

    @Override
    public void setContainerData(Field field) {
        final Placeholder ph = field.placeholder.get(0);
        for (int i = 0; i<7; i++) {
            CheckBox cb = (CheckBox) getChildAt(i);
            cb.setChecked(ph.weekDays.get(i));
            cb.setOnClickListener(new CheckBoxListener(ph.weekDays,i));
        }
        //parse time
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(ph.time));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        final int[] time = {cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)};
        final TextView tv = (TextView) getChildAt(8);
        tv.setText(DateFormat.getTimeFormat(getContext()).format(cal.getTime()));
        tv.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hour, int mn) {
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, hour);
                        cal.set(Calendar.MINUTE, mn);
                        tv.setText(DateFormat.getTimeFormat(getContext()).format(cal.getTime()));
                        ph.time = sdf.format(cal.getTime());
                    }
//                }, time[0], time[1], DateFormat.is24HourFormat(getContext())).show();
                }, time[0], time[1], preferencesRepository.getPreferences().is24hours()).show();
            }
        });
    }

    public static class CheckBoxListener implements OnClickListener {
        private final List<Boolean> booleans;
        private final int pos;

        public CheckBoxListener(List<Boolean> booleans,int pos) {
            this.booleans = booleans;
            this.pos = pos;
        }

        @Override
        public void onClick(View view) {
            booleans.set(pos,((CheckBox)view).isChecked());
        }
    }



}
