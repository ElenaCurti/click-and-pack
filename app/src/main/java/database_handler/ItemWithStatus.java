package database_handler;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class ItemWithStatus {

    @ColumnInfo(name = "items_in_list_id")
    public long items_in_list_id;

    @Embedded
    public ItemEntity item;

    @ColumnInfo(name = "isChecked")
    public boolean isChecked;
}
