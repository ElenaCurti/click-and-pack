package database_handler;

import static database_handler.MyDatabaseInitiator.detector_type.CUSTOM_OBJECT_DETECTOR;
import static database_handler.MyDatabaseInitiator.detector_type.IMAGE_LABELING;

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

public class MyDatabaseInitiator {

    private final static String TAG_LOGGER="DB_INITIATOR";
    enum detector_type {
        IMAGE_LABELING,
        CUSTOM_OBJECT_DETECTOR
    };
    private final static detector_type myDetectorType = CUSTOM_OBJECT_DETECTOR;
    private final static String FILE_WITH_IMAGE_LABELS= "image_labeler/filtered_image_labes.txt";
    private final static String FILE_WITH_DETECTABLE_OBJECTS_NAME = "custom_object_detector/detectable_objects.csv";


    public static void populateDB(Context appContext, AppDatabase appDatabase){

        // DB reset
        appDatabase.listDao().deleteAllLists();
        appDatabase.itemDao().deleteAllItems();
        appDatabase.itemsInListDao().deleteAllItemsInLists();

        // Read file with image labels and populate "items" DB
        AssetManager assetManager = appContext.getAssets();
        try {
            InputStream inputStream;
            if (myDetectorType == IMAGE_LABELING)
                inputStream = assetManager.open(FILE_WITH_IMAGE_LABELS);
            else if (myDetectorType == CUSTOM_OBJECT_DETECTOR)
                inputStream = assetManager.open(FILE_WITH_DETECTABLE_OBJECTS_NAME);
            else {
                Log.e(TAG_LOGGER, "You are trying to use a detector whose behaviour was not declared!");
                return;
            }
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

        Random random = new Random();

        // Sea list
        ListEntity seaList = new ListEntity("Seaside", "This is the list for my sea items");
        seaList.id  = appDatabase.listDao().insertList(seaList);

        List<Integer> idItemsSea = new ArrayList<>();
        if (myDetectorType == IMAGE_LABELING) {
            idItemsSea.add(30);     // Sunglasses
            idItemsSea.add(317);    // Handbag
            idItemsSea.add(344);    // Wallet
            idItemsSea.add(51);     // Shorts
            idItemsSea.add(68);     // Swimwear
            idItemsSea.add(28);     // Dress
            idItemsSea.add(165);    // Jersey
            idItemsSea.add(159);    // Cap
            idItemsSea.add(192);    // Toothbrush
            idItemsSea.add(193);    // Toothpaste
            idItemsSea.add(224);    // Soap
            idItemsSea.add(316);    // Shampoo
            idItemsSea.add(47);     // Sunscreen
            idItemsSea.add(190);    // Deodorant
            idItemsSea.add(186);    // Moisturizer
            idItemsSea.add(206);    // Hand sanitizer
            idItemsSea.add(293);    // Shoe
        } else if (myDetectorType == CUSTOM_OBJECT_DETECTOR) {
            idItemsSea.add(9);    // Water bottle
            idItemsSea.add(37);   // Towel
            idItemsSea.add(45);   // Sunglasses
            idItemsSea.add(58);   // Surfboard
            idItemsSea.add(86);   // Swimwear
            idItemsSea.add(23);   // T-shirt
            idItemsSea.add(223);   // Flying disc (frisbee)
            idItemsSea.add(187);   // Cap
            idItemsSea.add(397);   // Shoe
            idItemsSea.add(162);   // Flip-flops
            idItemsSea.add(620);   // Perfume
            idItemsSea.add(19);   // Toothbrush
            idItemsSea.add(181);   // Wallet
        }

        for(Integer idItem: idItemsSea) {
          ItemsInList il =  new ItemsInList(seaList.id, idItem, random.nextBoolean());
          il.id = appDatabase.itemsInListDao().insertItemsInList(il);
        }

        // Winter list
        ListEntity winterList = new ListEntity("Winter", "");
        winterList.id  = appDatabase.listDao().insertList(winterList);

        List<Integer> idItemsWinter = new ArrayList<>();
        if (myDetectorType == IMAGE_LABELING) {
            idItemsWinter.add(161); // Hat
            idItemsWinter.add(166); // Scarf
            idItemsWinter.add(83);  // Gloves
            idItemsWinter.add(113); // Watch
            idItemsWinter.add(410); // Umbrella
            idItemsWinter.add(249); // Leggins
            idItemsWinter.add(164); // Beanie
            idItemsWinter.add(192); // Toothbrush
            idItemsWinter.add(193); // Toothpaste
            idItemsWinter.add(224); // Soap
            idItemsWinter.add(316); // Shampoo
            idItemsWinter.add(190); // Deodorant
            idItemsWinter.add(186); // Moisturizer
            idItemsWinter.add(206); // Hand sanitizer
            idItemsWinter.add(293); // Shoe
        } else if (myDetectorType == CUSTOM_OBJECT_DETECTOR) {
            idItemsWinter.add(191); // Hat
            idItemsWinter.add(196); // Scarf
            idItemsWinter.add(41); // Glove
            idItemsWinter.add(546); // Jeans
            idItemsWinter.add(352); // Sweatpants
            idItemsWinter.add(52); // Sweater
            idItemsWinter.add(384); // Snowboard
            idItemsWinter.add(402); // Ski
            idItemsWinter.add(397); // Shoe
            idItemsWinter.add(60); // Boot
            idItemsWinter.add(561); // Watch
            idItemsWinter.add(591); // Umbrella
            idItemsWinter.add(19); // Toothbrush
        }

        for(Integer idItem: idItemsWinter) {
            ItemsInList il =  new ItemsInList(winterList.id, idItem, random.nextBoolean());
            il.id = appDatabase.itemsInListDao().insertItemsInList(il);
        }

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
