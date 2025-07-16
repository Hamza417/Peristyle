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
