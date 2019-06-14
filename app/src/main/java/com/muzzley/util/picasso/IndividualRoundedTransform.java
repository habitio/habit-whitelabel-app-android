package com.muzzley.util.picasso;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;

import com.squareup.picasso.Transformation;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 30/06/14.
 */
public class IndividualRoundedTransform implements Transformation {

    private final boolean topLeft;
    private final boolean topRight;
    private final boolean bottomLeft;
    private final boolean bottomRight;
    private final int radius;
    private final int margin;  // dp

    // radius is corner radii in dp
    // margin is the board in dp
    public IndividualRoundedTransform(final int margin, final int radius, boolean topLeft, boolean topRight, boolean bottomLeft, boolean bottomRight) {
        this.radius = radius;
        this.margin = margin;
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    @Override
    public Bitmap transform(final Bitmap source) {
        final Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setShader(new BitmapShader(source, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Rect rect = new Rect(margin, margin, source.getWidth() - margin, source.getHeight() - margin);
        RectF rectF = new RectF(rect);
        canvas.drawRoundRect(rectF, radius, radius, paint);

        if(!topLeft){
            Timber.d("topLeft");
            Rect rectTL = new Rect(margin, margin, (source.getWidth() - margin)/2, (source.getHeight() - margin)/2);
            canvas.drawRect(rectTL, paint);
        }

        if(!topRight){
            Timber.d("topRight");
            Rect rectTR = new Rect((source.getWidth() - margin)/2, margin, source.getWidth() - margin, (source.getHeight() - margin)/2);
            canvas.drawRect(rectTR, paint);
        }

        if(!bottomRight){
            Timber.d("bottomRight");
            Rect rectBR = new Rect((source.getWidth() - margin)/2, (source.getHeight() - margin)/2, source.getWidth() - margin, (source.getHeight() - margin)/2);
            canvas.drawRect(rectBR, paint);
        }

        if(!bottomLeft){
            Timber.d("bottomLeft");
            Rect rectBL = new Rect(margin, (source.getHeight() - margin)/2, (source.getWidth() - margin)/2, source.getHeight() - margin);
            canvas.drawRect(rectBL, paint);
        }

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(output, rect, rect, paint);

        if (source != output) {
            source.recycle();
        }

        return output;
    }

    @Override
    public String key() {
        return "rounded";
    }
}
