package database_handler;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items_in_list")
public class ItemsInList {

    @PrimaryKey(autoGenerate = true)
    public long id;
    private long listId;
    private long itemId;
    private boolean isChecked;

    public ItemsInList(long listId, long itemId, boolean isChecked) {
        this.listId = listId;
        this.itemId = itemId;
        this.isChecked = isChecked;
    }

    public long getListId(){
        return listId;
    }

    public long getItemId(){
        return itemId;
    }

    public boolean getIsChecked() {
        return isChecked;
    }

    @NonNull
    @Override
    public String toString() {
        return "listId: " + listId + " itemId: " + itemId + " isChecked: " + isChecked ;
    }
}
