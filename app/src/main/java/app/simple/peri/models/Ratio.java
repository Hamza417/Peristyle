package app.simple.peri.models;

import androidx.annotation.NonNull;

public class Ratio {
    
    private final float horizontal;
    private final float vertical;
    
    public Ratio(float horizontal, float vertical) {
        this.horizontal = horizontal;
        this.vertical = vertical;
    }
    
    public float getHorizontal() {
        return horizontal;
    }
    
    public float getVertical() {
        return vertical;
    }
    
    @NonNull
    @Override
    public String toString() {
        return "Ratio{" +
                "horizontal=" + horizontal +
                ", vertical=" + vertical +
                '}';
    }
}
