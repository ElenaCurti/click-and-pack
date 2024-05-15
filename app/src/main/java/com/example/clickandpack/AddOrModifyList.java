package com.example.clickandpack;

import static database_handler.AppDatabase.DB_NAME;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import database_handler.AppDatabase;
import database_handler.ItemEntity;
import database_handler.ItemsInList;
import database_handler.ListEntity;


public class AddOrModifyList extends AppCompatActivity {
    private static final String TAG_LOGGER = "MyTag_AddOrModifyList";
    private static final String ID_AND_NAME_SEPERATOR = ")";
    // Exisiting list (if users wants to modify one)
    private ListEntity listEntity = null;

    // All items detectable by images. They will be contained in the dropdown menu
    private List<ItemEntity> allDetectableItems = null;

    private  List<String> itemsInTheList = new ArrayList<>();

    private AppDatabase appDatabase;

    // Response of this view
    private String response = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_modify_list);

        // Read database, according to the action user wants to perform (add new list or modify one)
        Intent i = getIntent();
        String operation = i.getStringExtra(MainActivity.OPERATION_NAME);
        new Thread(new Runnable() {
            @Override
            public void run() {
                readDatabase(operation);
            }
        }).start();

        // Set click listener of the 3 button in the bottom (delete, cancel, save)
        findViewById(R.id.button_saveChanges).setOnClickListener(view -> saveChangesAndFinish());
        findViewById(R.id.button_cancelChanges).setOnClickListener(view -> exitWithoutSaving());
        findViewById(R.id.button_deleteList).setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_list_title))
                .setMessage(getString(R.string.delete_list_message))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, (d,b) -> deleteListAndFinish())
                .setNegativeButton(android.R.string.cancel, null).show();
        });
    }

    private void initializeAppDatabase(){
        if (appDatabase == null)
            appDatabase = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, DB_NAME).build();
    }

    private void readDatabase(String operation){
        initializeAppDatabase();

        // Read the list content if user wants to modify one
        if (operation.startsWith(MainActivity.OPERATION_MODIFY_LIST)) {
            long idList = Long.parseLong( ( (String[]) operation.split(" ") )[1]);
            listEntity = appDatabase.listDao().getListFromId(idList);
            listEntity.itemsInList = appDatabase.itemsInListDao().getAllItemsInList(idList);
        }

        // Read detectable items for the "dropdown" menu
        allDetectableItems = appDatabase.itemDao().getAllDetectableItems();

        // Set GUI
        runOnUiThread( () -> setInitialGUI(operation));

    }

    private void saveChangesAndFinish(){
        // Input checks: list name not empty
        String newName = ((TextView) findViewById(R.id.editText_listName)).getText().toString();
        if (newName.equals("")) {
            Toast.makeText(this, getString(R.string.error_name_non_empty), Toast.LENGTH_LONG).show();
            return;
        }

        new Thread(() -> {
            // run() method content:
            initializeAppDatabase();
            String newdescription = ((TextView) findViewById(R.id.editText_listDescription)).getText().toString();

            if (listEntity == null) {
                // User wants to create a new list
                listEntity = new ListEntity(newName, newdescription);
                listEntity.id = appDatabase.listDao().insertList(listEntity);

                response = getString(R.string.list_creation_ok);
            } else {
                // Update exsisting name and description
                appDatabase.listDao().updateListNameAndDescription(listEntity.id, newName, newdescription);
                // TODO questo e' sbagliato. gli item gia' in valigia non devono essere modificati
                appDatabase.itemsInListDao().deleteItemsInListByListId(listEntity.id);

                response = getString(R.string.list_update_ok);
            }

            for (String itemIdAndName : itemsInTheList) {
                int index = itemIdAndName.indexOf(ID_AND_NAME_SEPERATOR);
                long idItem = Long.parseLong(itemIdAndName.substring(0, index));
                String nameItem = itemIdAndName.substring(index + 1);

                if (idItem == -1) {
                    // New item creation
                    ItemEntity item1 = new ItemEntity(nameItem, false);
                    item1.id = appDatabase.itemDao().insertItem(item1);
                    idItem = item1.id;
                }

                // Insert new item in the list
                ItemsInList itemsInList1 = new ItemsInList(listEntity.getId(), idItem, false);
                itemsInList1.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList1);
            }
            finish();
        }).start();
    }

    private void exitWithoutSaving(){
        response = getString(R.string.cancel_changes_ok);
        finish();
    }

    private void deleteListAndFinish(){
        new Thread( ()  ->  {
            appDatabase.listDao().deleteListById(listEntity.id);
            response = getString(R.string.delete_list_ok);
            finish();
        }).start();
    }
    @SuppressLint("MissingSuperCall")
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
            .setTitle(getString(R.string.save_changes_title))
            .setMessage(getString(R.string.save_changes_message))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.yes, (dialog,  whichButton) -> saveChangesAndFinish() )
            .setNegativeButton(R.string.no,  (dialog,  whichButton) -> exitWithoutSaving() )
            .setNeutralButton(android.R.string.cancel, null).show();
    }

    @Override
    public void finish(){
        Intent i = new Intent();
        i.putExtra(MainActivity.RESPONSE_KEY, response);
        setResult(RESULT_OK, i);
        super.finish();
    }


    private void setInitialGUI(String operation){
        // EditText with list name cannot be empty
        EditText editTextListName = findViewById(R.id.editText_listName);
        editTextListName.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s)  {
                if (s.toString().trim().isEmpty())
                    editTextListName.setError(getString(R.string.error_name_non_empty));
                else
                    editTextListName.setError(null);
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        });
        editTextListName.setError(getString(R.string.error_name_non_empty));


        if (operation.equals(MainActivity.OPERATION_ADD_LIST)) {
            // If user wants to create a new list, i delete the "delete list" button
            findViewById(R.id.button_deleteList).setVisibility(View.GONE);
        } else {
            // Otherwise, if user wants to modify existing list, i set initial fields with list content
            editTextListName.setText(listEntity.getName());

            EditText editTextListDescription = findViewById(R.id.editText_listDescription);
            editTextListDescription.setText(listEntity.getDescription());

            if ( listEntity.itemsInList != null ) {
                for (ItemEntity ie : listEntity.itemsInList) {
                    itemsInTheList.add(ie.getId() + ID_AND_NAME_SEPERATOR + ie.getName());
                }
            }
        }

        if (itemsInTheList.isEmpty()) {
            findViewById(R.id.textView_emptyList).setVisibility(View.VISIBLE);
        }

        // Set the items already present in the list (if any) with the "remove" button
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.item_lista_da_rimuovere, itemsInTheList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                // Create view if null
                if (view == null) {
                    LayoutInflater inflater = LayoutInflater.from(getContext());
                    view = inflater.inflate(R.layout.item_lista_da_rimuovere, parent, false);
                }

                // Get item name and set it in the textview
                int index = itemsInTheList.get(position).indexOf(ID_AND_NAME_SEPERATOR);
                String justText = itemsInTheList.get(position).substring(index+1);
                TextView itemName = view.findViewById(R.id.textView_itemName);
                itemName.setText(justText);
                // TODO usa setTag

                // Set remove button of corresponding item
                view.findViewById(R.id.floatingActionButton_removeItem)
                    .setOnClickListener(clickedView -> {
                        itemsInTheList.remove(position);

                        if (itemsInTheList.isEmpty()){
                            findViewById(R.id.textView_emptyList).setVisibility(View.VISIBLE);
                        }
                        // Notify the adapter that the dataset has changed
                        notifyDataSetChanged();
                    });

                return view;
            }
        };

        ListView listView = findViewById(R.id.listView_itemsRemove);
        listView.setAdapter(adapter);

        // Dropdown menu
        ListView listViewDropDownMenu = findViewById(R.id.listViewResults);

        final ItemsArrayAdapter dropDownMenuHandler = new ItemsArrayAdapter(this);
        listViewDropDownMenu.setAdapter(dropDownMenuHandler.getAdapter());
        listViewDropDownMenu.setOnItemClickListener( (parent, view, position, id) -> {
            // When user clicks on an item of the drop down menu, I add it to the list of items
            Pair<Long, String> pair = dropDownMenuHandler.getIdAndName(position);
            long idItem = pair.first;
            String name = pair.second;
            itemsInTheList.add(0, idItem + ID_AND_NAME_SEPERATOR + name);
            adapter.notifyDataSetChanged(); // Notify the adapter that the dataset has changed
            findViewById(R.id.textView_emptyList).setVisibility(View.GONE);
            listViewDropDownMenu.setVisibility(View.GONE);
        });

        // EditText text change listener for searching
        EditText editTextSearch = findViewById(R.id.editTextText_newItemName);
        editTextSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Filter the items based on the search text
                listViewDropDownMenu.setVisibility(View.VISIBLE);
                dropDownMenuHandler.clear();

                // Items that match the searched item
                if (allDetectableItems != null ) {
                    for (ItemEntity itemEntity : allDetectableItems) {
                        String item_name = itemEntity.getName();
                        long item_id = itemEntity.getId();
                        if (item_name.toLowerCase().contains(s.toString().toLowerCase())) {
                            dropDownMenuHandler.add(item_id, item_name);
                        }
                    }
                }

                // Last item is always the user input, so he/she can insert custom objects
                String currentSearchedText = editTextSearch.getText().toString();
                if (!currentSearchedText.equals(""))
                    dropDownMenuHandler.add(-1, currentSearchedText);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });


        editTextSearch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // If user is not typing in the edittext, i remove the dropdown menu
                if (!hasFocus)
                    listViewDropDownMenu.setVisibility(View.GONE);
            }
        });
    }

}