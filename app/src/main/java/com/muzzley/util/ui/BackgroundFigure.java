package com.muzzley.util.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.muzzley.util.ScreenInspector;

import timber.log.Timber;

/**
 * Created by ruigoncalo on 31/08/15.
 */
public class BackgroundFigure extends View {

    private Context context;
    private Point anchor;
    private Point a;
    private Point b;
    private Point c;
    private Point d;
    private Path path;
    private Paint paint;

    public BackgroundFigure(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(2);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);

        int width = ScreenInspector.getScreenWidth(context);
        int height = ScreenInspector.getScreenHeight(context);
        int navigationBarHeightPx = ScreenInspector.getNavigationBarHeightPx(context);
        int statusBarHeightPx = ScreenInspector.getStatusBarHeightPx(context);
        int appBarHeightPx = ScreenInspector.getAppBarHeightPx(context);

//        Timber.d("Height = " + height + " Width = " + width);
//        Timber.d("Status bar = " + statusBarHeightPx + " | App bar = " + appBarHeightPx + " | Navigation bar = "+ navigationBarHeightPx);

        int topOffset = height - statusBarHeightPx - (appBarHeightPx * 2) - navigationBarHeightPx;
//        Timber.d("Top offset =" + topOffset);

        a = new Point(0, topOffset/2);
//        Timber.d("a: " + a.toString());

        b = new Point(width, topOffset/2);
//        Timber.d("b: " + b.toString());

        c = new Point(width, height);
//        Timber.d("c: " + c.toString());

        d = new Point(0, height);
//        Timber.d("d: " + d.toString());

        anchor = new Point(width/2, topOffset/2 - width/8);
        path = new Path();
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        path.moveTo(a.x, a.y);
        path.quadTo(anchor.x, anchor.y, b.x, b.y);
        path.lineTo(c.x, c.y);
        path.lineTo(d.x, d.y);
        path.lineTo(a.x, a.y);
        path.close();

        canvas.drawPath(path, paint);
    }
}
