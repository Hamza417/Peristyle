package app.simple.peri.database.migrations;

import android.database.sqlite.SQLiteException;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class TagsMigration extends Migration {
    public TagsMigration(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }
    
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        try {
            // Create the new table with correct schema
            database.execSQL("CREATE TABLE IF NOT EXISTS `tags_new` (" +
                    "`name` TEXT NOT NULL, " +
                    "`ids` TEXT NOT NULL, " +
                    "PRIMARY KEY(`name`))");
            
            // Copy the data from the old table to the new one
            database.execSQL("INSERT INTO `tags_new` (`name`, `ids`) " +
                    "SELECT `name`, IFNULL(`ids`, '') FROM `tags`");
            
            // Remove the old table
            database.execSQL("DROP TABLE `tags`");
            
            // Rename new table to original name
            database.execSQL("ALTER TABLE `tags_new` RENAME TO `tags`");
        } catch (SQLiteException e) {
            // Something failed, recreate the table
            database.execSQL("DROP TABLE IF EXISTS `tags`");
            database.execSQL("DROP TABLE IF EXISTS `tags_new`");
        }
    }
}
