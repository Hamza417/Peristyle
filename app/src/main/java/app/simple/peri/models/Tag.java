package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashSet;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import app.simple.peri.database.converters.IDListConverter;

@Entity (tableName = "tags")
public class Tag implements Parcelable {
    @PrimaryKey
    @NonNull
    @ColumnInfo (name = "name")
    private String name;
    
    @ColumnInfo (name = "ids")
    @TypeConverters (IDListConverter.class)
    private HashSet <String> sum;
    
    public Tag(@NonNull String name, HashSet <String> sum) {
        this.name = name;
        this.sum = sum != null ? new HashSet <>(sum) : new HashSet <>();
    }
    
    protected Tag(Parcel in) {
        name = Objects.requireNonNull(in.readString());
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Tag> CREATOR = new Creator <Tag>() {
        @Override
        public Tag createFromParcel(Parcel in) {
            return new Tag(in);
        }
        
        @Override
        public Tag[] newArray(int size) {
            return new Tag[size];
        }
    };
    
    @NonNull
    public String getName() {
        return name;
    }
    
    public void setName(@NonNull String name) {
        this.name = name;
    }
    
    public HashSet <String> getSum() {
        return sum;
    }
    
    public void setSum(HashSet <String> sum) {
        this.sum = sum;
    }
    
    public void addSum(String sum) {
        this.sum.add(sum);
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Tag{" +
                ", tag='" + name + '\'' +
                ", sum=" + sum +
                '}';
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        
        Tag tag1 = (Tag) o;
        
        if (!Objects.equals(name, tag1.name)) {
            return false;
        }
        return Objects.equals(sum, tag1.sum);
    }
    
    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (sum != null ? sum.hashCode() : 0);
        return result;
    }
}
