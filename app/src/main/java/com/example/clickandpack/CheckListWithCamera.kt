package com.example.clickandpack

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.window.OnBackInvokedDispatcher
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.clickandpack.databinding.ActivityCheckListWithImagesBinding
import database_handler.AppDatabase
import database_handler.ItemEntity
import java.util.concurrent.ConcurrentSkipListSet
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CheckListWithCamera : AppCompatActivity() {
    private lateinit var viewBinding: ActivityCheckListWithImagesBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var cameraManager: MyCameraHandler

    private val CAMERA_PERMISSION_REQUEST_CODE = 1
    private var response: String = ""
    private lateinit var idListOfDetectableItems : Set<Long>
    private lateinit var appDatabase: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCheckListWithImagesBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.floatingActionButtonBackToHome.setOnClickListener( View.OnClickListener {
            backToVisualizeList()
        } )


        //  Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
        val t = Thread {
            appDatabase = Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java, AppDatabase.DB_NAME
            ).build()
            var listItemsTmp = appDatabase.itemDao().allDetectableItems
            val listIdTmp =  mutableListOf<Long>()
            for(ie : ItemEntity in listItemsTmp)
                listIdTmp.add(ie.id)
            idListOfDetectableItems = listIdTmp.toSet()

        }
        t.start()

        // Wait for the background thread to finish
        try {
            t.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        runOnUiThread {
            startCamera()
        }
    }

    public fun idListOfDetectableItems() : Set<Long> {
        return idListOfDetectableItems
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
            cameraManager.startCameraView(this, idListOfDetectableItems)
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

    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        backToVisualizeList()
    }

    // Back to previous view
    private fun backToVisualizeList() {
        val i = Intent()
        i.putExtra(VisualizeList.RESPONSE_KEY_FROM_IMAGE_CHECKER, response)
        if (::cameraManager.isInitialized ) {
            var listOfDetectedItems: ConcurrentSkipListSet<String> =
                cameraManager.getListOfDetectedItems()
            Log.d("Item_arrivato", "sono qui" )
            for (item:String in listOfDetectedItems)
                Log.d("Item_arrivato", item )
            val arrayList: ArrayList<String> = ArrayList(listOfDetectedItems)
            i.putExtra(VisualizeList.RESPONSE_DETECTED_LABELS_FROM_IMAGE_CHECKER, arrayList)
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