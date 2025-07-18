package app.simple.peri.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

public class WallhavenResponse {
    
    @SerializedName ("data")
    private List <WallhavenItem> data;
    
    @SerializedName ("meta")
    private Meta meta;
    
    public List <WallhavenItem> getData() {
        return data;
    }
    
    public Meta getMeta() {
        return meta;
    }
    
    public static class WallhavenItem {
        @SerializedName ("id")
        private String id;
        
        @SerializedName ("url")
        private String url;
        
        @SerializedName ("path")
        private String path;
        
        @SerializedName ("thumbs")
        private Map <String, String> thumbs;
        
        @SerializedName ("category")
        private String category;
        
        @SerializedName ("resolution")
        private String resolution;
        
        @SerializedName ("ratio")
        private String ratio;
        
        @SerializedName ("uploader")
        private Uploader uploader;
        
        @SerializedName ("file_size")
        private long fileSize;
        
        @SerializedName ("colors")
        private List <String> colors;
        
        @SerializedName ("views")
        private int viewsCount;
        
        public String getId() {
            return id;
        }
        
        public String getUrl() {
            return url;
        }
        
        public String getPath() {
            return path;
        }
        
        public Map <String, String> getThumbs() {
            return thumbs;
        }
        
        public String getCategory() {
            return category;
        }
        
        public String getResolution() {
            return resolution;
        }
        
        public String getRatio() {
            return ratio;
        }
        
        public Uploader getUploader() {
            return uploader;
        }
        
        public long getFileSize() {
            return fileSize;
        }
        
        public void setId(String id) {
            this.id = id;
        }
        
        public void setUrl(String url) {
            this.url = url;
        }
        
        public void setPath(String path) {
            this.path = path;
        }
        
        public void setThumbs(Map <String, String> thumbs) {
            this.thumbs = thumbs;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public void setResolution(String resolution) {
            this.resolution = resolution;
        }
        
        public void setRatio(String ratio) {
            this.ratio = ratio;
        }
        
        public void setUploader(Uploader uploader) {
            this.uploader = uploader;
        }
        
        public void setFileSize(long fileSize) {
            this.fileSize = fileSize;
        }
        
        public List <String> getColors() {
            return colors;
        }
        
        public void setColors(List <String> colors) {
            this.colors = colors;
        }
        
        public int getViewsCount() {
            return viewsCount;
        }
        
        public void setViewsCount(int viewsCount) {
            this.viewsCount = viewsCount;
        }
    }
    
    public static class Uploader {
        @SerializedName ("username")
        private String username;
        
        public String getUsername() {
            return username;
        }
    }
    
    public static class Meta {
        @SerializedName ("current_page")
        private int currentPage;
        
        @SerializedName ("last_page")
        private int lastPage;
        
        @SerializedName ("total")
        private int total;
        
        public int getCurrentPage() {
            return currentPage;
        }
        
        public int getLastPage() {
            return lastPage;
        }
        
        public int getTotal() {
            return total;
        }
    }
}
