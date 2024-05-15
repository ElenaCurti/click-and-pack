package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;
import static database_handler.MyDatabaseInitiator.populateDB;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemsInList;
import database_handler.ListEntity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_LOGGER = "MyTag_MainActivity";

    // Intent request code
    private static final int  REQUEST_CODE_ADD_LIST = 1, REQUEST_CODE_MODIFY_LIST = 2, REQUEST_CODE_VISUALIZE_LIST = 3;
    // Intent keys and values
    public static final String OPERATION_NAME = "action";
    public static final String OPERATION_ADD_LIST = "add_list";
    public static final String OPERATION_MODIFY_LIST = "modify_list";

    public static final String ID_LIST = "id_list";

    public static final String RESPONSE_KEY = "response";

    // Lists adapter
    private ArrayAdapter<String> adapterListe;
    private List<ListEntity> userLists;


    // Database
    private AppDatabase appDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // "Add new list" button click listener
        findViewById(R.id.floatingActionButton_aggiungi).setOnClickListener( v -> {
            Intent i = new Intent(getApplicationContext(), AddOrModifyList.class);
            i.putExtra(OPERATION_NAME, OPERATION_ADD_LIST);
            startActivityForResult(i,REQUEST_CODE_ADD_LIST);
        });

        readAndShowUserLists();
    }

    private void readAndShowUserLists(){
        //  Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
        Thread t = new Thread(() -> {
            initializeAppDatabase();
            //populateDB(this, appDatabase); // TODO da rimuovere
            userLists = appDatabase.listDao().getAllLists();
        });
        t.start();

        // Wait for the background thread to finish
        try {
            t.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Execute setInitialGUI() on the main thread
        runOnUiThread(() ->  setInitialGUI());

    }

    private void initializeAppDatabase(){
        if (appDatabase  == null )
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();
    }




    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String textToShow = "";
        if (resultCode == RESULT_OK) {
            readAndShowUserLists();
            textToShow = data.getExtras().getString(RESPONSE_KEY);
        } else {
            switch(requestCode){
                case REQUEST_CODE_ADD_LIST:
                    textToShow = getString(R.string.error_creating_new_list);
                    break;
                case REQUEST_CODE_MODIFY_LIST:
                    textToShow = getString(R.string.error_modifying_list);
                    break;
                case REQUEST_CODE_VISUALIZE_LIST:
                    break;
                default:
                    textToShow = getString(R.string.general_error);
            }
        }
        if (!textToShow.equals(""))
            Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();
    }

    private void setInitialGUI() {
        if (userLists == null || userLists.size() == 0) {
            findViewById(R.id.textView_noList).setVisibility(View.VISIBLE);
        } else
            findViewById(R.id.textView_noList).setVisibility(View.GONE);

        // Temporary list of strings of fixed size passed to the ArrayAdapter
        List<String> fixedSizeList = new ArrayList<>(Collections.nCopies(userLists.size(), ""));

        // Adapter for the list
        adapterListe = new ArrayAdapter<String>(this, R.layout.elenco_liste_prima_videata, fixedSizeList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;

                // Create view if null
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.elenco_liste_prima_videata, parent, false);
                }

                // Set list name and description as main-item and sub-item
                ListEntity listEntity = userLists.get(position);
                long id = listEntity.getId();

                TextView mainItemTextView = view.findViewById(R.id.textView_main_item);
                mainItemTextView.setText(listEntity.getName());

                TextView subItemTextView = view.findViewById(R.id.textView_sub_item);
                subItemTextView.setText(listEntity.getDescription());

                // Click listener of "visualize list" and "modify list"
                FloatingActionButton buttonVisualizza = view.findViewById(R.id.floatingActionButton_visualizza);
                buttonVisualizza.setTag(id);
                buttonVisualizza.setOnClickListener(v -> {
                    // TODO
                    Intent i = new Intent(getApplicationContext(), VisualizeList.class);
                    i.putExtra(ID_LIST,  "" + v.getTag());
                    startActivityForResult(i,REQUEST_CODE_VISUALIZE_LIST);
                    //Toast.makeText(getApplicationContext(), "Id: " + v.getTag() + "  Tipo: visualizza" , Toast.LENGTH_SHORT).show();
                });

                FloatingActionButton buttonModifica = view.findViewById(R.id.floatingActionButton_modifica);
                buttonModifica.setTag(id);

                buttonModifica.setOnClickListener (v -> {
                    Intent i = new Intent(getApplicationContext(), AddOrModifyList.class);
                    // TODO metti un altro extra
                    i.putExtra(OPERATION_NAME, OPERATION_MODIFY_LIST + " " + v.getTag());
                    startActivityForResult(i,REQUEST_CODE_MODIFY_LIST);
                });

                return view;

            }
        };

        ListView listView = findViewById(R.id.listView_liste);
        listView.setAdapter(adapterListe);
    }







}