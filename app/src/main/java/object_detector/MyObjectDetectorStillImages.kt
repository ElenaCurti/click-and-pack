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
import object_detector.MyObjectDetectorCamera.Companion.CUSTOM_MODEL_PATH
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentSkipListSet

class MyObjectDetectorStillImages (private var callbackResult: (Map<Long, String>) -> Unit) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    private val imageDetector: ObjectDetector
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


    fun processImage(image: InputImage){
        imageDetector.process(image)
            .addOnSuccessListener(this)
            .addOnFailureListener(this)
    }

    override fun onSuccess(detectedObjects: List<DetectedObject>?) {
        val resultMap = mutableMapOf<Long, String>()

        if (detectedObjects == null || detectedObjects?.size == 0 ) {
            callbackResult.invoke(resultMap);
            return;
        }

        for (obj in detectedObjects) {

            obj.labels?.forEach { label ->
                //label.index label.text
                resultMap[label.index.toLong()] = label.text;
            }
        }
        callbackResult.invoke(resultMap);
    }

    override fun onFailure(e: Exception) {
        // Task failed with an exception. Should never be called
        Log.d("RESULT_IMAGE_LAB", "error:" + e.message);
        val resultMap = mutableMapOf<Long, String>()
        resultMap[-1L] = "Error with image processing"  // TODO string
        callbackResult.invoke(resultMap)
    }


}