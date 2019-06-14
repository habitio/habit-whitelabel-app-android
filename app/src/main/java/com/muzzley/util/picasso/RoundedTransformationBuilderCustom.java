package com.muzzley.util.picasso;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.ImageView;

import com.makeramen.roundedimageview.Corner;
import com.makeramen.roundedimageview.RoundedDrawable;
import com.muzzley.R;
import com.squareup.picasso.Transformation;
import java.util.Arrays;

public class RoundedTransformationBuilderCustom {
    //private final Resources mResources;
    private final DisplayMetrics mDisplayMetrics;
    private Context context;

    private float[] mCornerRadii = new float[] { 0, 0, 0, 0 };

    private boolean mOval = false;
    private float mBorderWidth = 0;
    private ColorStateList mBorderColor =
            ColorStateList.valueOf(RoundedDrawable.DEFAULT_BORDER_COLOR);
    private ImageView.ScaleType mScaleType = ImageView.ScaleType.FIT_CENTER;

    public RoundedTransformationBuilderCustom(Context context) {
        this.context = context;
        mDisplayMetrics = Resources.getSystem().getDisplayMetrics();
    }

    public RoundedTransformationBuilderCustom scaleType(ImageView.ScaleType scaleType) {
        mScaleType = scaleType;
        return this;
    }

    /**
     * Set corner radius for all corners in px.
     *
     * @param radius the radius in px
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom cornerRadius(float radius) {
        mCornerRadii[Corner.TOP_LEFT] = radius;
        mCornerRadii[Corner.TOP_RIGHT] = radius;
        mCornerRadii[Corner.BOTTOM_RIGHT] = radius;
        mCornerRadii[Corner.BOTTOM_LEFT] = radius;
        return this;
    }

    /**
     * Set corner radius for a specific corner in px.
     *
     * @param corner the corner to set.
     * @param radius the radius in px.
     * @return the builder for chaning.
     */
    public RoundedTransformationBuilderCustom cornerRadius(int corner, float radius) {
        mCornerRadii[corner] = radius;
        return this;
    }

    /**
     * Set corner radius for all corners in density independent pixels.
     *
     * @param radius the radius in density independent pixels.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom cornerRadiusDp(float radius) {
        return cornerRadius(
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, mDisplayMetrics));
    }

    /**
     * Set corner radius for a specific corner in density independent pixels.
     *
     * @param corner the corner to set
     * @param radius the radius in density independent pixels.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom cornerRadiusDp(int corner, float radius) {
        return cornerRadius(corner,
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, radius, mDisplayMetrics));
    }

    /**
     * Set the border width in pixels.
     *
     * @param width border width in pixels.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom borderWidth(float width) {
        mBorderWidth = width;
        return this;
    }

    /**
     * Set the border width in density independent pixels.
     *
     * @param width border width in density independent pixels.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom borderWidthDp(float width) {
        mBorderWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, width, mDisplayMetrics);
        return this;
    }

    /**
     * Set the border color.
     *
     * @param color the color to set.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom borderColor(int color) {
        mBorderColor = ColorStateList.valueOf(color);
        return this;
    }

    /**
     * Set the border color as a {@link ColorStateList}.
     *
     * @param colors the {@link ColorStateList} to set.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom borderColor(ColorStateList colors) {
        mBorderColor = colors;
        return this;
    }

    /**
     * Sets whether the image should be oval or not.
     *
     * @param oval if the image should be oval.
     * @return the builder for chaining.
     */
    public RoundedTransformationBuilderCustom oval(boolean oval) {
        mOval = oval;
        return this;
    }

    /**
     * Creates a {@link Transformation} for use with picasso.
     *
     * @return the {@link Transformation}
     */
    public Transformation build() {
        return new Transformation() {
            @Override public Bitmap transform(Bitmap source) {
                try {
                    Bitmap imageWithBG = Bitmap.createBitmap(source.getWidth(), source.getHeight(),source.getConfig());  // Create another image the same size
                    imageWithBG.eraseColor(Color.WHITE);  // set its background to white, or whatever color you want
                    Canvas canvas = new Canvas(imageWithBG);  // create a canvas to draw on the new image
                    canvas.drawBitmap(source, 0f, 0f, null); // draw old image on the background
                    source.recycle();  // clear out old image

                    Bitmap transformed = RoundedDrawable.fromBitmap(imageWithBG)
                            .setScaleType(mScaleType)
                            .setCornerRadius(mCornerRadii[0], mCornerRadii[1], mCornerRadii[2], mCornerRadii[3])
                            .setBorderWidth(mBorderWidth)
                            .setBorderColor(mBorderColor)
                            .setOval(mOval)
                            .toBitmap();
                    if (!imageWithBG.equals(transformed)) {
                        imageWithBG.recycle();
                    }
                    return transformed;
                } catch(Exception ex)
                {
                    return BitmapFactory.decodeResource(context.getResources(), R.drawable.device_placeholder);
                }
            }

            @Override public String key() {
                return "r:" + Arrays.toString(mCornerRadii)
                        + "b:" + mBorderWidth
                        + "c:" + mBorderColor
                        + "o:" + mOval;
            }
        };
    }
}
