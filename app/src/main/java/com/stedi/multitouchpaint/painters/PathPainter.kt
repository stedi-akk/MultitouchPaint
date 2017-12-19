package com.stedi.multitouchpaint.painters

import android.graphics.*
import android.util.SparseArray
import android.view.MotionEvent
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.data.Brush
import com.stedi.multitouchpaint.data.HistoryItem
import com.stedi.multitouchpaint.data.Pointer

object PathPainter : Painter() {
    private val historyItems = ArrayList<HistoryItem>()
    private val historyItemPerPointer = SparseArray<HistoryItem>()
    private val currentPointers = SparseArray<Pointer>()

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var cachedOldBitmap: Bitmap? = null
    private var oldBitmap: Bitmap? = null
    private var historyBitmap: Bitmap? = null

    private var cachedOldCanvas: Canvas? = null
    private var historyCanvas: Canvas? = null

    private var invalidateOnUndo = false
    private var cachedOldBitmapUpdated = false

    init {
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
    }

    override fun onPointerDown(event: MotionEvent, brush: Brush) {
        val pointerId = event.getPointerId(event.actionIndex)
        val x = event.getX(event.actionIndex)
        val y = event.getY(event.actionIndex)

        // new history and pointer item
        val item = HistoryItem(brush, Path(), HistoryItem.Status.ON_VIEW_CANVAS)
        historyItems.add(item)
        historyItemPerPointer.put(pointerId, item)
        currentPointers.put(pointerId, Pointer(x + 0.1f, y))

        // draw fake dot
        item.path.moveTo(x, y)
        item.path.lineTo(x + 0.1f, y)
        requestInvalidate()
    }

    override fun onPointerMove(event: MotionEvent, brush: Brush) {
        for (i in 0 until event.pointerCount) {
            val pointerId = event.getPointerId(i)
            val pointer = currentPointers.get(pointerId) ?: continue

            val x = event.getX(i)
            val y = event.getY(i)

            if (Math.abs(x - pointer.x) >= App.touchMoveAccuracy ||
                    Math.abs(y - pointer.y) >= App.touchMoveAccuracy) {
                val item = historyItemPerPointer.get(pointerId) ?: continue

                // draw smooth path
                item.path.quadTo(pointer.x, pointer.y,
                        (x + pointer.x) / 2, (y + pointer.y) / 2)
                requestInvalidate()

                pointer.x = x
                pointer.y = y
            }
        }
    }

    override fun onPointerUp(event: MotionEvent, brush: Brush) {
        val pointerId = event.getPointerId(event.actionIndex)

        val historyItem = historyItemPerPointer.get(pointerId)
        if (historyItem != null) {
            historyItem.status = HistoryItem.Status.ON_BITMAP_CANVAS
            historyItemPerPointer.remove(pointerId)
        }

        currentPointers.remove(pointerId)
        requestInvalidate()
    }

    // main drawing logic
    // '!!' is used here, because I dont want to write shitty code, just to make Kotlin compiler happy in "multithreading scenarios"
    // don't worry, this code was written in Java before, and never crashed from the moment of its release
    override fun onDraw(canvas: Canvas) {
        // update history bitmap and old bitmap (if needed) on undo
        if (invalidateOnUndo) {
            invalidateOnUndo = false

            if (cachedOldBitmap != null && cachedOldBitmapUpdated) {
                oldBitmap = cachedOldBitmap!!.copy(Bitmap.Config.ARGB_8888, false)
                cachedOldBitmapUpdated = false
            }

            historyBitmap!!.eraseColor(Color.TRANSPARENT)
            historyItems.removeAt(historyItems.size - 1)

            for (item in historyItems) {
                drawHistoryItem(item, historyCanvas!!)
            }
        }

        // draw bitmap containing deleted items (from cached bitmap)
        if (oldBitmap != null) {
            canvas.drawBitmap(oldBitmap, 0f, 0f, null)
        }

        // draw items which were previously current on history bitmap
        for (item in historyItems) {
            if (item.status == HistoryItem.Status.ON_BITMAP_CANVAS) {
                if (historyBitmap == null) {
                    historyBitmap = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
                    historyCanvas = Canvas(historyBitmap)
                }

                drawHistoryItem(item, historyCanvas!!)
                item.status = HistoryItem.Status.READY_TO_DELETE
            }
        }

        // draw bitmap containing history items (and more, if no undo was done)
        if (historyBitmap != null) {
            canvas.drawBitmap(historyBitmap, 0f, 0f, null)
        }

        // draw only current pointers on view canvas
        historyItems.filter { it.status == HistoryItem.Status.ON_VIEW_CANVAS }
                .forEach { drawHistoryItem(it, canvas) }

        // remove and cache in bitmap old history item
        // only one per onDraw()
        if (historyItems.size > App.maxTouchHistory) {
            val item = historyItems[0]
            if (item.status == HistoryItem.Status.READY_TO_DELETE) {
                if (cachedOldBitmap == null) {
                    cachedOldBitmap = Bitmap.createBitmap(canvas.width, canvas.height, Bitmap.Config.ARGB_8888)
                    cachedOldCanvas = Canvas(cachedOldBitmap)
                }

                drawHistoryItem(item, cachedOldCanvas!!)
                historyItems.remove(item)

                cachedOldBitmapUpdated = true
            }
        }
    }

    override fun onUndo(): Boolean {
        if (historyItems.size > 0) {
            invalidateOnUndo = true
            return true
        }
        return false
    }

    // '!!' same here...
    override fun onClear(): Boolean {
        if (cachedOldBitmap != null) {
            cachedOldBitmap!!.recycle()
            cachedOldBitmap = null
        }
        if (oldBitmap != null) {
            oldBitmap!!.recycle()
            oldBitmap = null
        }
        if (historyBitmap != null) {
            historyBitmap!!.recycle()
            historyBitmap = null
        }
        historyItems.clear()
        historyItemPerPointer.clear()
        currentPointers.clear()
        return true
    }

    // '!!' and here...
    override fun onSetPicture(bitmap: Bitmap) {
        val canvasView = canvasView
        if (canvasView != null) {
            cachedOldBitmap = Bitmap.createScaledBitmap(bitmap, canvasView.width, canvasView.height, true)
            if (!cachedOldBitmap!!.isMutable) {
                cachedOldBitmap = cachedOldBitmap!!.copy(Bitmap.Config.ARGB_8888, true)
            }
            cachedOldCanvas = Canvas(cachedOldBitmap)
            oldBitmap = cachedOldBitmap!!.copy(Bitmap.Config.ARGB_8888, false)
        }
    }

    override fun isDrawing() = currentPointers.size() > 0

    private fun drawHistoryItem(item: HistoryItem, canvas: Canvas) {
        paint.strokeWidth = item.brush.getThicknessPx()
        paint.color = item.brush.color
        canvas.drawPath(item.path, paint)
    }
}