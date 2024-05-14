// Code to draw rect found here
// https://medium.com/@mrizqi070502/building-a-face-detection-app-with-mlkit-a-step-by-step-guide-b729429119ec

package object_detector

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import object_detector.MyObjectDetectorCamera.Companion.SEPARATOR_LABEL
import java.util.concurrent.ConcurrentSkipListSet


@SuppressLint("ClickableViewAccessibility") // otherwise waring in init
class ObjectBoundingBoxView(context: Context, attrs: AttributeSet?) : View(context, attrs), OnTouchListener{
    // Displayed boxes' stuff
    private var boundingBoxes: List<RectF>? = null
    private var texts: List<String>? = null
    private var selectedRectIndex: Int = -1

    // Digit(s), point, space, end of text --> means the object has no label
    private val regexObjectIsUnknown = Regex("^\\d+\\.\\s$")

    // Tracking ids of the clicked items (the one how will be setted as "packed")
    private var clickedTrackedIds = ConcurrentSkipListSet<Int>()

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
        if (boundingBoxes == null || event == null)
            return true

        val x = event.x
        val y = event.y
        Log.d("item_cliccato", "x:" + x + " y:" + y)

        if (event.action == MotionEvent.ACTION_DOWN ) {
            // Retrieve index of clicked box
            selectedRectIndex = getSelectedRectIndex(x,y)
            if (selectedRectIndex == -1)
                return true

            // If clicked object was correctly labeled, it will be set as "packed"
            var savedText: String = texts!!.get(selectedRectIndex)
            if (!regexObjectIsUnknown.matches(savedText)) {
                clickedTrackedIds.add(extractTrackingId(texts!!.get(selectedRectIndex)))
                invalidate() // Re-draw window
            }
        }

        return false
    }

    fun getClickedTrackingIds() : ConcurrentSkipListSet<Int>{
        return clickedTrackedIds
    }
    private fun getSelectedRectIndex(x: Float, y: Float): Int {
        boundingBoxes?.forEachIndexed { index, rect ->
            if (rect.contains(x, y)) {
                return index
            }
        }
        return -1
    }

    fun setNoObjectFound(){
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

    private fun extractTrackingId(input: String): Int {
        val regex = Regex("""(\d+)\.""")
        val matchResult = regex.find(input)
        val numberString = matchResult?.groups?.get(1)?.value
        return numberString!!.toInt()
    }

    private fun isObjectPacked(input: String): Boolean {
       var number = extractTrackingId(input)
       return number != null && clickedTrackedIds.contains(number)
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
        // For each box (only one at the time will be displayed if in the
        // detector it wasn't enabled the multiple objects' detection), draw
        // a box and "print" labels
        boundingBoxes?.forEachIndexed { index, box ->

            // Retrieve the labels t referred to the box
            texts?.getOrNull(index)?.let { t ->
                // Set color based on the item "status"
                var textToShow : String = t
                if (isObjectPacked(t)) {
                    // Packed item
                    textPaint.color = Color.GREEN
                    backgroundPaint.color = Color.BLACK
                } else if (regexObjectIsUnknown.matches(t)) {
                    // Unrecognised item
                    textToShow = t + "Unknown object" // TODO metti in string.xml
                    textPaint.color = Color.RED
                    backgroundPaint.color = Color.WHITE
                } else {
                    // Recognised, but not packed item
                    textPaint.color = Color.BLUE
                    backgroundPaint.color = Color.WHITE
                }

                // Draw surrounding rectangle
                textPaint.style = Paint.Style.STROKE
                canvas.drawRect(box, textPaint)

                // Draw labels with background
                textPaint.style = Paint.Style.FILL

                val textX = box.centerX() - box.width() / 2
                var initialTextY = box.centerY() + textPaint.textSize / 2

                val labels = textToShow.split(SEPARATOR_LABEL)

                // First i draw the background items
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
    }


}