package com.example.clickandpack;



import android.util.Log
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.ExecutorService

class MyCameraHandler (private val viewBinding: ActivityCheckListWithImagesBinding, private val cameraExecutor: ExecutorService)  {

    private var save_img : Boolean  = false
    private lateinit var image_g: ImageProxy
    private var i: Int = 0
    private val CAMERA_TAG = "CAMERA_TAG";



    @androidx.camera.core.ExperimentalGetImage
    fun startCameraView(mainActivity: AppCompatActivity){
        val labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)


        val lineAnalyzer = ImageAnalysis.Builder()
                .build()
                .apply {
            setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                newImageAvailable();

                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image =
                        InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)

                    labeler.process(image)
                        .addOnSuccessListener { labels ->
                            // Task completed successfully
                            // ...
                            viewBinding.linearLayoutResultML.removeAllViews()
                            var textView = TextView(mainActivity)

                            Log.d("RESULT_IMAGE_LAB", "ok. " + labels.size);
                            var names : String = "";
                            labels.forEach { l ->
                                names += "" + l.index + "." + l.text + " "
                            }

                            textView.text = names
                            viewBinding.linearLayoutResultML.addView(textView)
                            //..textView_resultML.text = names;
                            Log.d("RESULT_IMAGE_LAB", names);




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