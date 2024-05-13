package object_detector

import android.graphics.Rect
import android.util.Log
import androidx.camera.core.ImageProxy
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class MyObjectDetector (private val viewBinding: ActivityCheckListWithImagesBinding) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    private var objecteBoundingBoxView: ObjectBoundingBoxView = viewBinding.objectBoundingBoxView
    private val imageDetector: ObjectDetector
    private var width : Int = -1
    private var height : Int = -1

    companion object {
        const val SEPARATOR_LABEL = "\n"
    }

    private val LABEL_TO_SKIP = "Clothing"

    // List of detected items' ids
    // It's thread-safe.
    // Expected average logarithmic time cost for the contains and add
    private var detectedIdsItems = ConcurrentSkipListSet<Int>()
    private var detectedLabelsItems = ConcurrentSkipListSet<String>()

    private var packedTrackedIds = ConcurrentSkipListSet<Int>()

    // Key is tracked id of the item. Value is the list of the object' labels' indexes
    val mapTrackedIdToIndex = ConcurrentHashMap<Int, ConcurrentSkipListSet<Int>>()

    private var packedLabelsItems = ConcurrentSkipListSet<String>()

    init {
        // Image Labeling
        // val imageDetector = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)

        // Or object detector
        val localModel = LocalModel.Builder()
            .setAssetFilePath("custom_object_detector/object_detector.tflite")
            .build()

        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.STREAM_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.6f)
                .setMaxPerObjectLabelCount(2)
                //.enableMultipleObjects()
                .build()

        imageDetector = ObjectDetection.getClient(customObjectDetectorOptions)

    }

    fun getListOfDetectedItems(): List<Int> {

        var clickedTrackingIds : ConcurrentSkipListSet<Int> = objecteBoundingBoxView.getClickedTrackingIds();

        val valuesForClickedKeys = mutableListOf<Int>()
        for (key in clickedTrackingIds) {
            mapTrackedIdToIndex[key]?.let { valuesForClickedKeys.addAll(it) }
        }
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

            imageDetector.process(image)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
                .addOnCompleteListener {
                    // Necessary to make cameraX work properly
                    mediaImage.close()
                    imageProxy.close()
                }
        }

    }


    fun processImage(image: InputImage){
        if (width == -1 || height == -1){
            width = image.width
            height = image.height
        }
        imageDetector.process(image)
            .addOnSuccessListener(this)
            .addOnFailureListener(this)
    }

    override fun onSuccess(detectedObjects: List<DetectedObject>?) {

        if (detectedObjects == null || detectedObjects?.size == 0 ) {
            objecteBoundingBoxView.setNoObjectFound()
            return;
        }

        val boundingBoxes = mutableListOf<Rect>()
        val texts = mutableListOf<String>()

        for (obj in detectedObjects) {
            boundingBoxes.add(obj.boundingBox)

            val trackingId = obj.trackingId ?: -1  // Should never be -1
            mapTrackedIdToIndex.putIfAbsent(trackingId, ConcurrentSkipListSet())

            obj.labels?.forEach { label ->
                /*if (!detectedIdsItems.contains(label.index)) {
                    // New object detected
                    Log.d("Items_rilevati", "just found " + label.text + " with index " + label.index)
                    detectedIdsItems.add(label.index)
                    detectedLabelsItems.add(label.text)
                }*/

                // If a new label was assigned to a tracked object, i add it
                var listOfIndexesForTrackedObject = mapTrackedIdToIndex[obj.trackingId]!!
                if (!listOfIndexesForTrackedObject.contains(label.index)){
                    listOfIndexesForTrackedObject.add(label.index)
                }


            }
            val filteredLabels = if (obj.labels.any { it.text == LABEL_TO_SKIP && obj.labels.size > 1 }) {
                obj.labels.filterNot { it.text == LABEL_TO_SKIP }
            } else {
                obj.labels
            }
            var labelTexts = filteredLabels.joinToString(separator = SEPARATOR_LABEL) { it.text + "(" +  String.format("%.2f", it.confidence) + ")" }


            labelTexts = "" + obj.trackingId + ". " + labelTexts

            Log.d("item_trovato" , "" + obj.trackingId + " - " +  labelTexts )

            texts.add(labelTexts)
        }

        objecteBoundingBoxView.setMultipleBoundingBoxes(boundingBoxes, width, height, texts)
    }

    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        Log.d("RESULT_IMAGE_LAB", "error:" + e.message);
    }


}