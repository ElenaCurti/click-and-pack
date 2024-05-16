package database_handler;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;
    private String name;
    private boolean isDetectableByImages;

    // Constructor
    public ItemEntity(String name, boolean isDetectableByImages) {
        this.name = name;
        this.isDetectableByImages = isDetectableByImages;
    }


    @Ignore
    public ItemEntity(long id, String name) {
        this.id = id;
        this.name = name;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDetectableByImages() {
        return isDetectableByImages;
    }

    public ItemEntity duplicate(){
        ItemEntity copy = new ItemEntity(name, isDetectableByImages);
        copy.id = this.id;
        return copy;
    }
}
