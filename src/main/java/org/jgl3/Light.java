package org.jgl3;

import org.joml.Vector4f;

public abstract class Light extends Node {
    
    private final Vector4f color = new Vector4f(1, 1, 1, 1);

    public Vector4f getColor() {
        return color;
    }
}
