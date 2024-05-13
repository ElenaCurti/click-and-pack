// Code to draw rect found here
// https://medium.com/@mrizqi070502/building-a-face-detection-app-with-mlkit-a-step-by-step-guide-b729429119ec

package object_detector

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
class ObjectBoundingBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private var boundingBoxes: List<RectF>? = null
    private var texts: List<String>? = null


    private val paintDetected = Paint().apply {
        color = Color.GREEN
        style = Paint.Style.STROKE
        textSize = 80f
        strokeWidth = 10f
    }

    private val paintNotDetected = Paint().apply {
        color = Color.RED
        style = paintDetected.style
        strokeWidth = paintDetected.strokeWidth
    }

    fun setNoneFound(){
        boundingBoxes = null
        texts = null
        invalidate()
    }

    fun setMultipleBoundingBoxes(rects: List<Rect>, imagesWidth: Int, imagesHeight: Int, texts: List<String>) {
        boundingBoxes = rects.map { rect ->
            val viewWidth = width
            val viewHeight = height
            val scaleFactorX = viewWidth.toFloat() / imagesHeight.toFloat()
            val scaleFactorY = viewHeight.toFloat() / imagesWidth.toFloat()
            RectF(
                rect.left * scaleFactorX,
                rect.top * scaleFactorY,
                rect.right * scaleFactorX,
                rect.bottom * scaleFactorY
            )
        }
        this.texts = texts.toList()
        invalidate()
    }




    fun setBoundingBox(rect: Rect, imageWidth: Int, imageHeight: Int, text: String) {
        /*val viewWidth = width
        val viewHeight = height
        val scaleFactorX = viewWidth.toFloat() / imageHeight.toFloat()
        val scaleFactorY = viewHeight.toFloat() / imageWidth.toFloat()
        boundingBox = RectF(
            rect.left * scaleFactorX,
            rect.top * scaleFactorY,
            rect.right * scaleFactorX,
            rect.bottom * scaleFactorY
        )
        invalidate()

        textLabel = text

        if (textLabel == "")
            paint.color = Color.RED
        else
            paint.color = Color.GREEN*/

    }
    override fun onDraw(canvas: Canvas) {
        boundingBoxes?.forEachIndexed { index, box ->

            var paint : Paint = paintNotDetected

            // Draw text
            texts?.getOrNull(index)?.let { text ->
                if (text == "")
                    paint = paintNotDetected
                else
                    paint = paintDetected
                val textX = box.centerX() - paint.measureText(text) / 2
                val textY = box.centerY() + paint.textSize / 2
                canvas.drawText(text, textX, textY, paint)
            }

            // Draw rectangle
            canvas.drawRect(box, paint)


        }

        /*
        super.onDraw(canvas)
        boundingBox?.let {
            canvas?.drawRect(it, paint)

            // Draw text

            val textX = it.centerX() - paint.measureText(textLabel) / 2
            val textY = it.centerY() + paint.textSize / 2
            canvas.drawText(textLabel, textX, textY, paint)

        }*/
    }
}