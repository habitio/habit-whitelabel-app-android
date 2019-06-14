package com.muzzley.util.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.muzzley.R;
import com.squareup.picasso.Transformation;

/**
 * Created by ruigoncalo on 19/05/14.
 */
public class CircleProfileEditBorderTransform implements Transformation {

    private Context context;
    private int strokeWidth=-1, strokeWidthDefault = 15;
    private boolean enabled;

    public CircleProfileEditBorderTransform(Context context) {
        this.context = context;
    }

    public CircleProfileEditBorderTransform(Context context, int strokeWidth, boolean enabled) {
        this.context = context;
        this.strokeWidth = strokeWidth;
        this.enabled = enabled;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Bitmap bitmap = Bitmap.createBitmap(size, size, source.getConfig());
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();

        BitmapShader shader = new BitmapShader(squaredBitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        float r = size/2f;
        canvas.drawCircle(r, r, r, paint);

        Paint paintStroke = new Paint();
        if (enabled) {
            paintStroke.setColor(context.getResources().getColor(R.color.blue));
        } else {
            paintStroke.setColor(context.getResources().getColor(R.color.white));
        }
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setAntiAlias(true);
        paint.setShader(shader);
        if(strokeWidth<0) {
            paintStroke.setStrokeWidth(strokeWidthDefault);
        } else {
            paintStroke.setStrokeWidth(strokeWidth);
        }
        canvas.drawCircle(r, r, r - 1, paintStroke);


        squaredBitmap.recycle();
        return bitmap;
    }

    @Override
    public String key() {
        return "circle";
    }
}