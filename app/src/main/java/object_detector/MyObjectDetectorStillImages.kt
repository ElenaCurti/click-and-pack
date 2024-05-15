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


class MyObjectDetectorStillImages (private var callbackResult: (List<Long>) -> Void) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    private val imageDetector: ObjectDetector
    private var numberOfImagesToProcess : Int = 0
    private var numberOfAlreadyProcessedImages : Int = 0

    // Map of ids and names of tracked items
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


    fun processImages(context : Context, urisOfImagesChosenByUser : CopyOnWriteArrayList<Uri> ){
        numberOfImagesToProcess = urisOfImagesChosenByUser.size
        resultMap = ConcurrentHashMap<Long, String>()
        for (imageUri : Uri in urisOfImagesChosenByUser) {
            var image: InputImage = try {
                InputImage.fromFilePath(context, imageUri)
            } catch (e: IOException) {
                numberOfAlreadyProcessedImages++
                continue
            }

            imageDetector.process(image)
                .addOnSuccessListener(this)
                .addOnFailureListener(this)
        }
    }

    override fun onSuccess(detectedObjects: List<DetectedObject>?) {
        if (!detectedObjects.isNullOrEmpty()) {
            for (obj in detectedObjects) {
                obj.labels?.forEach { label ->
                    resultMap[label.index.toLong()] = label.text;
                }
            }
        }
        numberOfAlreadyProcessedImages++
        if (numberOfAlreadyProcessedImages == numberOfImagesToProcess)
            callbackResult.invoke(resultMap.keys().toList())
    }

    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        Log.d("RESULT_IMAGE_LAB", "error:" + e.message);
        resultMap[-1L] = "Error with image processing"  // TODO string + handle this case
        callbackResult.invoke(resultMap.keys().toList())

    }


}