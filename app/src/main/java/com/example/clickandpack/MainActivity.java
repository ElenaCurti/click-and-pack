package com.example.clickandpack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.app.LauncherActivity;
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
import java.util.Random;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemsInList;
import database_handler.ListEntity;
import database_handler.ListDAO;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_LIST = 1;
    private static final String TAG_LOGGER = "MyTag_MainActivity";

    public static final String OPERATION_NAME = "action";
    public static final String OPERATION_ADD_LIST = "add_list";
    public static final String OPERATION_MODIFY_LIST = "modify_list";

    private AppDatabase appDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //populateListsCollection();

        FloatingActionButton buttonAddList = (FloatingActionButton) findViewById(R.id.floatingActionButton_aggiungi);

        buttonAddList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* User wants to add a new list */
                Intent i = new Intent(getApplicationContext(), AddOrModifyList.class);
                i.putExtra(OPERATION_NAME, OPERATION_ADD_LIST);
                startActivityForResult(i,REQUEST_CODE_ADD_LIST);
            }
        });


        //  Cannot access database on the main thread since it may potentially lock the UI for a long period of time.
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                creazioneEdUsoContentPersonalizzato(true);
            }
        });

        t.start();



    }

    private void creazioneEdUsoContentPersonalizzato(boolean resetAllaFine){
        appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app-database").build();

        appDatabase.listDao().deleteAllLists();
        appDatabase.itemDao().deleteAllItems();
        appDatabase.itemsInListDao().deleteAllItemsInLists();

        // Create items
        ItemEntity item1 = new ItemEntity("item 1", false);
        ItemEntity item2 = new ItemEntity("item 2", true);
        ItemEntity item3 = new ItemEntity("item 3", false);

        // Insert items into the database
        item1.id = appDatabase.itemDao().insertItem(item1);
        item2.id = appDatabase.itemDao().insertItem(item2);
        item3.id = appDatabase.itemDao().insertItem(item3);

        // Create list 1 with items item 1 and item 2
        ListEntity list1 = new ListEntity("list 1", "This is the description for list 1");
        list1.id  = appDatabase.listDao().insertList(list1);

        Log.d(TAG_LOGGER + "", "list1 id: " + list1.getId()) ;
        Log.d(TAG_LOGGER + "", "item1 id: " + item1.getId()) ;

        ItemsInList itemsInList1 = new ItemsInList();
        itemsInList1.listId = list1.getId();
        itemsInList1.itemId = item1.getId();
        itemsInList1.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList1);

        ItemsInList itemsInList2 = new ItemsInList();
        itemsInList2.listId = list1.getId();
        itemsInList2.itemId = item2.getId();
        itemsInList2.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList2);

        // Create list 2 with items item 2 and item 3
        ListEntity list2 = new ListEntity("list 2", "This is the description for list 2");
        list2.id = appDatabase.listDao().insertList(list2);

        ItemsInList itemsInList3 = new ItemsInList();
        itemsInList3.listId = list2.getId();
        itemsInList3.itemId = item2.getId();
        itemsInList3.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList3);

        ItemsInList itemsInList4 = new ItemsInList();
        itemsInList4.listId = list2.getId();
        itemsInList4.itemId = item3.getId();
        itemsInList4.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList4);

        populateListsCollection();

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_LIST) {
            // User has comed back from the "add list" view. I check if the result is ok or not.
            if (resultCode == RESULT_OK) {
                String risposta = data.getExtras().getString("CodRisposta");
                Toast.makeText(this, "Risposta: " + risposta, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, getString(R.string.error_creating_new_list), Toast.LENGTH_LONG).show();
            }
        }
    }

    private void populateListsCollection() {

        List<ListEntity> userLists = appDatabase.listDao().getAllLists();
        Log.d(MainActivity.TAG_LOGGER+"_nl_", "Numero liste: " + userLists.size());

        /*for (ListEntity lista: userLists) {
            id.
            mainItems.add(lista.getName());
            subItems.add(lista.getDescription());
        }*/

        // Sample data for main and sub items
        /*mainItems.add("Sea list");
        mainItems.add("Ski list");

        subItems.add("List for sea items");
        subItems.add("List for skii vacation");*/

        // Temporary list of strings of fixed size passed to the ArrayAdapter
        List<String> fixedSizeList = new ArrayList<>(Collections.nCopies(userLists.size(), ""));

        // TODO se #liste ==0 aggiungi textview del tipo "schiaccia + per aggiungere lista"
        /* Create an adapter and assign it to the ListView */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.elenco_liste_prima_videata, fixedSizeList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;

                if (view == null) {
                    Log.d(TAG_LOGGER, "view at position " + position + " is null");
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.elenco_liste_prima_videata, parent, false);
                }
                Log.d(TAG_LOGGER, "view at position " + position + " is NOT null");

                TextView mainItemTextView = view.findViewById(R.id.textView_main_item);
                TextView subItemTextView = view.findViewById(R.id.textView_sub_item);

                ListEntity listEntity = userLists.get(position);
                mainItemTextView.setText(listEntity.getName());
                subItemTextView.setText(listEntity.getDescription());
                long id = listEntity.getId();

                FloatingActionButton buttonVisualizza = (FloatingActionButton) view.findViewById(R.id.floatingActionButton_visualizza);
                FloatingActionButton buttonModifica = (FloatingActionButton) view.findViewById(R.id.floatingActionButton_modifica);

                View.OnClickListener buttonsClickListener = new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FloatingActionButton pressedButton = (FloatingActionButton) view;
                        String tipoButton = "";
                        if ( pressedButton == buttonVisualizza )
                            tipoButton = "visualizza";
                        else if ( pressedButton == buttonModifica ) {
                            tipoButton = "modifica";
                            Intent i = new Intent(getApplicationContext(), AddOrModifyList.class);
                            i.putExtra(OPERATION_NAME, OPERATION_MODIFY_LIST + " " + id);
                            startActivityForResult(i,REQUEST_CODE_ADD_LIST);
                        }
                        Toast.makeText(getApplicationContext(), "Id: " + id + "  Tipo: " + tipoButton , Toast.LENGTH_SHORT).show();
                    }
                };

                buttonVisualizza.setOnClickListener(buttonsClickListener);
                buttonModifica.setOnClickListener(buttonsClickListener);

                return view;

            }
        };

        ListView listView = findViewById(R.id.listView_liste);
        listView.setAdapter(adapter);
    }

}