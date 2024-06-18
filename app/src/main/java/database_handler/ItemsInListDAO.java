package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
/**
 * DAO class for the itemsInList
 */
@Dao
public interface ItemsInListDAO {

    @Insert
    long insertItemsInList(ItemsInList itemsInList);

    @Query("SELECT items.* FROM items_in_list " +
            "INNER JOIN items ON items_in_list.itemId = items.id " +
            "WHERE listId = :idSearched")
    List<ItemEntity> getAllItemsInList(long idSearched);

    @Query("DELETE FROM items_in_list")
    void deleteAllItemsInLists();

    @Query("DELETE FROM items_in_list WHERE listId = :listId AND itemId = :itemId")
    void deleteItemsInListByListIdAndItemId(long listId, long itemId);

    @Query("SELECT items.*, isChecked, items_in_list.id AS items_in_list_id " +
            "FROM items_in_list " +
            "INNER JOIN items ON items_in_list.itemId = items.id " +
            "WHERE items_in_list.listId = :listId")
    List<ItemWithStatus> getItemsWithStatus(long listId);

    @Query("UPDATE items_in_list SET isChecked = :isChecked WHERE id = :itemId")
    void updateItemChecked(long itemId, boolean isChecked);


    @Query("UPDATE items_in_list SET isChecked = :isChecked WHERE listId = :listId AND itemId = :itemId")
    void updateItemCheckedUsingListAndItemId(long listId, long itemId, boolean isChecked);

    @Query("SELECT COUNT(*) FROM items_in_list " +
            "INNER JOIN items ON items_in_list.itemId = items.id " +
            "WHERE items_in_list.listId = :listId AND items.isDetectableByImages = 1")
    int countItemsInListDetectableByImages(long listId);


}


