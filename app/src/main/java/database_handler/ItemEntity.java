package database_handler;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "items")
public class ItemEntity {

    @PrimaryKey(autoGenerate = true)
    public long id;     // TODO set to private
    private String name;
    private boolean isDetectableByImages;

    // Constructor
    public ItemEntity(String name, boolean isDetectableByImages) {
        this.name = name;
        this.isDetectableByImages = isDetectableByImages;
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
}
