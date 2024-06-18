package database_handler;
import androidx.room.Database;
import androidx.room.RoomDatabase;

/**
 * Class for the database handling
 */
@Database(entities = {ListEntity.class, ItemEntity.class, ItemsInList.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    /* DB name used in the application */
    public static final String DB_NAME = "my-app-database";

    /* DAOs used to handle the db */

    public abstract ListDAO listDao();
    public abstract ItemDAO itemDao();
    public abstract ItemsInListDAO itemsInListDao();
}

