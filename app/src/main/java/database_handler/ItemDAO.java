package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemDAO {

    @Insert
    long insertItem(ItemEntity item);


    @Query("DELETE FROM items")
    void deleteAllItems();


    @Query("SELECT * FROM items")
    List<ItemEntity> getAllItems();
}
