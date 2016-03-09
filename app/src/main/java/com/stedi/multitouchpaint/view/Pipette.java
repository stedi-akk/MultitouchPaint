package com.stedi.multitouchpaint.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.Utils;
import com.stedi.multitouchpaint.history.Pointer;

/**
 * Part of {@link com.stedi.multitouchpaint.view.CanvasView}
 */
class Pipette {
    private final float headRadius = Utils.dp2px(25);
    private final float needleLength = Utils.dp2px(50);
    private final float needleEnlargement = Utils.dp2px(8);
    private final float innerRadius = Utils.dp2px(20);
    private final float innerStrokeWidth = Utils.dp2px(0.5f);
    private final float shadowWidth = Utils.dp2px(2);

    private final int fillColor = Color.WHITE;
    private final int shadowColor = App.getContext().getResources().getColor(R.color.material_shadow);
    private final int innerStrokeColor = App.getContext().getResources().getColor(R.color.medium_gray);

    private Bitmap bitmap;
    private Pointer pointer;

    private Paint paint;
    private Path needlePath;

    private int color = Color.BLACK;

    Pipette(float x, float y, Bitmap bitmap) {
        this.pointer = new Pointer(x, y);
        this.bitmap = bitmap;
        tryColor();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePath = new Path();
        needlePath.setFillType(Path.FillType.EVEN_ODD);
    }

    void move(float x, float y) {
        pointer.set(x, y);
        tryColor();
    }

    int getColor() {
        return color;
    }

    void draw(Canvas canvas) {
        float x = pointer.getX();
        float y = pointer.getY();
        int xCorner = x + needleLength + headRadius > canvas.getWidth() ? -1 : 1;
        int yCorner = y - needleLength - headRadius < 0 ? -1 : 1;

        for (int step = 1; step <= 2; step++) {
            paint.setColor(step == 1 ? shadowColor : fillColor);
            float fakeOuterStroke = step == 1 ? shadowWidth : 0;

            // head
            canvas.drawCircle(x + needleLength * xCorner, y - needleLength * yCorner,
                    headRadius + fakeOuterStroke / 1.5f, paint);

            // needle
            needlePath.moveTo(x, y);
            needlePath.lineTo(x + (needleLength - needleEnlargement - fakeOuterStroke) * xCorner,
                    y + (-needleLength - needleEnlargement - fakeOuterStroke) * yCorner);
            needlePath.lineTo(x + (needleLength + needleEnlargement + fakeOuterStroke) * xCorner,
                    y + (-needleLength + needleEnlargement + fakeOuterStroke) * yCorner);
            needlePath.close();
            canvas.drawPath(needlePath, paint);
            needlePath.reset();

            // inner circle with color
            if (step == 2) {
                for (int innerStep = 1; innerStep <= 2; innerStep++) {
                    paint.setColor(innerStep == 1 ? innerStrokeColor : color);
                    float fakeInnerStroke = innerStep == 1 ? innerStrokeWidth : 0;

                    canvas.drawCircle(x + needleLength * xCorner, y - needleLength * yCorner,
                            innerRadius + fakeInnerStroke, paint);
                }
            }
        }
    }

    private void tryColor() {
        int x = (int) pointer.getX();
        int y = (int) pointer.getY();

        if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
            color = bitmap.getPixel(x, y);
        }
    }
}
