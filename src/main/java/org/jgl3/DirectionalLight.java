package org.jgl3;

import org.joml.Vector3f;

public class DirectionalLight extends Light {
    
    private final Vector3f direction = new Vector3f();

    public Vector3f getLightDirection() {
        return getRotation().getRow(2, direction);
    }
}
