package app.simple.peri.database.migrations;

import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class WallpaperMigration_9_10 extends Migration {
    
    public WallpaperMigration_9_10() {
        super(9, 10);
    }
    
    public WallpaperMigration_9_10(int startVersion, int endVersion) {
        super(startVersion, endVersion);
    }
    
    @Override
    public void migrate(SupportSQLiteDatabase database) {
        // Create the new table with correct schema
        database.execSQL("CREATE TABLE IF NOT EXISTS `wallpaper_usage` (" +
                "`wallpaper_id` TEXT NOT NULL, " +
                "`usage_count` INTEGER NOT NULL, " +
                "PRIMARY KEY(`wallpaper_id`), " +
                "FOREIGN KEY(`wallpaper_id`) REFERENCES `wallpapers`(`id`) ON DELETE CASCADE)");
    }
}
