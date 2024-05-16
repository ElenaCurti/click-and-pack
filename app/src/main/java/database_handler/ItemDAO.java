package database_handler;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface ItemDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertItem(ItemEntity item);

    // Needed to set id to 0
    @Query("UPDATE items SET id = :newId WHERE id = :oldId")
    void updateItemId(long oldId, long newId);

    @Query("DELETE FROM items")
    void deleteAllItems();

    @Query("SELECT * FROM items WHERE isDetectableByImages IS 1")
    List<ItemEntity> getAllDetectableItems();

    @Query("SELECT * FROM items WHERE id IN (:ids)")
    List<ItemEntity> getItemsFromIds(List<Long> ids);



}
