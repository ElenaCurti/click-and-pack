package com.example.clickandpack;



import android.util.Log
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import java.util.concurrent.ExecutorService

class MyCameraHandler (private val viewBinding: ActivityCheckListWithImagesBinding, private val cameraExecutor: ExecutorService)  {

    private var save_img : Boolean  = false
    private lateinit var image_g: ImageProxy
    private var i: Int = 0
    private val CAMERA_TAG = "CAMERA_TAG";

    fun startCameraView(mainActivity: AppCompatActivity){

        val lineAnalyzer = ImageAnalysis.Builder()
                .build()
                .apply {
            setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { image ->
                newImageAvailable();
                image.close()
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