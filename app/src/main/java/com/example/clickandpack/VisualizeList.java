package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemWithStatus;
import database_handler.ListEntity;

public class VisualizeList extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {
    private AppDatabase appDatabase;
    private ListEntity listEntity = null;
    private List<ItemWithStatus> itemsWithStatus = null ;
    private String response = "";
    public static final String RESPONSE_KEY_FROM_IMAGE_CHECKER = "response-checker";

    private int REQUEST_CODE_CHECK_LIST_WITH_IMAGES = 4;

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
    }

    private void backToHome(){
        if (!response.equals("")) {
            Intent i = new Intent();
            i.putExtra(MainActivity.RESPONSE_KEY, response);
            setResult(RESULT_OK, i);
        }

        finish();
    }

    private void checkListWithImages(){
        // TODO check lista items non vuota + check almeno 1 item e' detectable + permessi fotocamera
        Intent i = new Intent(getApplicationContext(), CheckListWithImages.class);
        startActivityForResult(i,REQUEST_CODE_CHECK_LIST_WITH_IMAGES);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("TAG","ora sono qui. data is null: ");
        Log.d("TAG", data == null ? "si" : "no");
        //Log.d("TAG","ora sono qui. data.getExtras() is null: " + data.getExtras() == null ? "si" : "no");
        if ( data != null && data.getExtras() != null) {
            String textToShow = data.getExtras().getString(RESPONSE_KEY_FROM_IMAGE_CHECKER);

            if (!textToShow.equals(""))
                Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();

        }
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