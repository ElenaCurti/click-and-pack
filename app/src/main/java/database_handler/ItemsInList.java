package database_handler;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items_in_list")
public class ItemsInList {

    @PrimaryKey(autoGenerate = true)
    public long id;

    public long listId;
    public long itemId;

    @NonNull
    @Override
    public String toString() {
        return "listId: " + listId + " itemId: " + itemId;
    }
}
