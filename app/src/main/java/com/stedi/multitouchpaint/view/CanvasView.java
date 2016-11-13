package com.stedi.multitouchpaint.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.stedi.multitouchpaint.data.Brush;

public class CanvasView extends View {
    private Brush brush;

    public interface Painter {
        void onPointerDown(MotionEvent event, Brush brush, CanvasView canvasView);

        void onPointerMove(MotionEvent event, Brush brush, CanvasView canvasView);

        void onPointerUp(MotionEvent event, Brush brush, CanvasView canvasView);

        void onDraw(Canvas viewCanvas);

        boolean onUndo();

        boolean onClear();

        void onSetPicture(Bitmap bitmap, int canvasWidth, int canvasHeight);

        boolean isDrawing();
    }

    public CanvasView(Context context) {
        this(context, null);
    }

    public CanvasView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CanvasView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setBackgroundColor(Color.WHITE);
    }

    public void setPainter(Painter painter) {
        this.painter = painter;
        invalidate();
    }

    public Painter getPainter() {
        return painter;
    }

    private Painter painter;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                painter.onPointerDown(event, brush, this);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                painter.onPointerMove(event, brush, this);
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                painter.onPointerUp(event, brush, this);
                break;
            }

            default:
                return false;
        }
        return true;
    }

    @Override
    protected void onDraw(Canvas viewCanvas) {
        painter.onDraw(viewCanvas);
    }

    public void setBrush(Brush brush) {
        this.brush = brush;
    }

    public void undo() {
        if (painter.onUndo())
            invalidate();
    }

    public Bitmap generatePicture() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    public void clearPicture() {
        if (painter.onClear())
            invalidate();
    }

    public void setPicture(Bitmap bitmap) {
        clearPicture();

        painter.onSetPicture(bitmap, getWidth(), getHeight());

        invalidate();
    }

    public boolean isDrawing() {
        return painter.isDrawing();
    }
}
