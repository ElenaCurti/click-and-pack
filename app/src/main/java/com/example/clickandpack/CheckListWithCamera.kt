package com.example.clickandpack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithCameraBinding
import object_detector.MyObjectDetectorCamera
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CheckListWithCamera : AppCompatActivity() {
    // Handler for camera
    private lateinit var cameraExecutor: ExecutorService

    // Object detector
    private lateinit var myObjDetector : MyObjectDetectorCamera

    // "xml" file of the view
    private lateinit var viewBinding: ActivityCheckListWithCameraBinding

    // Request code for camera permissions
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    // Response in case of errors
    private var errorResponse: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCheckListWithCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.floatingActionButtonBackToHome.setOnClickListener {
            backToVisualizeList()
        }

        runOnUiThread { handleCamera() }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                handleCamera()
            } else {
                // Camera permission denied
                errorResponse = getString(R.string.camera_permission_denied_error)
                backToVisualizeList()
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToVisualizeList()
    }

    private fun backToVisualizeList() {
        // Back to previous view
        val i = Intent()
        i.putExtra(VisualizeList.RESPONSE_ERROR_KEY_FROM_CAMERA_CHECKER, errorResponse)
        if (::myObjDetector.isInitialized ) {
            var listOfDetectedItems: List<Int> =  myObjDetector.getListOfDetectedAndClickedItems()

            Log.d("Item_arrivato", "sono qui" )
            for (itemIndex:Int in listOfDetectedItems)
                Log.d("Item_arrivato", "" +  itemIndex )
            val arrayList: ArrayList<Int> = ArrayList(listOfDetectedItems)
            i.putExtra(VisualizeList.RESPONSE_DETECTED_INDEXES_FROM_IMAGE_CHECKER, arrayList)
        }
        setResult(RESULT_OK, i)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized)
            cameraExecutor.shutdown()
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun  handleCamera(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED ) {
            // Permission is not already granted, so I request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Permission is granted, so I start the camera

        if (!::cameraExecutor.isInitialized)
            cameraExecutor = Executors.newSingleThreadExecutor()
        if (!::myObjDetector.isInitialized )
            myObjDetector =  MyObjectDetectorCamera(viewBinding)

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
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

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
                    this, cameraSelector, preview, lineAnalyzer)

            } catch(exc: Exception) {
                Log.e("camera", "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

}