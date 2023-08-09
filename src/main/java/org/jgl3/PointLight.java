package org.jgl3;

public class PointLight extends Light {
    
    private float range = 300;

    public float getRange() {
        return range;
    }

    public void setRange(float range) {
        this.range = range;
    }
}
