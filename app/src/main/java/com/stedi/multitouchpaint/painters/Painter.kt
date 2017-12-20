package com.stedi.multitouchpaint.painters

import android.graphics.Bitmap
import android.graphics.Canvas
import android.support.annotation.CallSuper
import android.view.MotionEvent
import com.stedi.multitouchpaint.data.Brush
import com.stedi.multitouchpaint.view.CanvasView

abstract class Painter {
    protected var canvasView: CanvasView? = null

    @CallSuper
    open fun onAttach(canvasView: CanvasView) {
        this.canvasView = canvasView
    }

    @CallSuper
    open fun onDetach(canvasView: CanvasView) {
        this.canvasView = null
    }

    open fun onPointerDown(event: MotionEvent, brush: Brush) {

    }

    open fun onPointerMove(event: MotionEvent, brush: Brush) {

    }

    open fun onPointerUp(event: MotionEvent, brush: Brush) {

    }

    open fun onDraw(canvas: Canvas) {

    }

    open fun onSetPicture(bitmap: Bitmap) {

    }

    open fun onUndo() = false

    open fun onClear() = false

    open fun isDrawing() = false

    protected fun requestInvalidate() {
        this.canvasView?.invalidate()
    }
}