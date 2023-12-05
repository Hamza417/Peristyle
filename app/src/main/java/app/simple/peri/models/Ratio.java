package app.simple.peri.models;

public class Ratio {
    
    private final float width;
    private final float height;
    
    public Ratio(float width, float height) {
        this.width = width;
        this.height = height;
    }
    
    public float getWidth() {
        return width;
    }
    
    public float getHeight() {
        return height;
    }
}
