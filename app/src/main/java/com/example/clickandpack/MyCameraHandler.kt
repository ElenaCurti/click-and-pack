package com.example.clickandpack;



import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis


import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithCameraBinding
import object_detector.MyObjectDetectorCamera
import java.util.concurrent.ExecutorService
// TODO fai classe unica per mycamera handler e checklist with camera
class MyCameraHandler (private val viewBinding: ActivityCheckListWithCameraBinding, private val cameraExecutor: ExecutorService)  {

    private val CAMERA_TAG = "CAMERA_TAG";
    private val myObjDetector =  MyObjectDetectorCamera(viewBinding)

    fun getListOfDetectedItems(): List<Int> {
        return myObjDetector.getListOfDetectedAndClickedItems()
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun  startCameraView(mainActivity: AppCompatActivity){

        val lineAnalyzer = ImageAnalysis.Builder()
                .build()
                .apply {
            setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
                // A new image is available from camera, so object detection algorithm will start with that image
                myObjDetector.processImageProxy(imageProxy)
            })
        }

        // Starting camera and camera preview. Code found here:
        // https://developer.android.com/media/camera/camerax/
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
}