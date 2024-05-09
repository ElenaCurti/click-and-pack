package com.example.clickandpack

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CheckListWithImages : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCheckListWithImagesBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: MyCameraHandler

    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private var response: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCheckListWithImagesBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.floatingActionButtonBackToHome.setOnClickListener( View.OnClickListener {
            backToVisualizeList()
        } )

        startCamera()



    }

    // Camera permission and (possibly) visualization
    private fun startCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission is not already granted, so I request it
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_REQUEST_CODE
            )
        } else {
            // Permission is granted, so I start the camera
            if (!::cameraExecutor.isInitialized)
                cameraExecutor = Executors.newSingleThreadExecutor()
            if (!::cameraManager.isInitialized )
                cameraManager = MyCameraHandler(viewBinding, cameraExecutor)
            cameraManager.startCameraView(this)
        }
    }

    // Camera permission
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Camera permission granted
                startCamera()
            } else {
                // Camera permission denied
                response = getString(R.string.camera_permission_denied_error)
                Log.d("TAG", response)
                backToVisualizeList()
            }
        }
    }

    // Back to previous view
    private fun backToVisualizeList() {
        val i = Intent()
        i.putExtra(VisualizeList.RESPONSE_KEY_FROM_IMAGE_CHECKER, response)
        setResult(RESULT_OK, i)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::cameraExecutor.isInitialized)
            cameraExecutor.shutdown()
    }
}