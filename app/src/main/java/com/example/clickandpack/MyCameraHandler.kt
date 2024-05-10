package com.example.clickandpack;



import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.DetectedObject
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.custom.CustomObjectDetectorOptions
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService

class MyCameraHandler (private val viewBinding: ActivityCheckListWithImagesBinding, private val cameraExecutor: ExecutorService)  {

    private var save_img : Boolean  = false
    private lateinit var image_g: ImageProxy
    private var i: Int = 0
    private val CAMERA_TAG = "CAMERA_TAG";
    // Thread-safe. Expected average logarithmic time cost for the contains and add
    private var detectedIdsItems = ConcurrentSkipListSet<Int>()

    @androidx.camera.core.ExperimentalGetImage
    fun startCameraView(mainActivity: AppCompatActivity, idListOfDetectableItems: Set<Long>){
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
                .setMaxPerObjectLabelCount(3)
                .build()

        val imageDetector =
            ObjectDetection.getClient(customObjectDetectorOptions)



        val lineAnalyzer = ImageAnalysis.Builder()
                .build()
                .apply {
            setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                newImageAvailable();

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                        imageDetector.process(image)
                            .addOnSuccessListener { detectedObjects ->
                            /*.addOnSuccessListener { labels ->
                                // Task completed successfully
                                // ...

                                var textViewProva = TextView(mainActivity)

                                Log.d("RESULT_IMAGE_LAB", "ok. " + labels.size);
                                var names : String = "";
                                labels.forEach { l ->
                                    // Image labeler
                                    names += "" + l.index + "." + l.text + " "

                                    if (idListOfDetectableItems.contains(l.index.toLong()) && !detectedIdsItems.contains(l.index)) {
                                        detectedIdsItems.add(l.index)
                                        var newItemName =  TextView(mainActivity)
                                        newItemName.text = l.text
                                        viewBinding.linearLayoutResultML.addView(newItemName, 0)
                                    }
                                 }
                                 */


                                viewBinding.linearLayoutResultML.removeAllViews()
                                var textViewProva2 = TextView(mainActivity)

                                for (obj in detectedObjects){
                                    textViewProva2.text =  (textViewProva2.text ?: "") as String +  "\n"
                                    Log.d("item_trovato", "nuovo item")
                                    for (lab: DetectedObject.Label in obj.labels) {
                                        textViewProva2.text =
                                            (textViewProva2.text ?: "") as String + " - " + lab.text
                                        Log.d("item_trovato", "index:" + lab.index + " label:" + lab.text)
                                    }

                                }
                                viewBinding.linearLayoutResultML.addView(textViewProva2)

                                //..textView_resultML.text = names;




                        }
                        .addOnFailureListener { e ->
                            // Task failed with an exception
                            // ...
                            Log.d("RESULT_IMAGE_LAB", "error:" + e.message);
                        }
                        .addOnCompleteListener {
                            mediaImage.close()
                            imageProxy.close() }
                }
            })
        }

        val cameraProviderFuture = ProcessCameraProvider.getInstance(mainActivity)

        // Used to bind the lifecycle of cameras to the lifecycle owner
        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                    .build()
                    .also {
                it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
            }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                        mainActivity, cameraSelector, preview, lineAnalyzer)

            } catch(exc: Exception) {
                Log.e(CAMERA_TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(mainActivity))


    }

    fun getListOfDetectedItems(): ConcurrentSkipListSet<Int> {
        for (itemId in detectedIdsItems) {
            Log.d("Items_rilevati", "" + itemId)
        }

        return detectedIdsItems
    }

    private fun setBoundingBox(view: View, x: Int, y: Int, width: Int, height: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.apply {
            // Calculate margins relative to the view
            val marginLeft = x - view.x.toInt()
            val marginTop = y - view.y.toInt()

            // Set the margins and size
            topMargin = marginTop
            leftMargin = marginLeft
            this.width = width
            this.height = height
        }
        view.layoutParams = layoutParams
    }

    private fun newImageAvailable() = viewBinding.viewFinder.post {
        // "callback" for every new image
        Log.d(CAMERA_TAG, "sono nell image analyzer. save_img = " + save_img)

        // Set box dimension
        var xPreview =  viewBinding.viewFinder.x.toInt()
        var yPreview = viewBinding.viewFinder.y.toInt()
        var width_preview = viewBinding.viewFinder.width
        var height_preview = viewBinding.viewFinder.height

        (viewBinding.boxPrediction.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin =  yPreview + 100
            leftMargin =  xPreview + 100
            width = width_preview - 120
            height = height_preview - 120
        }
    }

}