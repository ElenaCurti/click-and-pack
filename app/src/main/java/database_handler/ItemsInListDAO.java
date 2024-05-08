package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ItemsInListDAO {

    @Insert
    long insertItemsInList(ItemsInList itemsInList);

    @Query("SELECT * FROM items_in_list")
    List<ItemsInList> getAllItemsInLists();


    @Query("SELECT items.name FROM items_in_list " +
            "JOIN items ON items_in_list.itemId = items.id " +
            "WHERE items_in_list.listId = :idSearched")
    List<String> getItemsNamesInListsFromIdList(long idSearched);

    @Query("SELECT items.* FROM items_in_list " +
            "INNER JOIN items ON items_in_list.itemId = items.id " +
            "WHERE listId = :idSearched")
    List<ItemEntity> getAllItemsInList(long idSearched);

    @Query("SELECT items.* FROM items_in_list " +
            "INNER JOIN items ON items_in_list.itemId = items.id " +
            "WHERE listId = :idSearched AND items_in_list.isChecked IS :checkedStatus")
    List<ItemEntity> getItemsInListWithGivenChecked(long idSearched, int checkedStatus);


    @Query("DELETE FROM items_in_list")
    void deleteAllItemsInLists();

    @Query("DELETE FROM items_in_list WHERE listId = :listId")
    void deleteItemsInListByListId(long listId);

}


