package com.stedi.multitouchpaint.painters

import android.graphics.*
import android.view.MotionEvent
import com.stedi.multitouchpaint.App
import com.stedi.multitouchpaint.R
import com.stedi.multitouchpaint.data.Brush
import com.stedi.multitouchpaint.data.Pointer
import com.stedi.multitouchpaint.view.CanvasView

class PipettePainter(private val bitmap: Bitmap) : Painter() {
    private val headRadius = App.dp2px(25f)
    private val needleLength = App.dp2px(50f)
    private val needleEnlargement = App.dp2px(8f)
    private val innerRadius = App.dp2px(20f)
    private val innerStrokeWidth = App.dp2px(0.5f)
    private val shadowWidth = App.dp2px(2f)

    private val fillColor = Color.WHITE
    private val shadowColor = App.getContext().resources.getColor(R.color.material_shadow)
    private val innerStrokeColor = App.getContext().resources.getColor(R.color.medium_gray)

    private val currentPointers = HashSet<Int>() // for isDrawing()
    private val pointer = Pointer(bitmap.width / 2f, bitmap.height / 2f)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val needlePath = Path()

    private var color = Color.BLACK

    init {
        paint.style = Paint.Style.FILL_AND_STROKE
        needlePath.fillType = Path.FillType.EVEN_ODD
        pickColor()
    }

    fun getColor() = color

    override fun onPointerDown(event: MotionEvent, brush: Brush) {
        currentPointers.add(event.getPointerId(event.actionIndex))
        onMove(event)
    }

    override fun onPointerMove(event: MotionEvent, brush: Brush) {
        onMove(event)
    }

    override fun onPointerUp(event: MotionEvent, brush: Brush) {
        currentPointers.remove(event.getPointerId(event.actionIndex))
    }

    override fun isDrawing() = !currentPointers.isEmpty()

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, 0f, 0f, null)

        val xCorner = if (pointer.x + needleLength + headRadius > canvas.width) -1 else 1
        val yCorner = if (pointer.y - needleLength - headRadius < 0) -1 else 1

        for (step in 1..2) {
            paint.color = if (step == 1) shadowColor else fillColor
            val fakeOuterStroke = if (step == 1) shadowWidth else 0f

            //head
            canvas.drawCircle(pointer.x + needleLength * xCorner, pointer.y - needleLength * yCorner,
                    headRadius + fakeOuterStroke / 1.5f, paint)

            // needle
            needlePath.moveTo(pointer.x, pointer.y)
            needlePath.lineTo(pointer.x + (needleLength - needleEnlargement - fakeOuterStroke) * xCorner,
                    pointer.y + (-needleLength - needleEnlargement - fakeOuterStroke) * yCorner)
            needlePath.lineTo(pointer.x + (needleLength + needleEnlargement + fakeOuterStroke) * xCorner,
                    pointer.y + (-needleLength + needleEnlargement + fakeOuterStroke) * yCorner)
            needlePath.close()
            canvas.drawPath(needlePath, paint)
            needlePath.reset()

            // inner circle with color
            if (step == 2) {
                for (innerStep in 1..2) {
                    paint.color = if (innerStep == 1) innerStrokeColor else color
                    val fakeInnerStroke = if (innerStep == 1) innerStrokeWidth else 0f

                    canvas.drawCircle(pointer.x + needleLength * xCorner, pointer.y - needleLength * yCorner,
                            innerRadius + fakeInnerStroke, paint)
                }
            }
        }
    }

    override fun onDetach(canvasView: CanvasView) {
        super.onDetach(canvasView)
        bitmap.recycle()
    }

    private fun onMove(event: MotionEvent) {
        pointer.x = event.getX(event.actionIndex)
        pointer.y = event.getY(event.actionIndex)
        pickColor()
        requestInvalidate()
    }

    private fun pickColor() {
        if (pointer.x in 0 until bitmap.width && pointer.y in 0 until bitmap.height) {
            color = bitmap.getPixel(pointer.x.toInt(), pointer.y.toInt())
        }
    }
}