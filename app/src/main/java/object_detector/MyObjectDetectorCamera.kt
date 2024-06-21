package object_detector

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
import java.util.concurrent.ConcurrentSkipListSet

/**
 * Class that handles the real-time camera object detection
 */
class MyObjectDetectorCamera(viewBinding: ActivityCheckListWithCameraBinding) :
    OnSuccessListener<List<DetectedObject>>, OnFailureListener {

    /** Handler for coloured boxes with detected items */
    private var objectBoundingBoxView: ObjectBoundingBoxView = viewBinding.objectBoundingBoxView

    /** Object detector (provided by ML Kit) */
    private val objectDetector: ObjectDetector

    /** Images' width and height */
    private var width: Int = -1
    private var height: Int = -1

    /** Attribute that contains whether there was an error or not  */
    private var errorDuringProcessingOfCamera: Boolean = false


    /** Path of the custom model used for object detection */
    companion object {
        // Path to custom model
        const val CUSTOM_MODEL_PATH = "custom_object_detector/object_detector.tflite"
    }

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
                //.enableMultipleObjects()  // Actual graphics won't work with multiple objects
                .build()

        objectDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    fun cleanTheDetectedItems() {
        objectBoundingBoxView.cleanDetectedItems()
    }

    /**
     * Method that returns the ids of the detected and clicked items during the real-time detection
     * @return ids of the items
     */
    fun getListOfDetectedAndClickedItems(): ConcurrentSkipListSet<Int> {
        // Return the list of clicked (packed) items' indexes and the restored one
        return objectBoundingBoxView.getClickedItemsIndexes()
    }

    /**
     * Method that performs object detection on the input image
     */
    @androidx.camera.core.ExperimentalGetImage
    fun processImageProxy(imageProxy: ImageProxy) {

        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

            if (width == -1 || height == -1) {
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

    /**
     * Call-back for successful object detection. It will update the current shown bounding box
     * (size and labels)
     */
    override fun onSuccess(detectedObjects: List<DetectedObject>?) {
        // Callback with list of detected items
        if (detectedObjects.isNullOrEmpty()) {
            objectBoundingBoxView.setNoObjectFound()
            return;
        }

        if (detectedObjects.size > 1)
            Log.e("object detection", "Multiple object detection not supported!")

        // Draw detected object
        objectBoundingBoxView.setDetectedObject(detectedObjects[0], width, height)
    }

    /**
     * Call-back for failed object detection
     */
    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        errorDuringProcessingOfCamera = true
        Log.d("objectDetector", "object detector camera. error:" + e.message)
    }


}