package database_handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

public class MyDatabaseInitiator {

    private final static String TAG_LOGGER="DB_INITIATOR";
    private final static String FILE_WITH_IMAGE_LABELS="filtered_image_labes.txt";


    public static void populateDB(Context appContext, AppDatabase appDatabase){

        // DB reset
        appDatabase.listDao().deleteAllLists();
        appDatabase.itemDao().deleteAllItems();
        appDatabase.itemsInListDao().deleteAllItemsInLists();

        // Read file with image labels and populate "items" DB
        AssetManager assetManager = appContext.getAssets();
        try {
            InputStream inputStream = assetManager.open(FILE_WITH_IMAGE_LABELS);
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String line;
            while ((line = bufferedReader.readLine()) != null) {
                // Skip first line
                if (line.charAt(0) == '#')
                    continue;

                // Split the line into columns
                String[] columns = line.split(",");

                ItemEntity item1 = new ItemEntity(columns[1], true);
                item1.id = Long.parseLong(columns[0]);
                item1.id = appDatabase.itemDao().insertItem(item1);

                // Insertion of item with id zero, must be "forced"
                if (columns[0].equals("0")){
                    appDatabase.itemDao().updateItemId(item1.id, 0 );
                }

                Log.d("CSV Content", "Item id: " + item1.id + ", name: " + item1.getName());
            }

            bufferedReader.close();
        } catch (IOException e) {
            Log.d("CSV Content", "Error: "+ e.getMessage());
            e.printStackTrace();
        }

        List<ItemEntity> items = appDatabase.itemDao().getAllItems();
        for(ItemEntity ie: items)
            Log.d("item_dopo", ie.id + " " +  ie.getName());

        /*
        appDatabase.listDao().deleteAllLists();
        appDatabase.itemDao().deleteAllItems();
        appDatabase.itemsInListDao().deleteAllItemsInLists();

        Log.d("RISULTATO_INSERIMENTO", "Gia esiste? " + appDatabase.itemDao().getItemFromId().toString());
        ItemEntity item1 = new ItemEntity("item 1", true);
        item1.id = 1;
        item1.id = appDatabase.itemDao().insertItem(item1);
        Log.d("RISULTATO_INSERIMENTO", "" + item1.id);
        */
    }

    private static void creazioneEdUsoContentPersonalizzato(AppDatabase appDatabase, boolean resetAllaFine){

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

        ItemsInList itemsInList1 = new ItemsInList(list1.getId(), item1.getId(), false);
        itemsInList1.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList1);

        ItemsInList itemsInList2 = new ItemsInList(list1.getId(), item2.getId(), false);
        itemsInList2.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList2);

        // Create list 2 with items item 2 and item 3
        ListEntity list2 = new ListEntity("list 2", "This is the description for list 2");
        list2.id = appDatabase.listDao().insertList(list2);

        ItemsInList itemsInList3 = new ItemsInList(list2.getId(), item2.getId(), true);
        itemsInList3.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList3);

        ItemsInList itemsInList4 = new ItemsInList(list2.getId(), item3.getId(), false);
        itemsInList4.id = appDatabase.itemsInListDao().insertItemsInList(itemsInList4);



    }
}
