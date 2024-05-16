package com.example.clickandpack;

import android.content.Context;
import android.util.Pair;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

public class ItemsArrayAdapter {

    private ArrayAdapter<String> adapter ;
    private List<Long> listId = new ArrayList<>();

    public ItemsArrayAdapter(Context context) {
        adapter = new ArrayAdapter<>(context, R.layout.item_search_prova, new ArrayList<>() );
    }

    public ArrayAdapter<String> getAdapter() { return  adapter ; }

    public void add(long id, String nome){
        adapter.add(nome);
        listId.add(id);
    }

    public Pair<Long, String> getIdAndName(int position){
        String name = adapter.getItem(position);
        Long id = listId.get(position);
        return Pair.create(id, name);
    }

    public void clear(){
        adapter.clear();
        listId.clear();
    }
}
