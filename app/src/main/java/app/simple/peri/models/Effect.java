package app.simple.peri.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity (tableName = "effects")
public class Effect implements Parcelable {
    @PrimaryKey (autoGenerate = true)
    @ColumnInfo (name = "id")
    private int id;
    
    @ColumnInfo (name = "blur_value")
    private float blurValue;
    
    @ColumnInfo (name = "brightness_value")
    private float brightnessValue;
    
    @ColumnInfo (name = "contrast_value")
    private float contrastValue;
    
    @ColumnInfo (name = "saturation_value")
    private float saturationValue;
    
    @ColumnInfo (name = "hue_red_value")
    private float hueRedValue;
    
    @ColumnInfo (name = "hue_green_value")
    private float hueGreenValue;
    
    @ColumnInfo (name = "hue_blue_value")
    private float hueBlueValue;
    
    @ColumnInfo (name = "scale_red_value")
    private float scaleRedValue;
    
    @ColumnInfo (name = "scale_green_value")
    private float scaleGreenValue;
    
    @ColumnInfo (name = "scale_blue_value")
    private float scaleBlueValue;
    
    public Effect(float blurValue,
            float brightnessValue,
            float contrastValue,
            float saturationValue,
            float hueRedValue,
            float hueGreenValue,
            float hueBlueValue,
            float scaleRedValue,
            float scaleGreenValue,
            float scaleBlueValue) {
        this.blurValue = blurValue;
        this.brightnessValue = brightnessValue;
        this.contrastValue = contrastValue;
        this.saturationValue = saturationValue;
        this.hueRedValue = hueRedValue;
        this.hueGreenValue = hueGreenValue;
        this.hueBlueValue = hueBlueValue;
        this.scaleRedValue = scaleRedValue;
        this.scaleGreenValue = scaleGreenValue;
        this.scaleBlueValue = scaleBlueValue;
    }
    
    protected Effect(Parcel in) {
        id = in.readInt();
        blurValue = in.readFloat();
        brightnessValue = in.readFloat();
        contrastValue = in.readFloat();
        saturationValue = in.readFloat();
        hueRedValue = in.readFloat();
        hueGreenValue = in.readFloat();
        hueBlueValue = in.readFloat();
        scaleRedValue = in.readFloat();
        scaleGreenValue = in.readFloat();
        scaleBlueValue = in.readFloat();
    }
    
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeFloat(blurValue);
        dest.writeFloat(brightnessValue);
        dest.writeFloat(contrastValue);
        dest.writeFloat(saturationValue);
        dest.writeFloat(hueRedValue);
        dest.writeFloat(hueGreenValue);
        dest.writeFloat(hueBlueValue);
        dest.writeFloat(scaleRedValue);
        dest.writeFloat(scaleGreenValue);
        dest.writeFloat(scaleBlueValue);
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    
    public static final Creator <Effect> CREATOR = new Creator <Effect>() {
        @Override
        public Effect createFromParcel(Parcel in) {
            return new Effect(in);
        }
        
        @Override
        public Effect[] newArray(int size) {
            return new Effect[size];
        }
    };
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public float getBlurValue() {
        return blurValue;
    }
    
    public void setBlurValue(float blurValue) {
        this.blurValue = blurValue;
    }
    
    public float getBrightnessValue() {
        return brightnessValue;
    }
    
    public void setBrightnessValue(float brightnessValue) {
        this.brightnessValue = brightnessValue;
    }
    
    public float getContrastValue() {
        return contrastValue;
    }
    
    public void setContrastValue(float contrastValue) {
        this.contrastValue = contrastValue;
    }
    
    public float getSaturationValue() {
        return saturationValue;
    }
    
    public void setSaturationValue(float saturationValue) {
        this.saturationValue = saturationValue;
    }
    
    public float getHueRedValue() {
        return hueRedValue;
    }
    
    public void setHueRedValue(float hueRedValue) {
        this.hueRedValue = hueRedValue;
    }
    
    public float getHueGreenValue() {
        return hueGreenValue;
    }
    
    public void setHueGreenValue(float hueGreenValue) {
        this.hueGreenValue = hueGreenValue;
    }
    
    public float getHueBlueValue() {
        return hueBlueValue;
    }
    
    public void setHueBlueValue(float hueBlueValue) {
        this.hueBlueValue = hueBlueValue;
    }
    
    public float getScaleRedValue() {
        return scaleRedValue;
    }
    
    public void setScaleRedValue(float scaleRedValue) {
        this.scaleRedValue = scaleRedValue;
    }
    
    public float getScaleGreenValue() {
        return scaleGreenValue;
    }
    
    public void setScaleGreenValue(float scaleGreenValue) {
        this.scaleGreenValue = scaleGreenValue;
    }
    
    public float getScaleBlueValue() {
        return scaleBlueValue;
    }
    
    public void setScaleBlueValue(float scaleBlueValue) {
        this.scaleBlueValue = scaleBlueValue;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Effect{" +
                "id=" + id +
                ", blurValue=" + blurValue +
                ", brightnessValue=" + brightnessValue +
                ", contrastValue=" + contrastValue +
                ", saturationValue=" + saturationValue +
                ", hueRedValue=" + hueRedValue +
                ", hueGreenValue=" + hueGreenValue +
                ", hueBlueValue=" + hueBlueValue +
                ", scaleRedValue=" + scaleRedValue +
                ", scaleGreenValue=" + scaleGreenValue +
                ", scaleBlueValue=" + scaleBlueValue +
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
        
        Effect effect = (Effect) o;
        return getId() == effect.getId() &&
                Float.compare(getBlurValue(), effect.getBlurValue()) == 0 &&
                Float.compare(getBrightnessValue(), effect.getBrightnessValue()) == 0 &&
                Float.compare(getContrastValue(), effect.getContrastValue()) == 0 &&
                Float.compare(getSaturationValue(), effect.getSaturationValue()) == 0 &&
                Float.compare(getHueRedValue(), effect.getHueRedValue()) == 0 &&
                Float.compare(getHueGreenValue(), effect.getHueGreenValue()) == 0 &&
                Float.compare(getHueBlueValue(), effect.getHueBlueValue()) == 0 &&
                Float.compare(getScaleRedValue(), effect.getScaleRedValue()) == 0 &&
                Float.compare(getScaleGreenValue(), effect.getScaleGreenValue()) == 0 &&
                Float.compare(getScaleBlueValue(), effect.getScaleBlueValue()) == 0;
    }
    
    @Override
    public int hashCode() {
        int result = getId();
        result = 31 * result + Float.hashCode(getBlurValue());
        result = 31 * result + Float.hashCode(getBrightnessValue());
        result = 31 * result + Float.hashCode(getContrastValue());
        result = 31 * result + Float.hashCode(getSaturationValue());
        result = 31 * result + Float.hashCode(getHueRedValue());
        result = 31 * result + Float.hashCode(getHueGreenValue());
        result = 31 * result + Float.hashCode(getHueBlueValue());
        result = 31 * result + Float.hashCode(getScaleRedValue());
        result = 31 * result + Float.hashCode(getScaleGreenValue());
        result = 31 * result + Float.hashCode(getScaleBlueValue());
        return result;
    }
}
