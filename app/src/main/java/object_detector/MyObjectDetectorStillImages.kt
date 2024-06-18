package object_detector

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.ObjectDetector
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import object_detector.MyObjectDetectorCamera.Companion.CUSTOM_MODEL_PATH
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList


/**
 * Class that handles the object detection on static images
 */
class MyObjectDetectorStillImages (private var callbackResult: (List<Long>) -> Void) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    /** Object detector (provided by ML Kit) */
    private val imageDetector: ObjectDetector

    /** Number of images that are asked to process */
    private var numberOfImagesToProcess : Int = 0

    /** Number of already processed images */
    private var numberOfAlreadyProcessedImages : Int = 0

    /** Map of ids and names of tracked items */
    private lateinit var resultMap : ConcurrentHashMap<Long, String>

    init {
        val localModel = LocalModel.Builder()
            .setAssetFilePath(CUSTOM_MODEL_PATH)
            .build()
        val customObjectDetectorOptions =
            CustomObjectDetectorOptions.Builder(localModel)
                .setDetectorMode(CustomObjectDetectorOptions.SINGLE_IMAGE_MODE)
                .enableClassification()
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(3)
                .enableMultipleObjects()
                .build()
        imageDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    /**
     * Function that updates the number of already processed images and possibly calls the
     * call-back function, to inform of the end of the detection
     */
    fun passToNextImageOrBackToVisualize(){
        numberOfAlreadyProcessedImages++
        if (numberOfAlreadyProcessedImages == numberOfImagesToProcess)
            callbackResult.invoke(resultMap.keys().toList())
    }

    /**
     * Method that performs object detection on the input image
     */
    fun processImages(context : Context, urisOfImagesChosenByUser : CopyOnWriteArrayList<Uri> ){
        Log.d("img", "img arrivata");
        numberOfImagesToProcess = urisOfImagesChosenByUser.size
        resultMap = ConcurrentHashMap<Long, String>()
        for (imageUri : Uri in urisOfImagesChosenByUser) {
            val image: InputImage = try {
                InputImage.fromFilePath(context, imageUri)
            } catch (e: IOException) {
                resultMap[-1L] = "-"
                passToNextImageOrBackToVisualize()
                continue
            }

            /*
            // Invalid image for testing
            val invalidBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
            image = InputImage.fromBitmap(invalidBitmap, 0)
            */


            imageDetector.process(image)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
                .addOnCompleteListener {
                    passToNextImageOrBackToVisualize()
                }
        }
    }


    /**
     * Call-back for successful object detection. It will update the currently detected object
     */
    override fun onSuccess(detectedObjects: List<DetectedObject>?) {
        if (!detectedObjects.isNullOrEmpty()) {
            for (obj in detectedObjects) {
                obj.labels.forEach { label ->
                    resultMap[label.index.toLong()] = label.text
                }
            }
        }
    }

    /**
     * Call-back for failed object detection
     */
    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        Log.d("objectDetector", "object detector still image. error:" + e.message)
        resultMap[-1L] = "-"


    }


}