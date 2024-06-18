package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

/**
 * DAO class for the list
 */
@Dao
public interface ListDAO {
    @Insert
    long insertList(ListEntity list);

    @Query("SELECT * FROM lists")
    List<ListEntity> getAllLists();

    @Query("DELETE FROM lists")
    void deleteAllLists();


    @Query("SELECT * FROM lists WHERE id=:searchedId")
    ListEntity getListFromId(long searchedId);

    @Query("UPDATE lists SET name = :newName, description = :newDescription WHERE id = :listId")
    void updateListNameAndDescription(long listId, String newName, String newDescription);


    @Query("DELETE FROM lists WHERE id = :listId")
    void deleteListById(long listId);

}
