package com.example.clickandpack;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

public class DropDownItemsMenu {

    private ArrayAdapter<String> adapter ;
    private String customStringDistinguish;
    private List<Long> listId = new ArrayList<>();

    public DropDownItemsMenu(Context context, String customStringDistinguish) {
        adapter = new ArrayAdapter<String>(context, R.layout.item_search_prova, new ArrayList<String>()) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                // Last item is the custom one, so i set a different color
                if (position == getCount() - 1) {
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.colore_background_item_custom));
                } else {
                    view.setBackgroundColor(ContextCompat.getColor(context, R.color.colore_background));
                }

                return view;
            }
        };

        this.customStringDistinguish = customStringDistinguish;
    }

    public ArrayAdapter<String> getAdapter() { return  adapter ; }

    public void add(long id, String nome){
        if (id == -1)
            nome += customStringDistinguish;
        adapter.add(nome);
        listId.add(id);
    }

    public Pair<Long, String> getIdAndName(int position){
        String name = adapter.getItem(position);
        Long id = listId.get(position);
        if (id == -1)
            name = name.substring(0, name.length() - customStringDistinguish.length());
        return Pair.create(id, name);
    }

    public void clear(){
        adapter.clear();
        listId.clear();
    }
}
