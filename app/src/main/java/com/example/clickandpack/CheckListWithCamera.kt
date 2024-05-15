package com.example.clickandpack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clickandpack.databinding.ActivityCheckListWithCameraBinding
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CheckListWithCamera : AppCompatActivity() {
    // Handlers for camera
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: MyCameraHandler

    // "xml" file of the view
    private lateinit var viewBinding: ActivityCheckListWithCameraBinding

    // Request code for camera permissions
    private val CAMERA_PERMISSION_REQUEST_CODE = 1

    // Response in case of errors
    private var response: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCheckListWithCameraBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.floatingActionButtonBackToHome.setOnClickListener {
            backToVisualizeList()
        }

        runOnUiThread { startCamera() }
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
        i.putExtra(VisualizeList.RESPONSE_KEY_FROM_CAMERA_CHECKER, response)
        if (::cameraManager.isInitialized ) {
            var listOfDetectedItems: List<Int> = cameraManager.getListOfDetectedItems()
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
}