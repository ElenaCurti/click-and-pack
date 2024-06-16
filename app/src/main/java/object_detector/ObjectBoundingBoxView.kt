// Code to draw rect found here
// https://medium.com/@mrizqi070502/building-a-face-detection-app-with-mlkit-a-step-by-step-guide-b729429119ec

package object_detector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import com.google.mlkit.vision.objects.DetectedObject
import java.util.concurrent.ConcurrentSkipListSet


@SuppressLint("ClickableViewAccessibility") // otherwise waring in init
class ObjectBoundingBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs), OnTouchListener{
    // Displayed boxes
    private lateinit var objDetected : DetectedObject
    private var box : RectF = RectF()
    private var scaleFactorX : Float = -1f
    private var scaleFactorY : Float = -1f

    // Tracking ids of the clicked items (the one how will be setted as "packed")
    private var clickedTrackedIds = ConcurrentSkipListSet<Int>()

    // Indexes of the clicked items
    private var clickedItemsIndexes = ConcurrentSkipListSet<Int>()

    // Paints of the text label and their background
    private val textPaint = Paint().apply {
        textSize = 75f
        strokeWidth = 7f
    }

    val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = textPaint.strokeWidth
        textSize = textPaint.textSize
    }


    init {
        setOnTouchListener(this)
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        if (event == null)
            return true
//        Log.d("click", "click")
        if (event.action == MotionEvent.ACTION_DOWN ) {
            // If clicked object was correctly detected, it will be set as "packed"
            if (objDetected.labels.size > 0 && box.contains(event.x, event.y)) {
                objDetected.labels.forEach{label ->
                    clickedItemsIndexes.add(label.index)
                }
                clickedTrackedIds.add(objDetected.trackingId)
            }
        }

        return false
    }

    fun getClickedItemsIndexes() : ConcurrentSkipListSet<Int>{
        return clickedItemsIndexes
    }


    fun setNoObjectFound(){
        box.left = 0f
        box.top = 0f
        box.right = 0f
        box.bottom = 0f

        invalidate()
    }

    fun setDetectedObject(detectedObject: DetectedObject, imagesWidth: Int, imagesHeight: Int) {
        // Save detected object
        objDetected = detectedObject

        // Set bounding box's borders
        scaleFactorX = width.toFloat() / imagesHeight.toFloat()
        scaleFactorY = height.toFloat() / imagesWidth.toFloat()
        box.left = detectedObject.boundingBox.left * scaleFactorX
        box.top = detectedObject.boundingBox.top * scaleFactorY
        box.right = detectedObject.boundingBox.right * scaleFactorX
        box.bottom = detectedObject.boundingBox.bottom * scaleFactorY

        // Set bb color
        if (clickedTrackedIds.contains(detectedObject.trackingId)) {
            // Packed item
            textPaint.color = Color.GREEN
            backgroundPaint.color = Color.BLACK
        } else if (detectedObject.labels.size == 0) {
            // Unrecognised item
            textPaint.color = Color.RED
            backgroundPaint.color = Color.WHITE
        } else {
            // Recognised, but not packed item
            textPaint.color = Color.BLUE
            backgroundPaint.color = Color.WHITE
        }

        invalidate()
    }

    private fun getTextBackgroundSize(x: Float, y: Float, text: String): Rect {
        // Readapted from
        // https://stackoverflow.com/questions/8242439/how-to-draw-text-with-background-color-using-canvas
        val fontMetrics = textPaint.fontMetrics
        val halfTextLength = textPaint.measureText(text) / 2 + 5
        return Rect(
            x.toInt(),
            (y + fontMetrics.top ).toInt(),
            (x + halfTextLength*2).toInt(),
            (y + fontMetrics.bottom ).toInt()
        )
    }

    override fun onDraw(canvas: Canvas) {
        if (!::objDetected.isInitialized || (box.left == 0f && box.top == 0f &&  box.right == 0f && box.bottom == 0f))
            return

        // Paint bb
        textPaint.style = Paint.Style.STROKE
        canvas.drawRect(box, textPaint)

        // Draw labels with background
        textPaint.style = Paint.Style.FILL

        val textX = box.centerX() - box.width() / 2
        val initialTextY = box.centerY() + textPaint.textSize / 2

        val labels = mutableListOf<String>()
        if (objDetected.labels.size == 0)
            labels.add("" + objDetected.trackingId + ". Unknown object")
        else {
            var numOrNull = "" + objDetected.trackingId + ". "
            objDetected.labels.forEach {label ->
                labels.add(numOrNull + label.text + "(" +String.format("%.2f", label.confidence)+ ") ")
                numOrNull = ""
            }
        }

        // First I draw the background items
        var y: Float = initialTextY
        labels.forEach { label ->
            val background = getTextBackgroundSize(textX, y, label)
            canvas.drawRect(background, backgroundPaint)
            y += textPaint.textSize
        }

        // Then the text items
        y = initialTextY
        labels.forEach { label ->
            canvas.drawText(label, textX, y, textPaint)
            y += textPaint.textSize
        }
    }


}