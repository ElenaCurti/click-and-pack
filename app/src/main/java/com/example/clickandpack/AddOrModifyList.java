package com.example.clickandpack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemsInList;
import database_handler.ListEntity;


public class AddOrModifyList extends AppCompatActivity {
    private static final String TAG_LOGGER = "MyTag_AddOrModifyList";
    private String[] itemsNames = {"Select item...", "Elemento 1", "Elemento 2", "Elemento 3", "Elemento 4", "Other"};

    private ListEntity listEntity = null ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_modify_list);

        Intent i = getIntent();
        String operation = i.getStringExtra(MainActivity.OPERATION_NAME);


        //setViewItems(operation);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                getListInDb(operation);
            }
        });

        t.start();

        findViewById(R.id.button_deleteList).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_list_title))
                    .setMessage(getString(R.string.delete_list_message))
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int whichButton) {
                            // TODO User really wants to delete list
                            Toast.makeText(getApplicationContext(), "User really wants to delete list", Toast.LENGTH_SHORT).show();
                            finish();
                        }})
                    .setNegativeButton(android.R.string.cancel, null).show();

        });

    }

    private void getListInDb(String operation){
        if (operation.startsWith(MainActivity.OPERATION_MODIFY_LIST)) {
            AppDatabase appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "app-database").build();
            long idList = Long.parseLong( ( (String[]) operation.split(" ") )[1]);
            listEntity = appDatabase.listDao().getListFromId(idList);
            listEntity.itemsInList = appDatabase.itemsInListDao().getItemsInList(idList);
            Log.d(TAG_LOGGER + "_db", "Size get: " + appDatabase.itemsInListDao().getItemsInList(idList).size());

            for (ItemsInList il: appDatabase.itemsInListDao().getAllItemsInLists() ) {
                Log.d(TAG_LOGGER + "_db", "Item in list: " + il.toString());

            }
        }

        setViewItems(operation);
    }


    private void setViewItems(String operation){
        // Sample list of items
        List<String> itemList = new ArrayList<>();
        if (operation.equals(MainActivity.OPERATION_ADD_LIST)) {
            findViewById(R.id.button_deleteList).setVisibility(View.GONE);

        } else {
            // TODO prendi numero lista e setta tutti i campi
            ((EditText) findViewById(R.id.editText_listName)).setText(listEntity.getName());
            ((EditText) findViewById(R.id.editText_listDescription)).setText(listEntity.getDescription());

            if ( listEntity.itemsInList != null ) {
                Log.d(TAG_LOGGER + "_db", "Size: " + listEntity.itemsInList.size());
                for (ItemEntity ie : listEntity.itemsInList) {
                    itemList.add(ie.getName());
                    Log.d(TAG_LOGGER + "_db", ie.getName());
                }
            }
//            itemList = listEntity.itemsInList
            /*itemList.add("Swimsuit");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");
            itemList.add("Flip-flops");*/
        }

        if (itemList.isEmpty()) {
            findViewById(R.id.textView_emptyList).setVisibility(View.VISIBLE);
        }

        EditText editTextSearch = findViewById(R.id.editTextText_newItemName);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_lista_da_rimuovere, itemList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;

                if (view == null) {
                    Log.d(TAG_LOGGER, "view at position " + position + " is null");
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.item_lista_da_rimuovere, parent, false);
                }
                Log.d(TAG_LOGGER, "view at position " + position + " is NOT null");

                TextView itemName = view.findViewById(R.id.textView_itemName);

                itemName.setText(itemList.get(position));

                FloatingActionButton buttonRemove = (FloatingActionButton) view.findViewById(R.id.floatingActionButton_removeItem);


                buttonRemove.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FloatingActionButton pressedButton = (FloatingActionButton) view;
                        itemList.remove(position);

                        if (itemList.isEmpty()){
                            findViewById(R.id.textView_emptyList).setVisibility(View.VISIBLE);
                        }

                        notifyDataSetChanged(); // Notify the adapter that the dataset has changed

                        Toast.makeText(getApplicationContext(), "Posizione: " + position + "  Tipo: rimuovi" , Toast.LENGTH_SHORT).show();
                    }
                });


                return view;

            }
        };

        ListView listView = findViewById(R.id.listView_itemsRemove);
        listView.setAdapter(adapter);

        ListView listViewResults = findViewById(R.id.listViewResults);

        // Dummy data for demonstration TODO prendere da lista di items
        List<String> allItems = new ArrayList<>();
        allItems.add("Apple");
        allItems.add("Banana");
        allItems.add("Orange");
        allItems.add("Grapes");
        allItems.add("Grapes");
        allItems.add("Grapes");
        allItems.add("Grapes");
        allItems.add("Grapes");
        allItems.add("Grapes");
        allItems.add("");

        // Adapter for the ListView of the search bar
        final ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this,
                R.layout.item_search_prova, new ArrayList<String>());

        listViewResults.setAdapter(adapter2);

        // EditText text change listener for searching
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the items based on the search text
                listViewResults.setVisibility(View.VISIBLE);
                adapter2.clear();

                for (String item : allItems) {
                    if (item.toLowerCase().contains(s.toString().toLowerCase())) {
                        adapter2.add(item);
                    }
                }

                //if (adapter2.isEmpty()) {
                    String currentSearchedText = editTextSearch.getText().toString();
                    allItems.set(allItems.size()-1, currentSearchedText);
                    adapter2.add(currentSearchedText);
                //}
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus)
                    listViewResults.setVisibility(View.GONE);
            }
        });

        // ListView item click listener
        listViewResults.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                // Show a toast with the selected item
                itemList.add(0, selectedItem);
                findViewById(R.id.textView_emptyList).setVisibility(View.GONE);
                adapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
                listViewResults.setVisibility(View.GONE);
                //Toast.makeText(getApplicationContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });







    }

    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.save_changes_title))
                .setMessage(getString(R.string.save_changes_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        // User wants to save changes
                        Toast.makeText(getApplicationContext(), "User wants to save changes", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();

                    }
                })
                .setNeutralButton(android.R.string.cancel, null).show();


    }

    @Override
    public void finish(){


        Intent i = new Intent();
        i.putExtra("CodRisposta","Addio!");
        setResult(RESULT_OK, i);
        super.finish();


    }
}