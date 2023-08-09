package org.jgl3.demo;

import org.jgl3.BoundingBox;
import org.jgl3.Camera;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.OctTree;
import org.jgl3.Scene;
import org.jgl3.Sound;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class FPSCamera extends Camera {
    
    private final Vector3f right = new Vector3f();
    private final Vector3f up = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Matrix4f matrix = new Matrix4f();
    private final OctTree collidables;
    private final Vector3f velocity = new Vector3f();
    private float gravity = 2000;
    private boolean onGround = false;
    private final Matrix4f groundMatrix = new Matrix4f();
    private final Vector3f groundNormal = new Vector3f();
    private final Vector3f resolvedPosition = new Vector3f();
    private final Vector3f resolvedNormal = new Vector3f();
    private final BoundingBox bounds = new BoundingBox();
    private final float[] time = new float[1];
    private final Vector3f radii = new Vector3f(12, 24, 12);
    private float speed = 100;
    private final Vector3f delta = new Vector3f();
    private boolean resolved = false;
    private float groundSlope = 60;
    private final Matrix4f toUnit = new Matrix4f();
    private final Matrix4f inverseToUnit = new Matrix4f();
    private final Vector3f p = new Vector3f();
    private final Sound pain;

    public FPSCamera(OctTree collidables) throws Exception {
        this.collidables = collidables;

        pain = Game.getInstance().getAssets().load(IO.file("assets/sound/pain.wav"));
        pain.setVolume(0.25f);
    }

    public float getGravity() {
        return gravity;
    }

    public void setGravity(float gravity) {
        this.gravity = gravity;
    }

    public Vector3f getRadii() {
        return radii;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public float getGroundSlope() {
        return groundSlope;
    }

    public void setGroundSlope(float slope) {
        groundSlope = slope;
    }

    @Override
    public void update(Scene scene) throws Exception {
        Game game = Game.getInstance();

        getRotation().getRow(1, up);
        getRotation().getRow(2, direction).negate();
        matrix.identity().rotate(game.getDX() * -0.025f, 0,1 , 0);
        direction.cross(up, right).mulDirection(matrix).normalize();
        direction.mulDirection(matrix).normalize();
        matrix.identity().rotate(game.getDY() * 0.025f, right);
        right.cross(direction, up).mulDirection(matrix).normalize();
        direction.mulDirection(matrix).normalize();
        look(direction, up);

        velocity.mul(0, 1, 0);
        direction.y = 0;
        if((game.isButtonDown(0) || game.isButtonDown(1)) && direction.length() > 0.0000001) {
            velocity.add(direction.normalize((game.isButtonDown(0)) ? speed : -speed));
        }
        velocity.y -= gravity * game.getElapsedTime();

        toUnit.identity().scale(1 / radii.x, 1 / radii.y, 1 / radii.z);
        toUnit.invert(inverseToUnit);

        delta.set(velocity).mul(game.getElapsedTime());

        Vector3f position = getPosition();

        if(delta.length() > 0.1) {

            p.set(position);

            position.mulDirection(toUnit);
            delta.mulDirection(groundMatrix).mulDirection(toUnit);

            groundMatrix.identity();
            groundNormal.zero();
            onGround = false;

            if(delta.length() > 0.5f) {
                delta.normalize(0.5f);
            }

            position.add(delta);

            for(int i = 0; i < 3; i++) {
                resolved = false;
                time[0] = 1;
                bounds.getMin().set(p).sub(radii.x, radii.y, radii.z);
                bounds.getMax().set(p).add(radii.x, radii.y, radii.z);
                resolvedNormal.zero();

                collidables.traverse(bounds, (tri) -> {
                    tri.transform(toUnit);
                    if(tri.resolve(position, 1, time, resolvedPosition, resolvedNormal)) {
                        resolved = true;
                    }
                    tri.transform(inverseToUnit);
                    return true;
                });
                if(resolved) {
                    resolvedNormal.mulDirection(inverseToUnit);
                    if(Math.acos(Math.max(-0.999f, Math.min(0.999f, resolvedNormal.dot(0, 1, 0)))) < Math.toRadians(groundSlope)) {
                        groundNormal.add(resolvedNormal);
                        velocity.y = 0;
                        onGround = true;
                    }
                    position.set(resolvedPosition);
                    p.set(position);
                    p.mulDirection(inverseToUnit);
                } else {
                    break;
                }
            }

            if(onGround) {
                up.set(groundNormal).normalize();
                right.set(1, 0, 0);
                right.cross(up, direction).normalize();
                up.cross(direction, right).normalize();
                groundMatrix.set(
                    right.x, right.y, right.z, 0,
                    up.x, up.y, up.z, 0,
                    direction.x, direction.y, direction.z, 0,
                    0, 0, 0, 1
                );
            }

            position.mulDirection(inverseToUnit);
        }

        if(game.isKeyDown(GLFW.GLFW_KEY_ESCAPE)) {
            game.disableFPSMouse();
        }
    }
}
