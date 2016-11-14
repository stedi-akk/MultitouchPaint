package com.stedi.multitouchpaint.painters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.MotionEvent;

import com.stedi.multitouchpaint.data.Brush;
import com.stedi.multitouchpaint.view.CanvasView;

public abstract class Painter {
    public void onAttach(CanvasView canvasView) {
    }

    public void onDetach(CanvasView canvasView) {
    }

    public void onPointerDown(MotionEvent event, Brush brush, CanvasView canvasView) {
    }

    public void onPointerMove(MotionEvent event, Brush brush, CanvasView canvasView) {
    }

    public void onPointerUp(MotionEvent event, Brush brush, CanvasView canvasView) {
    }

    public void onDraw(Canvas canvas) {
    }

    public void onSetPicture(Bitmap bitmap, int canvasWidth, int canvasHeight) {
    }

    public boolean onUndo() {
        return false;
    }

    public boolean onClear() {
        return false;
    }

    public boolean isDrawing() {
        return false;
    }
}
