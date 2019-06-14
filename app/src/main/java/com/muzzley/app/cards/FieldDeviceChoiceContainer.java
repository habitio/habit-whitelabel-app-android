package com.muzzley.app.cards;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.greenfrvr.hashtagview.HashtagView;
import com.muzzley.R;
import com.muzzley.model.cards.Field;
import com.muzzley.model.cards.Placeholder;
import com.muzzley.model.channels.Device;
import com.muzzley.providers.BusProvider;
import com.muzzley.providers.MainThreadBus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import butterknife.ButterKnife;
import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by caan on 28-09-2015.
 */
//public class FieldDeviceChoiceContainer extends LinearLayout implements Container<Field>, AutoLabelUI.OnRemoveLabelListener {
public class FieldDeviceChoiceContainer extends LinearLayout implements Container<Field> {

    @BindView(R.id.devices)
    HashtagView tags;
//    AutoLabelUI labels;
    private String uuid = UUID.randomUUID().toString();
    private List<Device> devices;
    private Field field;
    private MainThreadBus bus;
    private ArrayList<String> labelStrings;

    public FieldDeviceChoiceContainer(Context context) {
        super(context);
    }

    public FieldDeviceChoiceContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public FieldDeviceChoiceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        bus.register(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        bus.unregister(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);
        bus = BusProvider.getInstance();
        tags.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                boolean removed = labelStrings.remove(item);
                if (labelStrings.isEmpty()) {
                    tags.setVisibility(View.GONE);
                } else {
                    tags.setData(labelStrings);
                    tags.invalidate();
                }
            }
        });

    }


    @Override
    public void setContainerData(final Field field) {
        this.field = field;
        devices = new ArrayList<>();
        labelStrings = new ArrayList<>();
        if (field.placeholder != null) {
            for (Placeholder ph : field.placeholder) {
                String label = !TextUtils.isEmpty(ph.label) ? ph.label : ph.component;
                labelStrings.add(label);
                devices.add(new Device(ph.remoteId, ph.profileId, ph.component, null, label, true));
            }
            if (labelStrings.size() > 0) {
                tags.setData(labelStrings);
            } else {
                tags.setVisibility(GONE);
            }
        }

    }

    @OnClick(R.id.button_add_device)
    public void getDevices(View view) {
        bus.post(new DeviceEventRequest(devices, field.filter, uuid));
    }

    @Subscribe
    public void onDeviceResult(DeviceEventResponse deviceEventResponse) {
        if (uuid.equals(deviceEventResponse.requestId)) {
            field.placeholder = new ArrayList<>();
            labelStrings.clear();
            devices = deviceEventResponse.devices;
            for (Device dev : deviceEventResponse.devices) {
                Placeholder ph = new Placeholder();
                ph.component = dev.componentId;
                ph.profileId = dev.profileId;
//                ph.remoteId = dev.remoteId;
                ph.label = dev.label;
                ph.classes = dev.classes;
                labelStrings.add(dev.label);
                field.placeholder.add(ph);
            }
            if (labelStrings.size() > 0) {
                tags.setVisibility(VISIBLE);
                tags.setData(labelStrings);
            } else {
                tags.setVisibility(GONE);
            }
        }

    }

}
