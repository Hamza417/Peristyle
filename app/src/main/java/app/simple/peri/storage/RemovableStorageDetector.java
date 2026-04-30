package app.simple.peri.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Modern SD card/removable storage detection utility for Android 10+.
 * Requires MANAGE_EXTERNAL_STORAGE permission for full access.
 * <p>
 * This class uses StorageManager API and reflection to reliably detect
 * removable storage mount points that can be scanned for media files.
 */
public class RemovableStorageDetector {
    private static final String TAG = "RemovableStorageDetector";
    private static final boolean DEBUG = true;
    
    /**
     * Get all available storage volumes including internal and removable storage.
     *
     * @param context Application context
     * @return List of StorageInfo objects containing details about each storage volume
     */
    @NonNull
    public static List <StorageInfo> getAllStorageVolumes(@NonNull Context context) {
        List <StorageInfo> storageInfoList;
        
        storageInfoList = getStorageVolumesModern(context);
        
        logDebug("Found " + storageInfoList.size() + " storage volumes");
        return storageInfoList;
    }
    
    /**
     * Get only removable storage volumes (SD cards, USB drives).
     *
     * @param context Application context
     * @return List of StorageInfo objects for removable storage only
     */
    @NonNull
    public static List <StorageInfo> getRemovableStorageVolumes(@NonNull Context context) {
        List <StorageInfo> allVolumes = getAllStorageVolumes(context);
        List <StorageInfo> removableVolumes = new ArrayList <>();
        
        for (StorageInfo info : allVolumes) {
            if (info.isRemovable() && info.isMounted()) {
                removableVolumes.add(info);
                logDebug("Found removable storage: " + info.path());
            }
        }
        
        logDebug("Found " + removableVolumes.size() + " removable storage volumes");
        return removableVolumes;
    }
    
    /**
     * Get the primary removable storage path (typically the SD card).
     *
     * @param context Application context
     * @return File pointing to the SD card mount point, or null if not found
     */
    @Nullable
    public static File getPrimaryRemovableStoragePath(@NonNull Context context) {
        List <StorageInfo> removableVolumes = getRemovableStorageVolumes(context);
        
        if (removableVolumes.isEmpty()) {
            logDebug("No removable storage found");
            return null;
        }
        
        // Return the first removable storage found (usually the SD card)
        StorageInfo primaryRemovable = removableVolumes.get(0);
        File path = primaryRemovable.path();
        
        if (path != null && path.exists() && path.canRead()) {
            logDebug("Primary removable storage: " + path.getAbsolutePath());
            return path;
        }
        
        logDebug("Primary removable storage exists but not accessible: " + path);
        return null;
    }
    
    /**
     * Get all removable storage paths.
     *
     * @param context Application context
     * @return List of File objects pointing to removable storage mount points
     */
    @NonNull
    public static List <File> getAllRemovableStoragePaths(@NonNull Context context) {
        List <StorageInfo> removableVolumes = getRemovableStorageVolumes(context);
        List <File> paths = new ArrayList <>();
        
        for (StorageInfo info : removableVolumes) {
            File path = info.path();
            if (path != null && path.exists() && path.canRead()) {
                paths.add(path);
            }
        }
        
        return paths;
    }
    
    /**
     * Modern implementation using StorageManager API (API 24+).
     */
    @NonNull
    private static List <StorageInfo> getStorageVolumesModern(@NonNull Context context) {
        List <StorageInfo> storageInfoList = new ArrayList <>();
        
        try {
            StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
            if (storageManager == null) {
                logDebug("StorageManager is null");
                return storageInfoList;
            }
            
            List <StorageVolume> storageVolumes = storageManager.getStorageVolumes();
            logDebug("StorageManager returned " + storageVolumes.size() + " volumes");
            
            for (StorageVolume volume : storageVolumes) {
                StorageInfo info = extractStorageInfo(volume);
                if (info != null) {
                    storageInfoList.add(info);
                    logDebug("Volume: " + info);
                }
            }
        } catch (Exception e) {
            logError("Error getting storage volumes", e);
        }
        
        return storageInfoList;
    }
    
