package com.muzzley.app.cards;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.muzzley.R;
import com.muzzley.model.cards.Graphics;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

/**
 * Created by caan on 26-09-2015.
 */
@Deprecated
public class GraphicsContainer extends androidx.appcompat.widget.AppCompatImageView implements Container<Graphics>{
    public GraphicsContainer(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setAdjustViewBounds(true);
        setScaleType(ScaleType.FIT_CENTER);
//        setImageResource(R.drawable.placeholder_muzzley);
    }

    public GraphicsContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GraphicsContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @Override
    public void setImageURI(Uri uri) {

        String scheme = uri.getScheme();
        if (scheme == null || !scheme.startsWith("http"))
            super.setImageURI(uri);
        else
            Picasso.get()
                    .load(uri.toString())
//                    .fit().centerCrop()
                    .error(R.drawable.placeholder_muzzley)
                    .into(this);

    }

    @Override
    public void setContainerData(Graphics data) {

        boolean hide = true;
        try {
            if (data != null && data.image != null) {
                setImageURI(Uri.parse(data.image));
                hide = false;
            }
        } catch (Exception e) {
            Timber.e("Invalid graphics " + data);
        }
        setVisibility(hide ? View.GONE : View.VISIBLE);
    }
}
