package database_handler;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    /* Id of the item. It will be auto increment. */
    @PrimaryKey(autoGenerate = true)
    public long id;

    /* Name of the item */
    private String name;

    /* Flag to determine if item is detectable by images or not */
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

    /* Getters */
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isDetectableByImages() {
        return isDetectableByImages;
    }

    /**
     * Method that return a copy of current item
      * @return copy of item with same id, name, isDetectableByImages
     */
    public ItemEntity duplicate(){
        ItemEntity copy = new ItemEntity(name, isDetectableByImages);
        copy.id = this.id;
        return copy;
    }
}