    /**
     * Extract storage information from StorageVolume using reflection for path access.
     */
    @Nullable
    private static StorageInfo extractStorageInfo(@NonNull StorageVolume volume) {
        try {
            // Get the mount path using reflection (getPath() is hidden but available)
            File path = null;
            String state = volume.getState();
            boolean isRemovable = volume.isRemovable();
            boolean isPrimary = volume.isPrimary();
            String description = volume.getDescription(null);
            String uuid;
            
            // Try to get the directory/path
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                // API 30+ has getDirectory()
                path = volume.getDirectory();
            } else {
                // API 24-29: use reflection to get path
                try {
                    Method getPathMethod = volume.getClass().getMethod("getPath");
                    String pathString = (String) getPathMethod.invoke(volume);
                    if (pathString != null) {
                        path = new File(pathString);
                    }
                } catch (Exception e) {
                    logDebug("Could not get path via reflection: " + e.getMessage());
                }
            }
            
            // Get UUID if available
            uuid = volume.getUuid();
            
            boolean isMounted = Environment.MEDIA_MOUNTED.equals(state) ||
                    Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
            
            return new StorageInfo(path, isRemovable, isPrimary, isMounted, description, uuid, state);
            
        } catch (Exception e) {
            logError("Error extracting storage info", e);
            return null;
        }
    }
    
    /**
     * Legacy implementation for older Android versions (API < 24).
     * Uses environment variables and getExternalFilesDirs().
     */
    @NonNull
    private static List <StorageInfo> getStorageVolumesLegacy(@NonNull Context context) {
        List <StorageInfo> storageInfoList = new ArrayList <>();
        
        try {
            // Get external file directories
            File[] externalFilesDirs = context.getExternalFilesDirs(null);
            
            for (File dir : externalFilesDirs) {
                if (dir == null) {
                    continue;
                }
                
                // Navigate up to get the storage root
                File storagePath = getStorageRoot(dir);
                
                if (storagePath != null && storagePath.exists()) {
                    boolean isRemovable = Environment.isExternalStorageRemovable(dir);
                    boolean isMounted = storagePath.canRead();
                    
                    StorageInfo info = new StorageInfo(
                            storagePath,
                            isRemovable,
                            false, // Can't determine primary easily
                            isMounted,
                            storagePath.getName(),
                            null,
                            isMounted ? Environment.MEDIA_MOUNTED : Environment.MEDIA_UNMOUNTED
                    );
                    
                    storageInfoList.add(info);
                }
            }
        } catch (Exception e) {
            logError("Error getting legacy storage volumes", e);
        }
        
        return storageInfoList;
    }
    
    /**
     * Navigate up from an app-specific directory to find the storage root.
     */
    @Nullable
    private static File getStorageRoot(@NonNull File dir) {
        String path = dir.getAbsolutePath();
        int androidIndex = path.indexOf("/Android");
        
        if (androidIndex > 0) {
            return new File(path.substring(0, androidIndex));
        }
        
        // Fallback: go up parent directories until we find a root-like directory
        File current = dir;
        while (current != null && current.getParent() != null) {
            String name = current.getName();
            if (name.equals("storage") || name.equals("mnt")) {
                return current.getParentFile();
            }
            current = current.getParentFile();
        }
        
        return dir;
    }
    
    private static void logDebug(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }
    
    private static void logError(String message, Throwable throwable) {
        Log.e(TAG, message, throwable);
    }
    
    /**
     * Data class containing information about a storage volume.
     */
    public record StorageInfo(
            File path,
            boolean isRemovable,
            boolean isPrimary,
            boolean isMounted,
            String description,
            String uuid,
            String state) {
        public StorageInfo(@Nullable File path, boolean isRemovable, boolean isPrimary,
                boolean isMounted, @Nullable String description,
                @Nullable String uuid, @Nullable String state) {
            this.path = path;
            this.isRemovable = isRemovable;
            this.isPrimary = isPrimary;
            this.isMounted = isMounted;
            this.description = description;
            this.uuid = uuid;
            this.state = state;
        }
        
        @Override
        @Nullable
        public File path() {
            return path;
        }
        
        @Override
        @Nullable
        public String description() {
            return description;
        }
        
        @Override
        @Nullable
        public String uuid() {
            return uuid;
        }
        
        @Override
        @Nullable
        public String state() {
            return state;
        }
        
        /**
         * Get total space in bytes (returns 0 if unavailable).
         */
        public long getTotalSpace() {
            return path != null ? path.getTotalSpace() : 0;
        }
        
        /**
         * Get free space in bytes (returns 0 if unavailable).
         */
        public long getFreeSpace() {
            return path != null ? path.getFreeSpace() : 0;
        }
        
        /**
         * Get usable space in bytes (returns 0 if unavailable).
         */
        public long getUsableSpace() {
            return path != null ? path.getUsableSpace() : 0;
        }
        
        /**
         * Check if the storage is accessible for reading.
         */
        public boolean isAccessible() {
            return path != null && path.exists() && path.canRead();
        }
        
        /**
         * Check if the storage is writable.
         */
        public boolean isWritable() {
            return path != null && path.exists() && path.canWrite();
        }
        
        @NonNull
        @Override
        public String toString() {
            return "StorageInfo{" +
                    "path=" + (path != null ? path.getAbsolutePath() : "null") +
                    ", isRemovable=" + isRemovable +
                    ", isPrimary=" + isPrimary +
                    ", isMounted=" + isMounted +
                    ", description='" + description + '\'' +
                    ", uuid='" + uuid + '\'' +
                    ", state='" + state + '\'' +
                    ", accessible=" + isAccessible() +
                    ", writable=" + isWritable() +
                    '}';
        }
    }
}