package com.example.clickandpack;



import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy


import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import object_detector.MyObjectDetector
import object_detector.ObjectBoundingBoxView
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService

class MyCameraHandler (private val viewBinding: ActivityCheckListWithImagesBinding, private val cameraExecutor: ExecutorService)  {

    private val CAMERA_TAG = "CAMERA_TAG";
    private val myObjDetector =  MyObjectDetector(viewBinding)

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun  startCameraView(mainActivity: AppCompatActivity, idListOfDetectableItems: Set<Long>){

        val lineAnalyzer = ImageAnalysis.Builder()
                .build()
                .apply {
            setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                // A new image is available, object detection algorithm will start with that image
                myObjDetector.processImageProxy(imageProxy)

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

    fun getListOfDetectedItems(): ConcurrentSkipListSet<String> {
        return myObjDetector.getListOfDetectedItems()
    }

    private fun setBoundingBox_old(view: View, x: Int, y: Int, width: Int, height: Int) {
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

    private fun setBoundingBox(view: View, marginLeft: Int, marginTop: Int, width: Int, height: Int) {
        val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
        layoutParams.apply {
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
        Log.d(CAMERA_TAG, "sono nell image analyzer")

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