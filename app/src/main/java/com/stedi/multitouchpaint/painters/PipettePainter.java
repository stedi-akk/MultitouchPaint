package com.stedi.multitouchpaint.painters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.MotionEvent;

import com.stedi.multitouchpaint.App;
import com.stedi.multitouchpaint.R;
import com.stedi.multitouchpaint.data.Brush;
import com.stedi.multitouchpaint.data.Pointer;
import com.stedi.multitouchpaint.view.CanvasView;

import java.util.HashSet;

public class PipettePainter extends Painter {
    private final float headRadius = App.dp2px(25);
    private final float needleLength = App.dp2px(50);
    private final float needleEnlargement = App.dp2px(8);
    private final float innerRadius = App.dp2px(20);
    private final float innerStrokeWidth = App.dp2px(0.5f);
    private final float shadowWidth = App.dp2px(2);

    private final int fillColor = Color.WHITE;
    private final int shadowColor = App.getContext().getResources().getColor(R.color.material_shadow);
    private final int innerStrokeColor = App.getContext().getResources().getColor(R.color.medium_gray);

    private HashSet<Integer> currentPointers = new HashSet<>();

    private Bitmap bitmap;
    private Pointer pointer;

    private Paint paint;
    private Path needlePath;

    private int color = Color.BLACK;

    public PipettePainter(Bitmap bitmap) {
        this.pointer = new Pointer(bitmap.getWidth() / 2, bitmap.getHeight() / 2);
        this.bitmap = bitmap;
        tryColor();

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        needlePath = new Path();
        needlePath.setFillType(Path.FillType.EVEN_ODD);
    }

    public int getColor() {
        return color;
    }

    @Override
    public void onPointerDown(MotionEvent event, Brush brush, CanvasView canvasView) {
        currentPointers.add(event.getPointerId(event.getActionIndex()));
        onMove(event, canvasView);
    }

    @Override
    public void onPointerMove(MotionEvent event, Brush brush, CanvasView canvasView) {
        onMove(event, canvasView);
    }

    @Override
    public void onPointerUp(MotionEvent event, Brush brush, CanvasView canvasView) {
        currentPointers.remove(event.getPointerId(event.getActionIndex()));
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.drawBitmap(bitmap, 0, 0, null);

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

    @Override
    public void onDetach(CanvasView canvasView) {
        bitmap.recycle();
    }

    @Override
    public boolean isDrawing() {
        return !currentPointers.isEmpty();
    }

    private void onMove(MotionEvent event, CanvasView canvasView) {
        float x = event.getX(event.getActionIndex());
        float y = event.getY(event.getActionIndex());
        pointer.set(x, y);
        tryColor();
        canvasView.invalidate();
    }

    private void tryColor() {
        int x = (int) pointer.getX();
        int y = (int) pointer.getY();

        if (x >= 0 && x < bitmap.getWidth() && y >= 0 && y < bitmap.getHeight()) {
            color = bitmap.getPixel(x, y);
        }
    }
}
