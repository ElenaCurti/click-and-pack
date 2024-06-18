package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

/**
 * DAO class for the item
 */
@Dao
public interface ItemDAO {
    /**
     * Inserts an item in the db.
     *
     * @param item item to insert
     * @return id of currently added item
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertItem(ItemEntity item);

    /**
     * Changes an item id. (This is used because items with id zero cannot be
     * inserted with previous method. So after its' insertion with a "auto increment" id
     * item id can be changed with this method.)
     *
     * @param oldId id of item that you want to change
     * @param newId new id
     */
    @Query("UPDATE items SET id = :newId WHERE id = :oldId")
    void updateItemId(long oldId, long newId);

    /**
     * Method that deletes all items in db
     */
    @Query("DELETE FROM items")
    void deleteAllItems();


    /**
     * Method that return all items detectable by images
     *
     * @return list of items detectable by image
     */
    @Query("SELECT * FROM items WHERE isDetectableByImages IS 1")
    List<ItemEntity> getAllDetectableItems();

    /**
     * Method that return items whose id is in the list of input ids
     *
     * @param ids ids to retrieve
     * @return items with specified id
     */
    @Query("SELECT * FROM items WHERE id IN (:ids)")
    List<ItemEntity> getItemsFromIds(List<Long> ids);
}
