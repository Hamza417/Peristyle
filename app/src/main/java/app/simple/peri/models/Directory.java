package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import androidx.annotation.NonNull;

public class Directory implements Parcelable {
    
    private String name;
    private String uri;
    
    private List <Wallpaper> wallpapers;
    private List <Directory> directories; // Sub-folders
    
    public Directory(String name, String uri, List <Wallpaper> wallpapers, List <Directory> directories) {
        this.name = name;
        this.uri = uri;
        this.wallpapers = wallpapers;
        this.directories = directories;
    }
    
    public Directory() {
        /* Empty constructor */
    }
    
    protected Directory(Parcel in) {
        name = in.readString();
        uri = in.readString();
        wallpapers = in.createTypedArrayList(Wallpaper.CREATOR);
        directories = in.createTypedArrayList(Directory.CREATOR);
    }
    
    public static final Creator <Directory> CREATOR = new Creator <>() {
        @Override
        public Directory createFromParcel(Parcel in) {
            return new Directory(in);
        }
        
        @Override
        public Directory[] newArray(int size) {
            return new Directory[size];
        }
    };
    
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
    
    public List <Wallpaper> getWallpapers() {
        return wallpapers;
    }
    
    public int getWallpapersCount() {
        return wallpapers.size();
    }
    
    public void setWallpapers(List <Wallpaper> wallpapers) {
        this.wallpapers = wallpapers;
    }
    
    public List <Directory> getDirectories() {
        return directories;
    }
    
    public void setDirectories(List <Directory> directories) {
        this.directories = directories;
    }
    
    public int getDirectoriesCount() {
        return directories.size();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(uri);
        dest.writeTypedList(wallpapers);
        dest.writeTypedList(directories);
    }
}
