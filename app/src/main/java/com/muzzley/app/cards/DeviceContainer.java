package com.muzzley.app.cards;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.muzzley.R;
import com.muzzley.model.channels.Device;
import com.squareup.picasso.Picasso;

import butterknife.ButterKnife;
import butterknife.BindView;

/**
 * Created by caan on 01-10-2015.
 */
public class DeviceContainer extends LinearLayout implements Container<Device>,Checkable
{

    @BindView(R.id.image)
    ImageView image;
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.checkbox)
    CheckBox checkBox;
    private Device device;

    public DeviceContainer(Context context) {
        super(context);
    }


    public DeviceContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DeviceContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this, this);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
                if (device != null) {
                    device.checked = isChecked();
                }
            }
        });
    }

    @Override
    public void setContainerData(Device device) {
        this.device = device;
        Picasso.get()
                .load(device.photoUrl)
//                .error(R.drawable.placeholder_muzzley)
                .into(image);

        text.setText(device.label);
        checkBox.setChecked(device.checked);
    }

    @Override
    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }

    @Override
    public boolean isChecked() {
        return checkBox.isChecked();
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }
}
