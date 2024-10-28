package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Folder implements Parcelable {
    private int hashcode;
    private String name;
    private String path;
    private int count;
    private boolean isNomedia;
    
    public Folder(int hashcode, String name, String path, int count, boolean isNomedia) {
        this.hashcode = hashcode;
        this.name = name;
        this.path = path;
        this.count = count;
        this.isNomedia = isNomedia;
    }
    
    public Folder() {
    }
    
    protected Folder(Parcel in) {
        hashcode = in.readInt();
        name = in.readString();
        path = in.readString();
        count = in.readInt();
        isNomedia = in.readByte() != 0;
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hashcode);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeInt(count);
        dest.writeByte((byte) (isNomedia ? 1 : 0));
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Folder> CREATOR = new Creator <>() {
        @Override
        public Folder createFromParcel(Parcel in) {
            return new Folder(in);
        }
        
        @Override
        public Folder[] newArray(int size) {
            return new Folder[size];
        }
    };
    
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
    
    public String getPath() {
        return path;
    }
    
    public void setPath(String path) {
        this.path = path;
    }
    
    public int getCount() {
        return count;
    }
    
    public void setCount(int count) {
        this.count = count;
    }
    
    public boolean isNomedia() {
        return isNomedia;
    }
    
    public void setNomedia(boolean nomedia) {
        isNomedia = nomedia;
    }
}
