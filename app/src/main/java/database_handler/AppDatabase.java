package database_handler;
import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {ListEntity.class, ItemEntity.class, ItemsInList.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ListDAO listDao();
    public abstract ItemDAO itemDao();

    public abstract ItemsInListDAO itemsInListDao();


}
