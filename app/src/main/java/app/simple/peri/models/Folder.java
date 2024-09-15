package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Folder implements Parcelable {
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
    
    protected Folder(Parcel in) {
        hashcode = in.readInt();
        name = in.readString();
        uri = in.readString();
        count = in.readInt();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(hashcode);
        dest.writeString(name);
        dest.writeString(uri);
        dest.writeInt(count);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Folder> CREATOR = new Creator <Folder>() {
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
