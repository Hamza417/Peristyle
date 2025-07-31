package app.simple.peri.database.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * 10 to 11 only changed wallpaper id from TEXT to INTEGER.
 */
public class WallpaperMigration_10_11 extends Migration {
    
    public WallpaperMigration_10_11() {
        super(10, 11);
    }
    
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Migrate wallpaper table
        // 1. Create new table with correct schema (id as INTEGER)
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `wallpapers_new` (" +
                        "`name` TEXT, " +
                        "`uri` TEXT NOT NULL, " +
                        "`file_path` TEXT NOT NULL, " +
                        "`id` INTEGER NOT NULL, " +
                        "`prominentColor` INTEGER NOT NULL, " +
                        "`width` INTEGER, " +
                        "`height` INTEGER, " +
                        "`dateModified` INTEGER NOT NULL, " +
                        "`size` INTEGER NOT NULL, " +
                        "`folder_id` INTEGER NOT NULL, " +
                        "`isSelected` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                        );
        
        // 2. Copy and convert data
        database.execSQL(
                "INSERT INTO `wallpapers_new` (" +
                        "`name`, `uri`, `file_path`, `id`, `prominentColor`, `width`, `height`, `dateModified`, `size`, `folder_id`, `isSelected`" +
                        ") SELECT " +
                        "`name`, `uri`, `file_path`, CAST(`id` AS INTEGER), `prominentColor`, `width`, `height`, `dateModified`, `size`, `folder_id`, `isSelected` " +
                        "FROM `wallpapers`"
                        );
        
        // 3. Drop old table
        database.execSQL("DROP TABLE `wallpapers`");
        
        /// 4. Rename new table (handle Android < 9)
        try {
            database.execSQL("ALTER TABLE `wallpapers_new` RENAME TO `wallpapers`");
        } catch (Exception e) {
            // Fallback: create, copy, drop, and recreate
            database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `wallpapers` (" +
                            "`name` TEXT, " +
                            "`uri` TEXT NOT NULL, " +
                            "`file_path` TEXT NOT NULL, " +
                            "`id` INTEGER NOT NULL, " +
                            "`prominentColor` INTEGER NOT NULL, " +
                            "`width` INTEGER, " +
                            "`height` INTEGER, " +
                            "`dateModified` INTEGER NOT NULL, " +
                            "`size` INTEGER NOT NULL, " +
                            "`folder_id` INTEGER NOT NULL, " +
                            "`isSelected` INTEGER NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                            );
            database.execSQL(
                    "INSERT INTO `wallpapers` (" +
                            "`name`, `uri`, `file_path`, `id`, `prominentColor`, `width`, `height`, `dateModified`, `size`, `folder_id`, `isSelected`" +
                            ") SELECT " +
                            "`name`, `uri`, `file_path`, `id`, `prominentColor`, `width`, `height`, `dateModified`, `size`, `folder_id`, `isSelected` " +
                            "FROM `wallpapers_new`"
                            );
            database.execSQL("DROP TABLE `wallpapers_new`");
        }
        
        // 5. Recreate indices
        database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_wallpapers_id` ON `wallpapers` (`id`)");
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_wallpapers_folder_id` ON `wallpapers` (`folder_id`)");
        database.execSQL("CREATE INDEX IF NOT EXISTS `index_wallpapers_dateModified` ON `wallpapers` (`dateModified`)");
        
        // Migration for wallpaper_usage table
        // 6. Create new wallpaper_usage table with INTEGER wallpaper_id
        database.execSQL(
                "CREATE TABLE IF NOT EXISTS `wallpaper_usage_new` (" +
                        "`usage_count` INTEGER NOT NULL, " +
                        "`wallpaper_id` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`wallpaper_id`), " +
                        "FOREIGN KEY(`wallpaper_id`) REFERENCES `wallpapers`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                        );
        
        // 7. Copy and convert data
        database.execSQL(
                "INSERT INTO `wallpaper_usage_new` (`usage_count`, `wallpaper_id`) " +
                        "SELECT `usage_count`, CAST(`wallpaper_id` AS INTEGER) FROM `wallpaper_usage`"
                        );
        
        // 8. Drop old table
        database.execSQL("DROP TABLE `wallpaper_usage`");
        
        // 9. Rename new table
        database.execSQL("ALTER TABLE `wallpaper_usage_new` RENAME TO `wallpaper_usage`");
    }
}
