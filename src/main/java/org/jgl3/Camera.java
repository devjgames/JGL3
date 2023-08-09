package org.jgl3;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera extends Node {
    
    private final Matrix4f projection = new Matrix4f();
    private boolean active = false;
    
    public Matrix4f getProjection() {
        return projection;
    }

    public boolean isActive() {
        return active;
    }

    public void activate() throws Exception {
        Node root = getRoot();

        root.traverse(null, (scene, node) -> {
            if(node instanceof Camera) {
                Camera camera = (Camera)node;

                camera.active = camera.getID().equals(getID());
            }
            return true;
        });
    }

    @Override
    public void calcLocalModel() {
        Vector3f p = getPosition();

        getLocalModel().set(getRotation()).translate(-p.x, -p.y, -p.z);
    }

    public void calcProjection(Size size) {
        getProjection().identity().perspective((float)Math.toRadians(60), size.getAspectRatio(), 1, 10000);
    }

    public void look(Vector3f direction, Vector3f up) {
        look(direction.x, direction.y, direction.z, up.x, up.y, up.z);
    }

    public void look(float directionX, float directionY, float directionZ, float upX, float upY, float upZ) {
        float length = Vector3f.length(directionX, directionY, directionZ);

        directionX /= length;
        directionY /= length;
        directionZ /= length;

        length = Vector3f.length(upX, upY, upZ);
        upX /= length;
        upY /= length;
        upZ /= length;

        float rX = directionY * upZ - directionZ * upY;
        float rY = directionZ * upX - directionX * upZ;
        float rZ = directionX * upY - directionY * upX;

        length = Vector3f.length(rX, rY, rZ);
        rX /= length;
        rY /= length;
        rZ /= length;

        upX = rY * directionZ - rZ * directionY;
        upY = rZ * directionX - rX * directionZ;
        upZ = rX * directionY - rY * directionX;

        length = Vector3f.length(upX, upY, upZ);
        upX /= length;
        upY /= length;
        upZ /= length;

        getRotation().set(
            rX, upX, -directionX, 0,
            rY, upY, -directionY, 0,
            rZ, upZ, -directionZ, 0,
            0, 0, 0, 1
        );
    }
}
