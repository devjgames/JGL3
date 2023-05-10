package org.jgl3;

import java.io.Serializable;

import org.joml.GeometryUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public final class Triangle implements Serializable {

    private static final long serialVersionUID = 1234567L;
    
    private final Vector3f p1 = new Vector3f(1, 0, 0);
    private final Vector3f p2 = new Vector3f(0, 1, 0);
    private final Vector3f p3 = new Vector3f(0, 0, 0);
    private final Vector3f n = new Vector3f();
    private float d = 0;
    private int tag = 1;
    private Object data = null;
    private final Vector3f point = new Vector3f();
    private final Vector3f a = new Vector3f();
    private final Vector3f b = new Vector3f();
    private final Vector3f n2 = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f ab = new Vector3f();
    private final Vector3f ap = new Vector3f();
    private final Vector3f c = new Vector3f();

    public Triangle() {
        calcPlane();
    }

    public Vector3f getP1() {
        return p1;
    }

    public Vector3f getP2() {
        return p2;
    }

    public Vector3f getP3() {
        return p3;
    }

    public Vector3f getNormal() {
        return n;
    }

    public float getDistance() {
        return d;
    }

    public int getTag() {
        return tag;
    }

    public Triangle setTag(int tag) {
        this.tag = tag;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Triangle setData(Object data) {
        this.data = data;
        return this;
    }

    public Vector3f getPoint(int i) {
        if(i == 1) {
            return p2;
        } else if(i == 2) {
            return p3;
        } else {
            return p1;
        }
    }

    public Triangle calcPlane() {
        GeometryUtils.normal(p1, p2, p3, n);
        d = -n.dot(p1);
        return this;
    }

    public Triangle transform(Matrix4f matrix) {
        p1.mulPosition(matrix);
        p2.mulPosition(matrix);
        p3.mulPosition(matrix);
        calcPlane();
        return this;
    }

    public Triangle set(Triangle triangle) {
        p1.set(triangle.p1);
        p2.set(triangle.p2);
        p3.set(triangle.p3);
        n.set(triangle.n);
        d = triangle.d;
        tag = triangle.tag;
        data = triangle.data;
        return this;
    }

    public boolean contains(Vector3f point, float buffer) {
        for(int i = 0; i != 3; i++) {
            a.set(getPoint(i));
            b.set(getPoint(i + 1));
            b.sub(a, n2).cross(n).normalize();
            v.set(n2).mul(buffer);
            a.add(v);
            
            float d2 = -a.dot(n2);
            float s = point.dot(n2) + d2;

            if(s > 0) {
                return false;
            }
        }
        return true;
    }

    public boolean intersectsPlane(Vector3f origin, Vector3f direction, float[] time) {
        float t = n.dot(direction);

        if(Math.abs(t) > 0.0000001) {
            t = (-d - n.dot(origin)) / t;
            if(t >= 0 && t < time[0]) {
                time[0] = t;
                return true;
            }
        }
        return false;
    }

    public boolean intersects(Vector3f origin, Vector3f direction, float buffer, float[] time) {
        float t = time[0];

        if(intersectsPlane(origin, direction, time)) {
            if(contains(direction.mul(time[0], point).add(origin), buffer)) {
                return true;
            }
        }
        time[0] = t;

        return false;
    }

    public Triangle closestPoint(Vector3f point, Vector3f closest) {
        float min = Float.MAX_VALUE;

        closest.set(p1);
        for(int i = 0; i != 3; i++) {
            a.set(getPoint(i));
            b.set(getPoint(i + 1));
            b.sub(a, ab);
            point.sub(a, ap);

            float s = ab.dot(ap);

            c.set(a);
            if(s >= 0) {
                s /= ab.lengthSquared();
                if(s < 1) {
                    c.set(ab.mul(s)).add(a);
                } else {
                    c.set(b);
                }
            }
            point.sub(c, v);
            if(v.length() < min) {
                closest.set(c);
                min = v.length();
            }
        }
        return this;
    }
}
