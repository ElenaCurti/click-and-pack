package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;
import static database_handler.MyDatabaseInitiator.resetAndPopulateDB;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
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
import database_handler.ListEntity;

/**
 * Main activity shown at the beginning of the app. It will show all the users' lists. User
 * will then be able to choose to add, visualize or modify a list.
 */
public class MainActivity extends AppCompatActivity {
    /* Intent request code */
    private static final int  REQUEST_CODE_ADD_LIST = 1, REQUEST_CODE_MODIFY_LIST = 2, REQUEST_CODE_VISUALIZE_LIST = 3;

    /* Intent keys and values */
    public static final String OPERATION_NAME = "action";
    public static final String OPERATION_ADD_LIST = "add_list";
    public static final String OPERATION_MODIFY_LIST = "modify_list";
    public static final String KEY_ID_LIST = "id_list";
    public static final String RESPONSE_KEY = "response";

    /* List of the user's lists */
    private List<ListEntity> userLists;

    /* Database "handler" */
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

    /**
     * Method that reads database with user's lists and shows them
     */
    private void readAndShowUserLists(){
        //  Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
        Thread t = new Thread(() -> {
            initializeAppDatabase();
            resetAndPopulateDB(this, appDatabase);
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

    /**
     * Method that initializes the database "handler", if not already done
     */
    private void initializeAppDatabase(){
        if (appDatabase  == null )
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();
    }

    /**
     * When activity ends, I check the result code.
     * If result code is ok, I re-load the lists (because they might have been changed).
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode The integer result code returned by the child activity
     *                   through its setResult().
     * @param data An Intent, which can return result data to the caller
     *               (various data can be attached to Intent "extras").
     *
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String textToShow = "";
        if (resultCode == RESULT_OK) {
            readAndShowUserLists();
            if (data.getExtras() != null)
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
        if (textToShow!= null && !textToShow.equals(""))
            Toast.makeText(this, textToShow, Toast.LENGTH_LONG).show();
    }

    /**
     * Method that handles the graphical part: shows the lists with a "visualize" and "modify" button.
     */
    private void setInitialGUI() {
        if (userLists == null || userLists.size() == 0) {
            findViewById(R.id.textView_noList).setVisibility(View.VISIBLE);
        } else
            findViewById(R.id.textView_noList).setVisibility(View.GONE);

        // Temporary list of strings of fixed size passed to the ArrayAdapter
        List<String> fixedSizeList = new ArrayList<>(Collections.nCopies(userLists.size(), ""));

        // Adapter for the list
        // Create view if null
        // Set list name and description as main-item and sub-item
        // Click listener of "visualize list" and "modify list"
        // Lists adapter
        ArrayAdapter<String> adapterListsOfUser = new ArrayAdapter<String>(this, R.layout.elenco_liste_prima_videata, fixedSizeList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;

                // Create view if null
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.elenco_liste_prima_videata, parent, false);
                }

                // Visualize click listener
                View.OnClickListener visualizeList = new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent i = new Intent(getApplicationContext(), VisualizeList.class);
                        i.putExtra(KEY_ID_LIST, "" + v.getTag());
                        startActivityForResult(i, REQUEST_CODE_VISUALIZE_LIST);
                    }
                } ;

                // Set list name and description as main-item and sub-item
                ListEntity listEntity = userLists.get(position);
                long id = listEntity.getId();
                TextView mainItemTextView = view.findViewById(R.id.textView_main_item);
                mainItemTextView.setText(listEntity.getName());
                mainItemTextView.setTag(id);
                mainItemTextView.setOnClickListener(visualizeList);

                TextView subItemTextView = view.findViewById(R.id.textView_sub_item);
                subItemTextView.setText(listEntity.getDescription());
                subItemTextView.setTag(id);
                subItemTextView.setOnClickListener(visualizeList);

                // Set visualize button
                FloatingActionButton buttonVisualize = view.findViewById(R.id.floatingActionButton_visualize);
                buttonVisualize.setTag(id);
                buttonVisualize.setOnClickListener(visualizeList);

                // Click listener of "modify list"
                FloatingActionButton buttonModify = view.findViewById(R.id.floatingActionButton_modify);
                buttonModify.setTag(id);

                buttonModify.setOnClickListener(v -> {
                    Intent i = new Intent(getApplicationContext(), AddOrModifyList.class);
                    i.putExtra(OPERATION_NAME, OPERATION_MODIFY_LIST);
                    i.putExtra(KEY_ID_LIST, Long.parseLong("" + v.getTag()));
                    startActivityForResult(i, REQUEST_CODE_MODIFY_LIST);
                });

                return view;

            }
        };

        ListView listView = findViewById(R.id.listView_liste);
        listView.setAdapter(adapterListsOfUser);
    }







}