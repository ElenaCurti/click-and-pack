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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemWithStatus;
import database_handler.ListEntity;
import object_detector.MyObjectDetectorStillImages;

public class VisualizeList extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private AppDatabase appDatabase;
    private ListEntity listEntity = null;
    private List<ItemWithStatus> itemsWithStatus = null ;
    private String response = "";
    public static final String RESPONSE_KEY_FROM_IMAGE_CHECKER = "response-checker";
    public static final String RESPONSE_DETECTED_INDEXES_FROM_IMAGE_CHECKER = "detected-indexes";
    private int REQUEST_CODE_CHECK_LIST_WITH_CAMERA = 4;

    private List<ItemEntity> itemsPackedWithImages;
    private int numberOfItemsInListDetectableByImages ;
    private static final int STORAGE_PERMISSION_CODE = 1;
    private static final int PICK_IMAGE_REQUEST_SINGLE = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_list);

        // Get list id from mainactivity and read database info about list
        Intent i = getIntent();
        Long idList = Long.parseLong(i.getStringExtra(MainActivity.ID_LIST));
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

        findViewById(R.id.button_checkListWithCamera).setOnClickListener(view -> checkListWithCamera());
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
            // TODO metti string
            Toast.makeText(this, "To check list with images or camera it's necessary to have at least one detectable item!", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void checkListWithCamera() {
        if (checkIfObjectDetectionIsPossible()) {
            Intent i = new Intent(getApplicationContext(), CheckListWithCamera.class);
            startActivityForResult(i, REQUEST_CODE_CHECK_LIST_WITH_CAMERA);
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Proceed with your logic here
                askUserToChoseImageForObjectDetection(); // TODO altro ?
            } else {
                // Permission is denied
                // TODO metti string
                Toast.makeText(this, "Permission is necessary to read images!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void checkListWithChosenImage(Uri imageUri){
        InputImage image;
        try {
            image = InputImage.fromFilePath(this, imageUri);
        } catch (IOException e) {
            // TODO handle
            return;
        }

        MyObjectDetectorStillImages objectDetectorStillImages = new MyObjectDetectorStillImages( mapResult -> {
            // Found result
            // TODO
            Log.d("images", "sono qui");
            List<Long> idsFound = new ArrayList<>(mapResult.keySet());
            showObjectRecognitionResultAndAskConfirm(idsFound);

            /*AlertDialog.Builder builder = new AlertDialog.Builder(this);
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_layout, null);
            LinearLayout ll = dialogView.findViewById(R.id.linearLayout_dialogView);
            for (Map.Entry<Long, String> entry : mapResult.entrySet()) {
                Long idItem = entry.getKey();
                String nameItem = entry.getValue();

                MyCheckBox c = new MyCheckBox(this);
                c.setText(nameItem);
                c.setTag(idItem);

                ll.addView(c);
            }
            // You can set the checkbox state or listen to its events here
            builder.setView(dialogView)
                    .setTitle("Found items")    // todo string
                    .setMessage("This objects were found and will be set as ")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle OK button click

                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Handle Cancel button click
                            dialog.dismiss();
                        }
                    });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
*/
            return null;
        });

        objectDetectorStillImages.processImage(image);

    }

    private void askUserToChoseImageForObjectDetection(){
        if (checkIfObjectDetectionIsPossible()) {
            // Possibly ask user permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted, i ask it
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_CODE);
                return;
            } else {
                // Permission is already granted, I ask user to choose photo
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                // TODO select picture in string
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST_SINGLE);


            }

        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // User has selected an image to process
        if (requestCode == PICK_IMAGE_REQUEST_SINGLE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            checkListWithChosenImage(selectedImageUri);
            return;
        }

        // Object detection with camera ended
        if ( requestCode == REQUEST_CODE_CHECK_LIST_WITH_CAMERA &&  data != null && data.getExtras() != null) {
            String textToShow = data.getExtras().getString(RESPONSE_KEY_FROM_IMAGE_CHECKER);

            if (!textToShow.equals(""))
                Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();

            // Retrieve ids of detected objects
            ArrayList<Integer> detectedItemsIndexes = data.getExtras().getIntegerArrayList(RESPONSE_DETECTED_INDEXES_FROM_IMAGE_CHECKER);
            if (detectedItemsIndexes == null ) {
                // TODO "No items packed";   // TODO metti string
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

        if (itemsIds.size() == 0 ){
            // todo set string e scrivi una cosa generica sia per check camera sia per check images
            Toast.makeText(this, "No item is set as packed", Toast.LENGTH_LONG).show();
            return;
        }

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

            commonItems = commonItems.equals("") ? "" : "These items will be setted as packed: " + commonItems + ".\n";
            notCommonItems = notCommonItems.equals("") ? "" : "\nThe following items where found but aren't in the list," +
                    " so they will be ignored: " + notCommonItems;

            stringToShow = commonItems + notCommonItems;

        } catch (InterruptedException e) {
            // TODO string
            Toast.makeText(this,  "Error during the retain of packed items with images", Toast.LENGTH_LONG).show();
            return;
        }

        // Ask confirmation to user
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
                    // TODO set string e eventualmente torna alla home ?
                    Toast.makeText(this, "Error during saving or re-loadding of user list!", Toast.LENGTH_LONG).show();
                    return;
                }

                runOnUiThread( () -> setInitialGUI() );
            }
            dialog.dismiss(); // Dismiss the dialog
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Dialog Title"); // TODO setta string
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

        ((TextView) findViewById(R.id.textView_listName)).setText(listEntity.getName());
        ((TextView) findViewById(R.id.textView_listDescription)).setText(listEntity.getDescription());

        LinearLayout linearLayoutListItems = findViewById(R.id.linearLayout_listItems);
        linearLayoutListItems.removeAllViews();

        if (itemsWithStatus == null || itemsWithStatus.size() == 0){
            findViewById(R.id.textView_noItems).setVisibility(View.VISIBLE);
            return;
        }

        findViewById(R.id.textView_noItems).setVisibility(View.GONE);


        for (int i = 0; i < itemsWithStatus.size(); i++) {
            ItemWithStatus item_status = itemsWithStatus.get(i);

            CheckBox checkBox = new MyCheckBox(this);
            checkBox.setText(item_status.item.getName());
            checkBox.setTag(item_status.items_in_list_id);
            checkBox.setChecked(item_status.isChecked);
            checkBox.setOnCheckedChangeListener(this);

            linearLayoutListItems.addView(checkBox);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        long idItemInList = Long.parseLong("" + buttonView.getTag());
        //Toast.makeText(this, "view:" + buttonView.getText() + " " + buttonView.getTag(), Toast.LENGTH_LONG ).show();
        new Thread(() -> appDatabase.itemsInListDao().updateItemChecked(idItemInList, isChecked ))
                .start();
    }
}