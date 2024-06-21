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

/**
 * Class that shows bounding box and labels across the detected object
 * Code to draw rect was found here: https://medium.com/@mrizqi070502/building-a-face-detection-app-with-mlkit-a-step-by-step-guide-b729429119ec
 */
@SuppressLint("ClickableViewAccessibility") // otherwise warning in init
class ObjectBoundingBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs), OnTouchListener{
    /** Displayed boxes auxiliary attributes */
    private lateinit var objDetected : DetectedObject
    private var box : RectF = RectF()
    private var scaleFactorX : Float = -1f
    private var scaleFactorY : Float = -1f

    /**
     * Tracking ids of the clicked items (the one how will be setted as "packed").
     * It's thread-safe and will not contain uplicate
     */
    private var clickedTrackedIds = ConcurrentSkipListSet<Int>()

    /**  Indexes of the clicked items */
    private var clickedItemsIndexes = ConcurrentSkipListSet<Int>()

    /**  Paints of the text label and their background*/
    private val textPaint = Paint().apply {
        textSize = 75f
        strokeWidth = 7f
    }

    private val backgroundPaint = Paint().apply {
        style = Paint.Style.FILL
        strokeWidth = textPaint.strokeWidth
        textSize = textPaint.textSize
    }


    init {
        setOnTouchListener(this)
    }

    /**
     * When the user touches the display, this method checks if the click was performed on the
     * bounding box. In this case it will update the packed items' temporary list
     */
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

    /**
     * Method that returns the indexes of the clicked items
     */
    fun getClickedItemsIndexes() : ConcurrentSkipListSet<Int>{
        return clickedItemsIndexes
    }

    fun cleanDetectedItems() {
        clickedTrackedIds = ConcurrentSkipListSet()
        clickedItemsIndexes = ConcurrentSkipListSet()
        setNoObjectFound()
    }

    /**
     * Method that resets the bounding box
     */
    fun setNoObjectFound(){
        box.left = 0f
        box.top = 0f
        box.right = 0f
        box.bottom = 0f

        invalidate()
    }

    /**
     * Sets the detected object and updates the graphics
     */
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

    /**
     * Method that calculates the rect that will contain some text. This is used to generate
     * a "background effect" for the displayed text.
     * This code was readapted from:
     * https://stackoverflow.com/questions/8242439/how-to-draw-text-with-background-color-using-canvas
     */
    private fun getTextBackgroundSize(x: Float, y: Float, text: String): Rect {
        val fontMetrics = textPaint.fontMetrics
        val halfTextLength = textPaint.measureText(text) / 2 + 5
        return Rect(
            x.toInt(),
            (y + fontMetrics.top ).toInt()+20,
            (x + halfTextLength*2).toInt(),
            (y + fontMetrics.bottom ).toInt()
        )
    }

    /**
     * Draws bounding boxes, labels (and their "background") of the detected object
     */
    override fun onDraw(canvas: Canvas) {
        if (!::objDetected.isInitialized || (box.left == 0f && box.top == 0f &&  box.right == 0f && box.bottom == 0f))
            return

        // Paint bb
        textPaint.style = Paint.Style.STROKE
        canvas.drawRect(box, textPaint)

        // Draw labels with background
        textPaint.style = Paint.Style.FILL

        val textX = box.centerX() - box.width() / 2
        var textY = box.centerY() + textPaint.textSize / 2

        if (objDetected.labels.size == 0) {
            val textToShow = "" + objDetected.trackingId + ". Unknown object"
            val background = getTextBackgroundSize(textX, textY, textToShow)
            canvas.drawRect(background, backgroundPaint)
            canvas.drawText(textToShow, textX, textY, textPaint)
            return
        }


        var numOrNull = "" + objDetected.trackingId + ". "
        objDetected.labels.forEach {label ->
            val textToShow = numOrNull + label.text + "(" +String.format("%.2f", label.confidence)+ ") "
            numOrNull = ""

            val background = getTextBackgroundSize(textX, textY, textToShow)
            canvas.drawRect(background, backgroundPaint)

            canvas.drawText(textToShow, textX, textY, textPaint)

            textY += textPaint.textSize
        }

    }


}