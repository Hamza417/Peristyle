package app.simple.peri.models;

import java.io.Serializable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

@Entity (tableName = "wallpaper_usage",
        foreignKeys = @ForeignKey (entity = Wallpaper.class,
                parentColumns = "id",
                childColumns = "wallpaper_id",
                onDelete = ForeignKey.CASCADE)
)
public class WallpaperUsage implements Serializable {
    
    @PrimaryKey
    @ColumnInfo (name = "wallpaper_id")
    private int wallpaperId;
    
    @ColumnInfo (name = "usage_count")
    private int usageCount;
    
    public WallpaperUsage(int wallpaperId, int usageCount) {
        this.wallpaperId = wallpaperId;
        this.usageCount = usageCount;
    }
    
    public int getWallpaperId() {
        return wallpaperId;
    }
    
    public int getUsageCount() {
        return usageCount;
    }
    
    public void setWallpaperId(int wallpaperId) {
        this.wallpaperId = wallpaperId;
    }
    
    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "WallpaperUsage{" +
                "wallpaperId='" + wallpaperId + '\'' +
                ", usageCount=" + usageCount +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof WallpaperUsage that)) {
            return false;
        }
        return usageCount == that.usageCount && wallpaperId == that.wallpaperId;
    }
}
