package org.jgl3.demo;

import org.jgl3.Camera;
import org.jgl3.DualTextureMesh;
import org.jgl3.Game;
import org.jgl3.Scene;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class PlayerCamera extends Camera {
    
    private final Vector3f target = new Vector3f();
    private final Vector3f up = new Vector3f(0, 1, 0);
    private final Vector3f offset = new Vector3f();
    private final Vector3f right = new Vector3f();
    private final Matrix4f matrix = new Matrix4f();
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final float[] time = new float[1];
    private final Triangle triangle = new Triangle();

    public Vector3f getTarget() {
        return target;
    }

    public Vector3f getUp() {
        return up;
    }

    @Override
    public void postUpdate(Scene scene) throws Exception {
        Vector3f eye = getPosition();

        getRotation().identity().lookAlong(target.x - eye.x, target.y - eye.y, target.z - eye.z, up.x, up.y, up.z);
    }

    public void rotate() {
        Game game = Game.getInstance();

        getPosition().sub(target, offset);
        matrix.identity().rotate(game.getDX() * -0.025f, 0, 1, 0);
        offset.cross(up, right).mulDirection(matrix).normalize();
        offset.mulDirection(matrix);
        matrix.identity().rotate(game.getDY() * 0.025f, right);
        right.cross(offset, up).mulDirection(matrix).normalize();
        offset.mulDirection(matrix).normalize();

        target.add(offset, getPosition());
    }

    public void collide(float offsetLength, float radius) {
        float length = offsetLength;

        getPosition().sub(target, direction).normalize();
        origin.set(target);
        time[0] = offsetLength + radius + 2;
        for(DualTextureMesh mesh : Collidables.meshes) {
            for(int i = 0; i != mesh.getPipeline().getIndexCount(); ) {
                int i1 = mesh.getPipeline().getIndex(i++);
                int i2 = mesh.getPipeline().getIndex(i++);
                int i3 = mesh.getPipeline().getIndex(i++);

                triangle.getP1().set(
                    mesh.getPipeline().getVertexComponent(i1, 0),
                    mesh.getPipeline().getVertexComponent(i1, 1),
                    mesh.getPipeline().getVertexComponent(i1, 2)
                );

                triangle.getP2().set(
                    mesh.getPipeline().getVertexComponent(i2, 0),
                    mesh.getPipeline().getVertexComponent(i2, 1),
                    mesh.getPipeline().getVertexComponent(i2, 2)
                );

                triangle.getP3().set(
                    mesh.getPipeline().getVertexComponent(i3, 0),
                    mesh.getPipeline().getVertexComponent(i3, 1),
                    mesh.getPipeline().getVertexComponent(i3, 2)
                );
                triangle.transform(mesh.getModel());

                if(triangle.intersects(origin, direction, 1, time)) {
                    length = Math.min(offsetLength, time[0]) - radius - 2;
                }
            }
        }
        target.add(direction.mul(length), getPosition());
    }
}
