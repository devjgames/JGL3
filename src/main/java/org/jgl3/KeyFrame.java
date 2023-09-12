package org.jgl3;

public final class KeyFrame {

    private final String name;
    private final float[] vertices;
    private final BoundingBox bounds = new BoundingBox();

    public KeyFrame(String name, float[] vertices) {
        this.name = name;
        this.vertices = vertices.clone();

        for(int i = 0; i != vertices.length; i += 8) {
            float x = vertices[i + 0];
            float y = vertices[i + 1];
            float z = vertices[i + 2];

            bounds.add(x, y, z);
        }
    }

    public String getName() {
        return name;
    }

    public float[] getVertices() {
        return vertices;
    }

    public BoundingBox getBounds() {
        return bounds;
    }
}
