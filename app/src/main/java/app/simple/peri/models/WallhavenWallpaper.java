package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class WallhavenWallpaper implements Parcelable {
    private String id;
    private String url;
    private String thumbnailUrl;
    private String originalUrl;
    private String category;
    private String resolution;
    private String aspectRatio;
    private String uploader;
    private long fileSize;
    
    public WallhavenWallpaper(String id, String url, String thumbnailUrl, String originalUrl, String category, String resolution, String aspectRatio, String uploader, long fileSize) {
        this.id = id;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.originalUrl = originalUrl;
        this.category = category;
        this.resolution = resolution;
        this.aspectRatio = aspectRatio;
        this.uploader = uploader;
        this.fileSize = fileSize;
    }
    
    protected WallhavenWallpaper(Parcel in) {
        id = in.readString();
        url = in.readString();
        thumbnailUrl = in.readString();
        originalUrl = in.readString();
        category = in.readString();
        resolution = in.readString();
        aspectRatio = in.readString();
        uploader = in.readString();
        fileSize = in.readLong();
    }
    
    public static final Creator <WallhavenWallpaper> CREATOR = new Creator <WallhavenWallpaper>() {
        @Override
        public WallhavenWallpaper createFromParcel(Parcel in) {
            return new WallhavenWallpaper(in);
        }
        
        @Override
        public WallhavenWallpaper[] newArray(int size) {
            return new WallhavenWallpaper[size];
        }
    };
    
    // Getters and setters
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
    
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
    
    public String getOriginalUrl() {
        return originalUrl;
    }
    
    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getResolution() {
        return resolution;
    }
    
    public void setResolution(String resolution) {
        this.resolution = resolution;
    }
    
    public String getAspectRatio() {
        return aspectRatio;
    }
    
    public void setAspectRatio(String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
    
    public String getUploader() {
        return uploader;
    }
    
    public void setUploader(String uploader) {
        this.uploader = uploader;
    }
    
    public long getFileSize() {
        return fileSize;
    }
    
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        
        dest.writeString(id);
        dest.writeString(url);
        dest.writeString(thumbnailUrl);
        dest.writeString(originalUrl);
        dest.writeString(category);
        dest.writeString(resolution);
        dest.writeString(aspectRatio);
        dest.writeString(uploader);
        dest.writeLong(fileSize);
    }
}
