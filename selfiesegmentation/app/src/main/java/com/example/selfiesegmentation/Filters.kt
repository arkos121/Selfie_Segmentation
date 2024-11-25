package com.example.selfiesegmentation

import android.graphics.*
import kotlin.math.min
import kotlin.math.max

object Filters {
    fun applyGrayscale(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
                setSaturation(0f)
            })
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    fun applySepia(original: Bitmap): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val colorMatrix = ColorMatrix(floatArrayOf(
            1.0f, 0.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.6f, 0.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f, 0.0f
        ))

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    fun applyBrightness(original: Bitmap, value: Float): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    fun applyContrast(original: Bitmap, contrast: Float): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val scale = contrast + 1f
        val translation = (-0.5f * scale + 0.5f) * 255f

        val colorMatrix = ColorMatrix().apply {
            set(floatArrayOf(
                scale, 0f, 0f, 0f, translation,
                0f, scale, 0f, 0f, translation,
                0f, 0f, scale, 0f, translation,
                0f, 0f, 0f, 1f, 0f
            ))
        }

        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(colorMatrix)
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    fun applyBlur(original: Bitmap, radius: Float): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint().apply {
            maskFilter = BlurMaskFilter(radius, BlurMaskFilter.Blur.NORMAL)
        }

        canvas.drawBitmap(original, 0f, 0f, paint)
        return result
    }

    fun applyVignette(original: Bitmap, intensity: Float = 1f): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        val paint = Paint()
        val radialGradient = RadialGradient(
            width / 2f,
            height / 2f,
            min(width, height) * 0.7f,
            intArrayOf(Color.TRANSPARENT, Color.BLACK),
            floatArrayOf(0.7f, 1f),
            Shader.TileMode.CLAMP
        )

        // Draw original bitmap
        canvas.drawBitmap(original, 0f, 0f, paint)

        // Apply vignette effect
        paint.shader = radialGradient
        paint.alpha = (255 * intensity).toInt()
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.DARKEN)
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        return result
    }

    fun applyTint(original: Bitmap, color: Int, intensity: Float): Bitmap {
        val width = original.width
        val height = original.height
        val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(result)

        // Draw original bitmap
        canvas.drawBitmap(original, 0f, 0f, null)

        // Apply tint
        val paint = Paint().apply {
            alpha = (255 * intensity).toInt()
            xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_ATOP)
        }
        canvas.drawColor(color, PorterDuff.Mode.SRC_ATOP)

        return result
    }

    fun stackFilters(original: Bitmap, vararg filters: (Bitmap) -> Bitmap): Bitmap {
        return filters.fold(original) { bitmap, filter -> filter(bitmap) }
    }
}