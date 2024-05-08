package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ListEntity;

public class VisualizeList extends AppCompatActivity {
    private AppDatabase appDatabase;
    private ListEntity listEntity = null;
    private List<ItemEntity> checkedItems = null;
    private List<ItemEntity> notCheckedItems = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visualize_list);

        Intent i = getIntent();
        String idList = i.getStringExtra(MainActivity.ID_LIST);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                readDatabase(Long.parseLong(idList));
            }
        });
        t.start();

        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Execute setInitialGUI() on the main thread
        runOnUiThread(() ->  setInitialGUI());

        findViewById(R.id.button_checkListWithImages).setOnClickListener(view -> checkListWithImages());


    }

    private void initializeAppDatabase(){
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();
    }


    private void readDatabase(long idList) {
        initializeAppDatabase();
        listEntity = appDatabase.listDao().getListFromId(idList);
        listEntity.itemsInList = appDatabase.itemsInListDao().getAllItemsInList(idList);
        checkedItems = appDatabase.itemsInListDao().getItemsInListWithGivenChecked(idList, 1);
        notCheckedItems = appDatabase.itemsInListDao().getItemsInListWithGivenChecked(idList, 0);
    }

    private void checkListWithImages(){


    }


    private void setInitialGUI(){
        if (listEntity == null){
            // TODO forse torna alla home
            return;
        }

        ((TextView) findViewById(R.id.textView_listName)).setText(listEntity.getName());
        ((TextView) findViewById(R.id.textView_listDescription)).setText(listEntity.getDescription());

        LinearLayout linearLayoutListItems = findViewById(R.id.linearLayout_listItems);

//        for (int i = 0; i < 20 ; i++) {

            for (ItemEntity ie : listEntity.itemsInList) {
                CheckBox checkBox = new MyCheckBox(this);

                // Set CheckBox properties
                checkBox.setText(ie.getName());
                checkBox.setTag(ie.getId());
                checkBox.setChecked(false); // TODO

                linearLayoutListItems.addView(checkBox);
            }

//        }



    }

}