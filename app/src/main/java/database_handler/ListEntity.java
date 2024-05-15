package database_handler;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "lists")
public class ListEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    private String name;
    private String description;

    @Ignore
    public List<ItemEntity> itemsInList;

    // Constructor
    public ListEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
