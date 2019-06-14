package com.muzzley.app.tiles

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

import com.squareup.picasso.Transformation

class Grayshade : Transformation {
    override fun transform(source: Bitmap): Bitmap {
        try {
            val paint = Paint()
            val output = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)

            // Create canvas and draw the source image
            val canvas = Canvas(output)
            canvas.drawBitmap(source, 0f, 0f, paint)

            // Setup the paint for painting the shade
            paint.color = Color.parseColor("#63C4C4C4")

            // Paint the shade
            canvas.drawPaint(paint)

            // Recycle and return
            source.recycle()
            return output
        } catch (e: Exception) {
            return source
        }

    }

    override fun key(): String {
        return "Gray"
    }

}
