package org.jgl3.scene;

import java.io.Serializable;

import org.jgl3.GFX;
import org.jgl3.Game;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public final class Camera implements Serializable {

    private static final long serialVersionUID = 1234567L;
    
    private final Vector3f eye = new Vector3f(100, 100, 100);
    private final Vector3f target = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f r = new Vector3f();
    private final Vector3f f = new Vector3f();
    private final Vector3f offset = new Vector3f();
    private float fieldOfView = 60;
    private float zNear = 0.1f;
    private float zFar = 10000;
    private final Matrix4f projection = new Matrix4f();
    private final Matrix4f view = new Matrix4f();
    private final Matrix4f matrix = new Matrix4f();

    public Vector3f getEye() {
        return eye;
    }

    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getUp() {
        return up;
    }

    public Vector3f getOffset() {
        return eye.sub(target, offset);
    }

    public float getFieldOfView() {
        return fieldOfView;
    }

    public Camera setFieldOfView(float fieldOfView) {
        this.fieldOfView = fieldOfView;
        return this;
    }

    public float getZNear() {
        return zNear;
    }

    public Camera setZNear(float zNear) {
        this.zNear = zNear;
        return this;
    }

    public float getZFar() {
        return zFar;
    }

    public Camera setZFar(float zFar) {
        this.zFar = zFar;
        return this;
    }

    public Matrix4f getProjection() {
        return projection;
    }

    public Matrix4f getView() {
        return view;
    }

    public Camera calcTransforms(float aspectRatio) {
        projection.identity().perspective((float)Math.toRadians(fieldOfView), aspectRatio, zNear, zFar);
        view.identity().lookAt(eye, target, up);
        return this;
    }

    public Camera rotateAroundEye(float dx, float dy) {
        matrix.identity().rotate(dx, 0, 1, 0);
        target.sub(eye, f).normalize().cross(up, r).mulDirection(matrix).normalize();
        f.mulDirection(matrix).normalize();
        matrix.identity().rotate(dy, r);
        r.cross(f, up).mulDirection(matrix).normalize();
        f.mulDirection(matrix).normalize();
        eye.add(f, target);
        return this;
    }

    public Camera rotateAroundTarget(float dx, float dy) {
        matrix.identity().rotate(dx, 0, 1, 0);
        eye.sub(target, f).cross(up, r).mulDirection(matrix).normalize();
        f.mulDirection(matrix);
        matrix.identity().rotate(dy, r);
        r.cross(f, up).mulDirection(matrix).normalize();
        target.add(f.mulDirection(matrix), eye);
        return this;
    }

    public Vector3f move(Vector3f point, float dx, float dy) {
        float dl = Vector2f.length(dx, dy);

        if(dl > 0.1) {
            eye.sub(target, offset);
            f.set(offset).mul(-1, 0, -1);
            if(f.length() > 0.0000001) {
                f.normalize().cross(0, 1, 0, r).normalize().mul(dx);
                point.add(f.mul(dy).add(r));
            }
            target.add(offset, eye);
        }
        return point;
    }

    public Vector3f move(Vector3f point, float dy) {
        eye.sub(target, offset);
        point.y += dy;
        target.add(offset, eye);
        return point;
    }

    public Camera zoom(float amount) {
        Vector3f x = getOffset();
        float len = x.length();

        target.add(x.normalize().mul(len + amount), eye);

        return this;
    }

    public Vector3f unProject(float z, Game game, Vector3f point) {
        return unProject(game.getMouseX(), game.getHeight() - game.getMouseY() - 1, z, 0, 0, game.getWidth(), game.getHeight(), point);
    }

    public Vector3f unProject(float wx, float wy, float wz, int vx, int vy, int vw, int vh, Vector3f point) {
        return GFX.unProject(wx, wy, wz, vx, vy, vw, vh, projection, view, point);
    }
}
