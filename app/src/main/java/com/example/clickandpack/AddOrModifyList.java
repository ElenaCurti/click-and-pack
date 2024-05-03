package com.example.clickandpack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class AddOrModifyList extends AppCompatActivity {
    private static final String TAG_LOGGER = "MyTag_AddOrModifyList";

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
    }

    @Override
    public void finish(){
        Intent i = new Intent();
        i.putExtra("CodRisposta","Addio!");
        setResult(RESULT_OK, i);
        super.finish();
    }
}