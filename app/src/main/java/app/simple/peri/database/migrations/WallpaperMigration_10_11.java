package app.simple.peri.database.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class WallpaperMigration_10_11 extends Migration {
    
    public WallpaperMigration_10_11() {
        super(10, 11);
    }
    
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Create the new table with correct schema
        database.execSQL("CREATE TABLE IF NOT EXISTS `wallpapers_new` (" +
                "`id` INTEGER NOT NULL PRIMARY KEY, " +
                "`other_column` TEXT)");
        
        // Copy and convert data (assuming ids are numeric strings)
        database.execSQL("INSERT INTO `wallpapers_new` (`id`, `other_column`) " +
                "SELECT CAST(`id` AS INTEGER), `other_column` FROM `wallpapers`");
        
        // Rename new table
        database.execSQL("ALTER TABLE `wallpapers_new` RENAME TO `wallpapers`");
    }
}
