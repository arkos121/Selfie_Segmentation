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

    private val matrix = Matrix()
    private val savedMatrix = Matrix()

    private var currentScale = 1f
    private val minScale = 0.5f
    private val maxScale = 4f

    private val startPoint = PointF()
    private val midPoint = PointF()

    private var touchMode = TOUCH_NONE

    private val scaleDetector: ScaleGestureDetector
    private val gestureDetector: GestureDetector

    init {
        scaleType = ScaleType.MATRIX

        scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                val scaleFactor = detector.scaleFactor
                val newScale = currentScale * scaleFactor

                if (newScale in minScale..maxScale) {
                    currentScale = newScale
                    matrix.postScale(
                        scaleFactor,
                        scaleFactor,
                        detector.focusX,
                        detector.focusY
                    )
                    imageMatrix = matrix
                    invalidate()
                }
                return true
            }
        })

        gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                // Reset to default zoom
                matrix.reset()
                currentScale = 1f
                imageMatrix = matrix
                invalidate()
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                savedMatrix.set(matrix)
                startPoint.set(event.x, event.y)
                touchMode = TOUCH_DRAG
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    savedMatrix.set(matrix)
                    midPoint.set(
                        (event.getX(0) + event.getX(1)) / 2,
                        (event.getY(0) + event.getY(1)) / 2
                    )
                    touchMode = TOUCH_ZOOM
                }
            }

            MotionEvent.ACTION_MOVE -> {
                when (touchMode) {
                    TOUCH_DRAG -> {
                        matrix.set(savedMatrix)
                        matrix.postTranslate(
                            event.x - startPoint.x,
                            event.y - startPoint.y
                        )
                        imageMatrix = matrix
                        invalidate()
                    }
                }
            }

            MotionEvent.ACTION_UP,
            MotionEvent.ACTION_POINTER_UP -> {
                touchMode = TOUCH_NONE
            }
        }

        return true
    }

    override fun onDraw(canvas: Canvas) {
        canvas.save()
        canvas.concat(matrix)
        super.onDraw(canvas)
        canvas.restore()
    }

    companion object {
        private const val TOUCH_NONE = 0
        private const val TOUCH_DRAG = 1
        private const val TOUCH_ZOOM = 2
    }
}