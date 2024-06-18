package database_handler;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

/**
 * Ausiliary class for items and their "status" (packed or not packed)
 */
public class ItemWithStatus {

    @ColumnInfo(name = "items_in_list_id")
    public long items_in_list_id;

    @Embedded
    public ItemEntity item;

    @ColumnInfo(name = "isChecked")
    public boolean isChecked;
}
