package com.example.clickandpack;

import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ItemsArrayAdapter extends ArrayAdapter<String> {
    List<Long> listId = new ArrayList<>();
    public ItemsArrayAdapter(@NonNull Context context, int resource, @NonNull List<String> objects) {
        super(context, resource, objects);
        Log.d("TAG", "Sono qui");
    }

    public void add(long id, String nome){
        super.add(nome);
        listId.add(id);
    }

    public Pair<Long, String> getIdAndName(int position){
        String name = super.getItem(position);
        Long id = listId.get(position);
        return Pair.create(id, name);
    }

    @Override
    public void clear(){
        super.clear();
        listId.clear();
    }


}
