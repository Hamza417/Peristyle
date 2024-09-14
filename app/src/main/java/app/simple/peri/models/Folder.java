package app.simple.peri.models;

import android.net.Uri;

public class Folder {
    private int hashcode;
    private String name;
    private String uri;
    private int count;
    
    public Folder(int hashcode, String name, String uri, int count) {
        this.hashcode = hashcode;
        this.name = name;
        this.uri = uri;
        this.count = count;
    }
    
    public Folder() {
    }
    
    public int getHashcode() {
        return hashcode;
    }
    
    public void setHashcode(int hashcode) {
        this.hashcode = hashcode;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getUri() {
        return uri;
    }
    
    public void setUri(String uri) {
        this.uri = uri;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
}
