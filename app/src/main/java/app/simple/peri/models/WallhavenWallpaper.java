package app.simple.peri.models;

public class WallhavenWallpaper {
    private String id;
    private String url;
    private String thumbnailUrl;
    private String originalUrl;
    private String category;
    private String resolution;
    private String aspectRatio;
    private String uploader;
    
    public WallhavenWallpaper(String id, String url, String thumbnailUrl, String originalUrl, String category, String resolution, String aspectRatio, String uploader) {
        this.id = id;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.originalUrl = originalUrl;
        this.category = category;
        this.resolution = resolution;
        this.aspectRatio = aspectRatio;
        this.uploader = uploader;
    }
    
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
}
