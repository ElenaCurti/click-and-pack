package com.example.clickandpack;

import androidx.appcompat.app.AppCompatActivity;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;



public class AddOrModifyList extends AppCompatActivity {
    private static final String TAG_LOGGER = "MyTag_AddOrModifyList";
    private String[] itemsNames = {"Select item...", "Elemento 1", "Elemento 2", "Elemento 3", "Elemento 4", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_or_modify_list);

        Intent i = getIntent();
        String ops = i.getStringExtra("action");

        setViewItems();

    }

    private void setViewItems(){
        // Sample list of items
        List<String> itemList = new ArrayList<>();
        itemList.add("T-Shirts");
        itemList.add("Swimsuit");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");
        itemList.add("Flip-flops");

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
                        Toast.makeText(getApplicationContext(), "Posizione: " + position + "  Tipo: rimuovi" , Toast.LENGTH_SHORT).show();
                    }
                });


                return view;

            }
        };

        ListView listView = findViewById(R.id.listView_itemsRemove);
        listView.setAdapter(adapter);


        /* // Metodo 1 con spinner
        EditText newItemName = (EditText) findViewById(R.id.editTextText_newItemName);
        Spinner itemsSpinner = (Spinner) findViewById(R.id.spinner_itemsName);
        ArrayAdapter<CharSequence> adapterItemsSpinner = new ArrayAdapter<>(this, R.layout.simple_list_item_1, itemsNames);
        adapterItemsSpinner.setDropDownViewResource(android.R.layout.simple_list_item_1);
        itemsSpinner.setAdapter(adapterItemsSpinner);
        itemsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                //((TextView) view).setTextSize(50);
                //Log.d(TAG_LOGGER,((TextView) view).getText().toString() );

                if ( position == itemsNames.length - 1 ) {
                    // Last item selected
                    //findViewById(R.id.editTextText_newItemName).setVisibility(View.VISIBLE);
                    newItemName.setVisibility(View.VISIBLE);
                    itemsSpinner.setVisibility(View.GONE);
                    Toast.makeText(getApplicationContext(), "Last item " + itemsNames[position] + " Selected..", Toast.LENGTH_SHORT).show();


                } else
                    Toast.makeText(getApplicationContext(), "" + itemsNames[position] + " Selected..", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        itemsSpinner.setSelection(adapter.getPosition( itemsNames[0]));

        newItemName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String testo = newItemName.getText().toString();
                    if ( testo.equals("") ) {
                        Log.d(TAG_LOGGER + "_testo", "TESTO VUOTO");
                        newItemName.setVisibility(View.GONE);
                        itemsSpinner.setVisibility(View.VISIBLE);
                    }

                    Log.d(TAG_LOGGER + "_testo", "Testo: " + testo);

                }

                Log.d(TAG_LOGGER + "_focus", "Focus: " + hasFocus);
            }
        }); */


        EditText editTextSearch = findViewById(R.id.editTextText_newItemName);
        ListView listViewResults = findViewById(R.id.listViewResults);

        // Dummy data for demonstration
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
        allItems.add("Grapes");

        // Adapter for the ListView
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
                Toast.makeText(getApplicationContext(), "Selected: " + selectedItem, Toast.LENGTH_SHORT).show();
            }
        });





    }




    @Override
    public void finish(){
        Intent i = new Intent();
        i.putExtra("CodRisposta","Addio!");
        setResult(RESULT_OK, i);
        super.finish();
    }
}