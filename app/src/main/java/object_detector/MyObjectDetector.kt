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
import java.util.concurrent.ConcurrentSkipListSet

class MyObjectDetector (private val viewBinding: ActivityCheckListWithImagesBinding) : OnSuccessListener<List<DetectedObject>>, OnFailureListener {
    private var objecteBoundingBoxView: ObjectBoundingBoxView = viewBinding.faceBoundingBoxView
    private val imageDetector: ObjectDetector
    private var width : Int = -1
    private var height : Int = -1

    private val SEPARATOR_LABEL = " - "
    private val LABEL_TO_SKIP = "Clothing"

    // List of detected items' ids
    // It's thread-safe.
    // Expected average logarithmic time cost for the contains and add
    private var detectedIdsItems = ConcurrentSkipListSet<Int>()
    private var detectedLabelsItems = ConcurrentSkipListSet<String>()



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
                .setClassificationConfidenceThreshold(0.5f)
                .setMaxPerObjectLabelCount(4)
                //.enableMultipleObjects()
                .build()

        imageDetector = ObjectDetection.getClient(customObjectDetectorOptions)
    }

    fun getListOfDetectedItems(): ConcurrentSkipListSet<String> {
        return detectedLabelsItems
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
            objecteBoundingBoxView.setNoneFound()
            return;
        }

        val boundingBoxes = mutableListOf<Rect>()
        val texts = mutableListOf<String>()

        for (obj in detectedObjects) {
            boundingBoxes.add(obj.boundingBox)


            obj.labels?.forEach { label ->
                if (!detectedIdsItems.contains(label.index)) {
                    // New object detected
                    Log.d("Items_rilevati", "just found " + label.text + " with index " + label.index)
                    detectedIdsItems.add(label.index)
                    detectedLabelsItems.add(label.text)
                }

            }


            var labelTexts = obj.labels.joinToString(separator = SEPARATOR_LABEL) { it.text }

            if (labelTexts != LABEL_TO_SKIP) {
                labelTexts = labelTexts.replace(LABEL_TO_SKIP + SEPARATOR_LABEL, "").trim()
            }

            labelTexts = "" + obj.trackingId + ". " + labelTexts

            val tmp: String = if (obj.labels?.isNotEmpty() == true) obj.labels!![0].text else ""
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