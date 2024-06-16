package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;
import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemWithStatus;
import database_handler.ListEntity;
import object_detector.MyObjectDetectorStillImages;

public class VisualizeList extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    // Database handler
    private AppDatabase appDatabase;

    // Current list which is visualized
    private ListEntity listEntity = null;

    // Items in the list and their "status" (checked or not-checked)
    private List<ItemWithStatus> itemsWithStatus = null ;

    // List of items (recognised with images) that were packed
    private List<ItemEntity> itemsPackedWithImages;

    // Number of items that are in the list and are detectable by images
    private int numberOfItemsInListDetectableByImages ;

    // Response to send to MainActivity
    private String response = "";

    // Key for message received from the camera object recognition
    public static final String RESPONSE_ERROR_KEY_FROM_CAMERA_CHECKER = "response-checker";

    // Key for ids of detected items by camera
    public static final String RESPONSE_DETECTED_INDEXES_FROM_IMAGE_CHECKER = "detected-indexes";

    private static final int REQUEST_CODE_CHECK_LIST_WITH_CAMERA = 4;
    private static final int REQUEST_CODE_PICK_IMAGES = 1;
    private static final int STORAGE_PERMISSION_CODE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_list);

        // Get list id from MainActivity and read database info about list
        Intent i = getIntent();
        Long idList = Long.parseLong(i.getStringExtra(MainActivity.KEY_ID_LIST));
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                readDatabase(idList);
            }
        });

        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Execute setInitialGUI() on the main thread, otherwise an exception
        // will be thrown when trying to modify the view
        runOnUiThread( () -> setInitialGUI() );

        findViewById(R.id.button_checkListWithCamera).setOnClickListener(view -> {
            if (checkIfObjectDetectionIsPossible()) {
                Intent intentCheckListWithCamera = new Intent(getApplicationContext(), CheckListWithCamera.class);
                startActivityForResult(intentCheckListWithCamera, REQUEST_CODE_CHECK_LIST_WITH_CAMERA);
            }
        });
        findViewById(R.id.button_checkListWithImages).setOnClickListener(view -> askUserToChoseImageForObjectDetection());
        findViewById(R.id.floatingActionButton_backToHome).setOnClickListener(view -> backToHome());

    }

    private void initializeAppDatabase(){
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();
    }

    private void readDatabase(long idList) {
        initializeAppDatabase();
        listEntity = appDatabase.listDao().getListFromId(idList);
        itemsWithStatus = appDatabase.itemsInListDao().getItemsWithStatus(idList);
        numberOfItemsInListDetectableByImages = appDatabase.itemsInListDao().countItemsInListDetectableByImages(idList);
    }

    private void backToHome(){
        if (!response.equals("")) {
            Intent i = new Intent();
            i.putExtra(MainActivity.RESPONSE_KEY, response);
            setResult(RESULT_OK, i);
        }

        finish();
    }

    private boolean checkIfObjectDetectionIsPossible(){
        if (itemsWithStatus == null || itemsWithStatus.size() == 0 || numberOfItemsInListDetectableByImages < 1){
            Toast.makeText(this, getString(R.string.necessary_at_least_one_detectable_item), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void askUserToChoseImageForObjectDetection(){
        if (checkIfObjectDetectionIsPossible()) {
            // Possibly ask user permission
            if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&*/
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, i ask it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
            } else {
                // Permission is already granted, I ask user to choose photo
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_images)), REQUEST_CODE_PICK_IMAGES);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Storage permissions handling and possibly ask user to chose images for object detection
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Proceed with your logic here
                askUserToChoseImageForObjectDetection();
            } else {
                // Permission is denied
                Toast.makeText(this, getString(R.string.storage_permission_denied_error), Toast.LENGTH_LONG).show();
            }
        }
    }

    // Check which activity ended:
    //  - If picking images ended, I will start object detection in those images
    //  - Else, if camera object recognition ended, I will display detected objects
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("img", "Request code:"  + REQUEST_CODE_PICK_IMAGES + "  Result code:" + resultCode);
        // User has selected an image to process
        if (requestCode == REQUEST_CODE_PICK_IMAGES) {
            if (resultCode != RESULT_OK || data == null) {
                Toast.makeText(this, getString(R.string.no_image_selected), Toast.LENGTH_LONG).show();
                return;
            }
            CopyOnWriteArrayList<Uri> urisOfImagesChosenByUser = new CopyOnWriteArrayList<>();

            if (data.getClipData() != null) {
                // Multiple images selected
                int count = data.getClipData().getItemCount();
                for (int i = 0; i < count; i++) {
                    Uri imageUri = data.getClipData().getItemAt(i).getUri();
                    urisOfImagesChosenByUser.add(imageUri);
                }
            } else if (data.getData() != null) {
                // Single image selected
                Uri imageUri = data.getData();
                urisOfImagesChosenByUser.add(imageUri);
            }
            String tmp1 = data.getClipData() == null ? "null" : "non null";
            String tmp2 = data.getData() == null ? "null" : "non null";
            Log.d("img", "data.getClipData() == null:"  + tmp1 + "  data.getData() == null:" + tmp2);
            // Start object detection for all images
            MyObjectDetectorStillImages objectDetectorStillImages = new MyObjectDetectorStillImages( idsFound -> {
                // Call-back with results
                showObjectRecognitionResultAndAskConfirm(idsFound);
                return null;
            });
            objectDetectorStillImages.processImages(this, urisOfImagesChosenByUser);

            return;
        }

        // Object detection with camera ended
        if ( requestCode == REQUEST_CODE_CHECK_LIST_WITH_CAMERA &&  data != null && data.getExtras() != null) {
            String textToShow = data.getExtras().getString(RESPONSE_ERROR_KEY_FROM_CAMERA_CHECKER);

            if (!textToShow.equals("")) {
                Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();
                return;
            }
            // Retrieve ids of detected objects
            ArrayList<Integer> detectedItemsIndexes = data.getExtras().getIntegerArrayList(RESPONSE_DETECTED_INDEXES_FROM_IMAGE_CHECKER);
            if (detectedItemsIndexes == null ) {
                Toast.makeText(this, getString(R.string.no_item_packed_from_images) , Toast.LENGTH_LONG).show();
            } else {
                List<Long> longList = new ArrayList<>();
                for (Integer integer : detectedItemsIndexes) {
                    longList.add(integer.longValue());
                }
                showObjectRecognitionResultAndAskConfirm(longList);
            }
        }
    }

    private void showObjectRecognitionResultAndAskConfirm(List<Long> itemsIds){
        initializeAppDatabase();

        if (itemsIds == null || itemsIds.size() == 0 ){
            Toast.makeText(this, getString(R.string.no_item_packed_from_images), Toast.LENGTH_LONG).show();
            return;
        }

        if (itemsIds.contains(-1L)) {
            Toast.makeText(this, getString(R.string.error_in_processing_object_detector), Toast.LENGTH_LONG).show();
            if (itemsIds.size() == 1)
                return;
        }


        // Retrieve names of detected objects
        Thread t = new Thread(() -> {
            itemsPackedWithImages = appDatabase.itemDao().getItemsFromIds(itemsIds);
        });
        t.start();

        String stringToShow = "";
        List<Long> commonItemIds = new ArrayList<>();

        try {
            t.join();

            String commonItems = "", notCommonItems = "";

            // I check which clicked items are in user list and which are not
            for (int i = 0; i < itemsPackedWithImages.size(); i++) {
                ItemEntity itemCheckedWithImages = itemsPackedWithImages.get(i);
                boolean found = false;
                for (ItemWithStatus itemInUserList : itemsWithStatus) {
                    if (itemInUserList.item.id == itemCheckedWithImages.id) {
                        commonItemIds.add(itemCheckedWithImages.id);
                        found = true;
                        break;
                    }
                }
                if (found)
                    commonItems += "\n\t- " + itemCheckedWithImages.getName();
                else
                    notCommonItems += "\n\t- " + itemCheckedWithImages.getName();
            }

            commonItems = commonItems.equals("") ? "" : getString(R.string.items_set_as_packed) + commonItems + ".\n";
            notCommonItems = notCommonItems.equals("") ? "" : "\n" + getString(R.string.items_found_but_ignored) + notCommonItems;

            stringToShow = commonItems + notCommonItems;

        } catch (InterruptedException e) {
            Toast.makeText(this,  getString(R.string.error_packed_items), Toast.LENGTH_LONG).show();
            return;
        }

        // Ask confirmation to user before setting items as "packed"
        DialogInterface.OnClickListener alertClickListener = (dialog, which) -> {
            if (which == -1) {
                // If positive button was pressed, i save changes, re-load them and show them in GUI
                Thread t2 = new Thread(() -> {
                    for(Long idItem : commonItemIds) {
                        appDatabase.itemsInListDao().updateItemCheckedUsingListAndItemId(listEntity.id, idItem,true);
                    }
                    readDatabase(listEntity.id) ;
                });
                t2.start();

                try {
                    t2.join();
                } catch (InterruptedException e) {
                    response = getString(R.string.error_saving_re_loading);
                    backToHome();
                }

                runOnUiThread( () -> setInitialGUI() );
            }
            dialog.dismiss();
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.packed_items_with_images_confirmation));
        builder.setMessage(stringToShow);
        builder.setPositiveButton(android.R.string.ok, alertClickListener);
        builder.setNegativeButton(android.R.string.cancel, alertClickListener);
        builder.create().show();

        itemsPackedWithImages = null;
    }


    private void setInitialGUI(){

        if (listEntity == null){
            // Should never happen
            response = getString(R.string.error_with_existing_list);
            backToHome();
            return;
        }
        // Set name and description
        ((TextView) findViewById(R.id.textView_listName)).setText(listEntity.getName());
        ((TextView) findViewById(R.id.textView_listDescription)).setText(listEntity.getDescription());

        LinearLayout linearLayoutListItems = findViewById(R.id.linearLayout_listItems);
        linearLayoutListItems.removeAllViews();

        // If in the list there are no items, I will just display a textview
        if (itemsWithStatus == null || itemsWithStatus.size() == 0){
            findViewById(R.id.textView_noItems).setVisibility(View.VISIBLE);
            return;
        }

        findViewById(R.id.textView_noItems).setVisibility(View.GONE);

        // I add every item in the list in the LinearView as a checkbox (with their current status)
        for (int i = 0; i < itemsWithStatus.size(); i++) {
            ItemWithStatus item_status = itemsWithStatus.get(i);

            CheckBox checkBox = new CheckBox(this);
            checkBox.setTextSize(20);
            checkBox.setPadding(0, 15, 0, 15);
            checkBox.setText(item_status.item.getName());
            checkBox.setTag(item_status.items_in_list_id);
            checkBox.setChecked(item_status.isChecked);
            checkBox.setOnCheckedChangeListener(this);

            linearLayoutListItems.addView(checkBox);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Every time a checkbox is clicked, I update the database
        long idItemInList = Long.parseLong("" + buttonView.getTag());
        new Thread(() -> appDatabase.itemsInListDao().updateItemChecked(idItemInList, isChecked ))
                .start();
    }
}