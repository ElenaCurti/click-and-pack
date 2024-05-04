package com.example.clickandpack;

import androidx.appcompat.app.AppCompatActivity;

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
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ADD_LIST = 1;
    private static final String TAG_LOGGER = "MyTag_MainActivity";

    public static final String OPERATION_NAME = "action";
    public static final String OPERATION_ADD_LIST = "add_list";
    public static final String OPERATION_MODIFY_LIST = "modify_list";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        populateListsCollection();

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
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_LIST) {
            if (resultCode == RESULT_OK) {
                String risposta = data.getExtras().getString("CodRisposta");
                Toast.makeText(this, "Risposta: " + risposta, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "There was an error during the creation of a new list!", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void populateListsCollection() {

        // Sample data for main and sub items
        List<String> mainItems = new ArrayList<>();
        mainItems.add("Sea list");
        mainItems.add("Ski list");

        List<String> subItems = new ArrayList<>();
        subItems.add("List for sea items");
        subItems.add("List for skii vacation");

        // TODO se lista vuota aggiungi textview del tipo "schiaccia + per aggiungere lista"
        /* Create an adapter and assign it to the ListView */
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.elenco_liste_prima_videata, mainItems) {
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

                mainItemTextView.setText(mainItems.get(position));
                subItemTextView.setText(subItems.get(position));

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
                            i.putExtra(OPERATION_NAME, OPERATION_MODIFY_LIST + " " + position);
                            startActivityForResult(i,REQUEST_CODE_ADD_LIST);
                        }
                        Toast.makeText(getApplicationContext(), "Posizione: " + position + "  Tipo: " + tipoButton , Toast.LENGTH_SHORT).show();
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