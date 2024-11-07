package com.example.selfiesegmentation


import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.PointF
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView

class ZoomableImageView(context: Context, attrs: AttributeSet?) : AppCompatImageView(context, attrs) {

    private var matrix = Matrix()
    private var scale = 0.5f
    private var minScale = 0.5f
    private var maxScale = 2f

    private val scaleGestureDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    private val lastPoint = PointF()
    private val currentPoint = PointF()
    private var isDragging = false

    init {
        scaleType = ScaleType.MATRIX
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleGestureDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        val pointerCount = event.pointerCount
        if (pointerCount == 1) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    lastPoint.set(event.x, event.y)
                    isDragging = true
                }
                MotionEvent.ACTION_MOVE -> if (isDragging) {
                    currentPoint.set(event.x, event.y)
                    val dx = currentPoint.x - lastPoint.x
                    val dy = currentPoint.y - lastPoint.y
                    matrix.postTranslate(dx, dy)
                    lastPoint.set(currentPoint.x, currentPoint.y)
                    invalidate()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> isDragging = false
            }
        }
        imageMatrix = matrix
        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.onDraw(canvas)
        canvas.restore()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scaleFactor = detector.scaleFactor
            val newScale = scale * scaleFactor
            if (newScale in minScale..maxScale) {
                scale = newScale
                matrix.postScale(scaleFactor, scaleFactor, detector.focusX, detector.focusY)
                invalidate()
            }
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // Reset zoom on double-tap
            scale = 1f
            matrix.reset()
            invalidate()
            return true
        }
    }
}
