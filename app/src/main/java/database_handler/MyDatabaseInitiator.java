package database_handler;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Auxiliary class with a static method that:
 * - resets lists, items, itemsInList tables in the DB
 * - inserts the detectable items in the "item" table
 * - creates two lists: one for the sea and one for the winter. They are filled with proper items.
 */
public class MyDatabaseInitiator {

    private final static String TAG_LOGGER="DB_INITIATOR";
    //private final static String FILE_WITH_IMAGE_LABELS= "image_labeler/filtered_image_labes.txt";

    /** Path to the csv with the object detection's labels */
    private final static String FILE_WITH_DETECTABLE_OBJECTS_NAME = "custom_object_detector/detectable_objects.csv";


    public static void resetAndPopulateDB(Context appContext, AppDatabase appDatabase){
        if (appDatabase.itemDao().getAllDetectableItems().size() > 0 )
            return;

        // DB reset
        appDatabase.listDao().deleteAllLists();
        appDatabase.itemDao().deleteAllItems();
        appDatabase.itemsInListDao().deleteAllItemsInLists();

        // Read file with image labels and populate "items" DB
        AssetManager assetManager = appContext.getAssets();
        try {
            InputStream inputStream= assetManager.open(FILE_WITH_DETECTABLE_OBJECTS_NAME);
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
            }

            bufferedReader.close();
        } catch (IOException e) {
            Log.d(TAG_LOGGER, "Error: "+ e.getMessage());
            e.printStackTrace();
        }

        Random random = new Random();

        // Sea list
        ListEntity seaList = new ListEntity("Seaside", "This is the list for my sea items");
        seaList.id  = appDatabase.listDao().insertList(seaList);

        List<Integer> idItemsSea = new ArrayList<>();
        idItemsSea.add(9);      // Water bottle
        idItemsSea.add(37);     // Towel
        idItemsSea.add(45);     // Sunglasses
        idItemsSea.add(58);     // Surfboard
        idItemsSea.add(86);     // Swimwear
        idItemsSea.add(23);     // T-shirt
        idItemsSea.add(223);    // Flying disc (frisbee)
        idItemsSea.add(187);    // Cap
        idItemsSea.add(397);    // Shoe
        idItemsSea.add(162);    // Flip-flops
        idItemsSea.add(620);    // Perfume
        idItemsSea.add(19);     // Toothbrush
        idItemsSea.add(181);    // Wallet

        for(Integer idItem: idItemsSea) {
          ItemsInList il =  new ItemsInList(seaList.id, idItem, random.nextBoolean());
          il.id = appDatabase.itemsInListDao().insertItemsInList(il);
        }

        // Winter list
        ListEntity winterList = new ListEntity("Winter", "");
        winterList.id  = appDatabase.listDao().insertList(winterList);

        List<Integer> idItemsWinter = new ArrayList<>();
        idItemsWinter.add(191);     // Hat
        idItemsWinter.add(196);     // Scarf
        idItemsWinter.add(41);      // Glove
        idItemsWinter.add(546);     // Jeans
        idItemsWinter.add(352);     // Sweatpants
        idItemsWinter.add(52);      // Sweater
        idItemsWinter.add(384);     // Snowboard
        idItemsWinter.add(402);     // Ski
        idItemsWinter.add(397);     // Shoe
        idItemsWinter.add(60);      // Boot
        idItemsWinter.add(561);     // Watch
        idItemsWinter.add(591);     // Umbrella
        idItemsWinter.add(19);      // Toothbrush

        for(Integer idItem: idItemsWinter) {
            ItemsInList il =  new ItemsInList(winterList.id, idItem, random.nextBoolean());
            il.id = appDatabase.itemsInListDao().insertItemsInList(il);
        }
    }
}
