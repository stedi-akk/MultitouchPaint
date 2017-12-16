package com.stedi.multitouchpaint.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.stedi.multitouchpaint.data.Brush
import com.stedi.multitouchpaint.painters.Painter

class CanvasView : View {
    private lateinit var painter: Painter
    private lateinit var brush: Brush

    constructor(context: Context?) : this(context, null)
    constructor(context: Context?, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        setBackgroundColor(Color.WHITE)
    }

    fun setPainter(painter: Painter) {
        if (this::painter.isInitialized) {
            this.painter.onDetach(this)
        }
        this.painter = painter
        this.painter.onAttach(this)
        invalidate()
    }

    fun getPainter() = painter

    fun setBrush(brush: Brush) {
        this.brush = brush
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                painter.onPointerDown(event, brush, this)
                true
            }
            MotionEvent.ACTION_MOVE -> {
                painter.onPointerMove(event, brush, this)
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                painter.onPointerUp(event, brush, this)
                true
            }
            else -> false
        }
    }

    override fun onDraw(canvas: Canvas) {
        painter.onDraw(canvas)
    }

    fun undo() {
        if (painter.onUndo()) {
            invalidate()
        }
    }

    fun clearPicture() {
        if (painter.onClear()) {
            invalidate()
        }
    }

    fun isDrawing() = painter.isDrawing

    fun generatePicture(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        draw(Canvas(bitmap))
        return bitmap
    }

    fun setPicture(bitmap: Bitmap) {
        clearPicture()
        painter.onSetPicture(bitmap, width, height)
        invalidate()
    }
}