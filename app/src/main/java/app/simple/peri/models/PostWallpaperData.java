package app.simple.peri.models;

public class PostWallpaperData {
    
    private long oldSize = 0;
    private long newSize = 0;
    private int oldWidth = 0;
    private int oldHeight = 0;
    private int newWidth = 0;
    private int newHeight = 0;
    private String path = "";
    
    public PostWallpaperData() {
    }
    
    public PostWallpaperData(long oldSize, long newSize, int oldWidth, int oldHeight, int newWidth, int newHeight, String path) {
        this.oldSize = oldSize;
        this.newSize = newSize;
        this.oldWidth = oldWidth;
        this.oldHeight = oldHeight;
        this.newWidth = newWidth;
        this.newHeight = newHeight;
        this.path = path;
    }
    
    public long getOldSize() {
        return oldSize;
    }
    
    public void setOldSize(long oldSize) {
        this.oldSize = oldSize;
    }
    
    public long getNewSize() {
        return newSize;
    }
    
    public void setNewSize(long newSize) {
        this.newSize = newSize;
    }
    
    public int getOldWidth() {
        return oldWidth;
    }
    
    public void setOldWidth(int oldWidth) {
        this.oldWidth = oldWidth;
    }
    
    public int getOldHeight() {
        return oldHeight;
    }
    
    public void setOldHeight(int oldHeight) {
        this.oldHeight = oldHeight;
    }
    
    public int getNewWidth() {
        return newWidth;
    }
    
    public void setNewWidth(int newWidth) {
        this.newWidth = newWidth;
    }
    
    public int getNewHeight() {
        return newHeight;
    }
    
    public void setNewHeight(int newHeight) {
        this.newHeight = newHeight;
    }
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public float getNewAspectRatio() {
        return (float) newWidth / newHeight;
    }
    
    public float getOldAspectRatio() {
        return (float) oldWidth / oldHeight;
    }
    
    @Override
    public String toString() {
        return "PostWallpaperData{" +
                "oldSize=" + oldSize +
                ", newSize=" + newSize +
                ", oldWidth=" + oldWidth +
                ", oldHeight=" + oldHeight +
                ", newWidth=" + newWidth +
                ", newHeight=" + newHeight +
                ", path='" + path + '\'' +
                '}';
    }
}
