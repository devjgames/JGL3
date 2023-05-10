package org.jgl3;

import java.io.Serializable;

import org.joml.Intersectionf;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class BoundingBox implements Serializable {

    private static final long serialVersionUID = 1234567L;
    
    private final Vector3f min = new Vector3f();
    private final Vector3f max = new Vector3f();
    private final Vector2f result = new Vector2f();

    public BoundingBox() {
        clear();
    }

    public BoundingBox(float x1, float y1, float z1, float x2, float y2, float z2) {
        set(x1, y1, z1, x2, y2, z2);
    }

    public BoundingBox(Vector3f min, Vector3f max) {
        set(min, max);
    }

    public BoundingBox(BoundingBox b) {
        set(b);
    }

    public boolean isEmpty() {
        return min.x > max.x || min.y > max.y || min.z > max.z;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public Vector3f getCenter(Vector3f center) {
        if(!isEmpty()) {
            max.add(min, center).mul(0.5f);
        }
        return center;
    }

    public Vector3f getSize(Vector3f size) {
        if(!isEmpty()) {
            max.sub(min, size);
        }
        return size;
    }

    public BoundingBox clear() {
        min.set(1, 1, 1).mul(Float.MAX_VALUE);
        min.negate(max);
        return this;
    }

    public BoundingBox set(float x1, float y1, float z1, float x2, float y2, float z2) {
        min.set(x1, y1, z1);
        max.set(x2, y2, z2);
        return this;
    }

    public BoundingBox set(Vector3f min, Vector3f max) {
        this.min.set(min);
        this.max.set(max);
        return this;
    }

    public BoundingBox set(BoundingBox b) {
        min.set(b.min);
        max.set(b.max);
        return this;
    }

    public BoundingBox add(float x, float y, float z) {
        min.x = Math.min(min.x, x);
        min.y = Math.min(min.y, y);
        min.z = Math.min(min.z, z);
        max.x = Math.max(max.x, x);
        max.y = Math.max(max.y, y);
        max.z = Math.max(max.z, z);
        return this;
    }

    public BoundingBox add(Vector3f p) {
        return add(p.x, p.y, p.z);
    }

    public BoundingBox add(BoundingBox b) {
        if(!b.isEmpty()) {
            add(b.min);
            add(b.max);
        }
        return this;
    }

    public BoundingBox buffer(float x, float y, float z) {
        if(!isEmpty()) {
            min.sub(x, y, z);
            max.add(x, y, z);
        }
        return this;
    }

    public BoundingBox buffer(Vector3f p) {
        return buffer(p.x, p.y, p.z);
    }

    public boolean contains(float x, float y, float z) {
        if(!isEmpty()) {
            return 
                x >= min.x && x <= max.x &&
                y >= min.y && y <= max.y &&
                z >= min.z && z <= max.z;
        }
        return false;
    }

    public boolean contains(Vector3f p) {
        return contains(p.x, p.y, p.z);
    }

    public boolean touches(BoundingBox b) {
        if(!isEmpty() && !b.isEmpty()) {
            return Intersectionf.testAabAab(min, max, b.min, b.max);
        }
        return false;
    }

    public boolean intersects(Vector3f origin, Vector3f direction, float[] time) {
        if(!isEmpty()) {
            if(Intersectionf.intersectRayAab(origin, direction, min, max, result)) {
                if(result.x >= 0) {
                    time[0] = result.x;
                    return true;
                }
            }
        }
        return false;
    }

    public BoundingBox transform(Matrix4f matrix) {
        if(!isEmpty()) {
            matrix.transformAab(min, max, min, max);
        }
        return this;
    }

    @Override
    public String toString() {
        return "" + min + " -> " + max;
    }
}
