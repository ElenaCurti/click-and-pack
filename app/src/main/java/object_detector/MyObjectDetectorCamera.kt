package object_detector

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.clickandpack.databinding.ActivityCheckListWithCameraBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class MyObjectDetectorCamera (private val viewBinding: ActivityCheckListWithCameraBinding) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    // Handler for coloured boxes with detected items
    private var objecteBoundingBoxView: ObjectBoundingBoxView = viewBinding.objectBoundingBoxView

    // Object detector
    private val objectDetector: ObjectDetector

    // Images' width and height
    private var width : Int = -1
    private var height : Int = -1

    private var  errorDuringProcessingOfCamera : Boolean = false

    companion object {
        // Separator between one label and another
        const val SEPARATOR_LABEL = "\n"

        // Path to custom model
        const val CUSTOM_MODEL_PATH = "custom_object_detector/object_detector.tflite"
    }

    // Label to skip because it's too generic
    private val LABEL_TO_SKIP = "Clothing"

    // List of detected items' ids. It's thread-safe.
    // Key is tracked id of the item. Value is the list of the object' labels' indexes
    val mapTrackedIdToIndex = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    init {
        // Object detector initiation
        val localModel = LocalModel.Builder()
            .setAssetFilePath(CUSTOM_MODEL_PATH)
            .build()

        // STREAM_MODE is necessary for live camera for the tracking ids
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.6f)
                .setMaxPerObjectLabelCount(3)
                //.enableMultipleObjects()
                .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    fun getListOfDetectedAndClickedItems(): List<Int> {
        // Return the list of clicked (packed) items

        var clickedTrackingIds : ConcurrentSkipListSet<Int> = objecteBoundingBoxView.getClickedTrackingIds();

        val valuesForClickedKeys = mutableListOf<Int>()
        for (key in clickedTrackingIds) {
            mapTrackedIdToIndex[key]?.let { valuesForClickedKeys.addAll(it) }
        }
        if (errorDuringProcessingOfCamera)
            valuesForClickedKeys.add(-1)
        return valuesForClickedKeys
    }

    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy){

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            if (width == -1 || height == -1){
                width = imageProxy.width
                height = imageProxy.height
            }

            /*
            // Invalid image for testing
            val invalidBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            image = InputImage.fromBitmap(invalidBitmap, 0)
            */


            objectDetector.process(image)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
                .addOnCompleteListener {
                    // Necessary lines to make cameraX work properly
                    mediaImage.close()
                    imageProxy.close()
                }
        }

    }

    override fun onSuccess(detectedObjects: List<DetectedObject>?) {
        // Callback with list of detected items
        if (detectedObjects == null || detectedObjects?.size == 0 ) {
            objecteBoundingBoxView.setNoObjectFound()
            return;
        }

        val boundingBoxes = mutableListOf<Rect>()
        val texts = mutableListOf<String>()

        for (obj in detectedObjects) {
            val trackingId = obj.trackingId ?: -1  // Should never be -1

            // Possibly i save the new tracked item
            mapTrackedIdToIndex.putIfAbsent(trackingId, ConcurrentSkipListSet())

            // If a new label was assigned to a tracked object, i add it to the list
            obj.labels?.forEach { label ->
                var listOfIndexesForTrackedObject = mapTrackedIdToIndex[obj.trackingId]!!
                if (!listOfIndexesForTrackedObject.contains(label.index)){
                    listOfIndexesForTrackedObject.add(label.index)
                }
            }

            // Set text and size of coloured boxes to show
            val filteredLabels = if (obj.labels.any { it.text == LABEL_TO_SKIP && obj.labels.size > 1 }) {
                obj.labels.filterNot { it.text == LABEL_TO_SKIP }   // Skip generic label
            } else {
                obj.labels
            }
            var labelTexts = filteredLabels.joinToString(separator = SEPARATOR_LABEL) { it.text + "(" +  String.format("%.2f", it.confidence) + ")" }
            labelTexts = "" + obj.trackingId + ". " + labelTexts
            texts.add(labelTexts)

            boundingBoxes.add(obj.boundingBox)
        }

        // Set coloured bounding boxes
        objecteBoundingBoxView.setMultipleBoundingBoxes(boundingBoxes, width, height, texts)
    }

    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        errorDuringProcessingOfCamera = true
        Log.d("objectDetector", "object detector camera. error:" + e.message);
    }


}