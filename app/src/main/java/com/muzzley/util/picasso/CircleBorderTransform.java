package com.muzzley.util.picasso;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

import com.muzzley.R;
import com.squareup.picasso.Transformation;

/**
 * Created by ruigoncalo on 19/05/14.
 */
public class CircleBorderTransform implements Transformation {

    private Context context;
    private int strokeWidth=-1, strokeWidthDeault = 3;

    public CircleBorderTransform(Context context) {
        this.context = context;
    }

    public CircleBorderTransform(Context context, int strokeWidth) {
        this.context = context;
        this.strokeWidth = strokeWidth;
    }

    @Override
    public Bitmap transform(Bitmap source) {
        try{
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
            paintStroke.setColor(context.getResources().getColor(R.color.device_picker_circular_border));
            paintStroke.setStyle(Paint.Style.STROKE);
            paintStroke.setAntiAlias(true);
            if(strokeWidth<0) {
                paintStroke.setStrokeWidth(strokeWidthDeault);
            } else {
                paintStroke.setStrokeWidth(strokeWidth);
            }
            canvas.drawCircle(r, r, r-2, paintStroke);

            squaredBitmap.recycle();
            return bitmap;
        } catch(Exception ex)
        {
            return BitmapFactory.decodeResource(context.getResources(), R.drawable.device_placeholder);
        }
    }

    @Override
    public String key() {
        return "circle";
    }
}