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

/**
 * Auxiliary class for the dropdown menu shown in the AddOrModifyList activity
 */
public class DropDownItemsMenu {
    /**  Adapter for the items' names in the dropdown menu */
    private ArrayAdapter<String> adapter ;

    /**  Ids of the items in the dropdown menu */
    private List<Long> listId = new ArrayList<>();

    /**  String added to distinguish between "detectable by images" and "not detectable by images" items */
    private String customStringDistinguish;

    /**
     * Creates the dropdown's menu, with none items. Last item will have different
     * color from others, because it will be "not detectable" by images.
     * @param context context where the dropdown menu is shown
     * @param customStringDistinguish String added to "not detectable by images" items
     */
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

    /**
     * Returns the adapter
     * @return the adapter
     */
    public ArrayAdapter<String> getAdapter() { return  adapter ; }

    /**
     * Add item to the dropdown menu
     * @param id id of the item. -1 is interpreted as an item "not detectable by images", so the
     *           customStringDistinguish will be added to its name
     * @param nome name of the item to add
     */
    public void add(long id, String nome){
        if (id == -1)
            nome += customStringDistinguish;
        adapter.add(nome);
        listId.add(id);
    }

    /**
     * Returns id and name of the item at the input position
     * @param position index position of the list
     * @return pair formed by id and name of the item
     */
    public Pair<Long, String> getIdAndName(int position){
        String name = adapter.getItem(position);
        Long id = listId.get(position);
        if (id == -1)
            name = name.substring(0, name.length() - customStringDistinguish.length());
        return Pair.create(id, name);
    }

    /**
     * Resets the dropdown menu
     */
    public void clear(){
        adapter.clear();
        listId.clear();
    }
}
