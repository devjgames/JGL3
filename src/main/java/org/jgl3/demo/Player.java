package org.jgl3.demo;

import org.jgl3.DualTextureMesh;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.KeyFrameMesh;
import org.jgl3.Node;
import org.jgl3.Scene;
import org.jgl3.Sound;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Player extends Node {

    private static final int RADIUS = 16;
    
    private int jumpAmount = 0;
    private float offset = 150;
    private final Vector3f cameraOffset = new Vector3f();
    private final Vector3f forward = new Vector3f();
    private final Vector3f right = new Vector3f();
    private final Vector3f up = new Vector3f();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f delta = new Vector3f();
    private final Vector3f resolvedPosition = new Vector3f();
    private final Vector3f resolvedNormal = new Vector3f();
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final Vector3f groundNormal = new Vector3f();
    private final Matrix4f groundMatrix = new Matrix4f();
    private final Triangle triangle = new Triangle();
    private final float[] time = new float[1];
    private boolean onGround = false;
    private Sound jump = null;

    public int getJumpAmount() {
        return jumpAmount;
    }

    public void setJumpAmount(int amount) {
        jumpAmount = amount;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    @Override
    public void update(Scene scene) throws Exception {
        Game game = Game.getInstance();

        PlayerCamera camera = (PlayerCamera)scene.getCamera();

        if(game.isButtonDown(1)) {
            camera.rotate();
        }
        camera.getPosition().sub(camera.getTarget(), cameraOffset).normalize(offset);

        float dX = game.getMouseX() - game.getWidth() / 2;
        float dY = game.getMouseY() - game.getHeight() / 2;
        float dL = Vector2f.length(dX, dY);
        boolean moving = false;
        KeyFrameMesh mesh = (KeyFrameMesh)getChild(0);

        cameraOffset.negate(forward).mul(1, 0, 1);
        velocity.mul(0, 1, 0);
        if(game.isButtonDown(0) && forward.length() > 0.0000001 && dL > 0.01) {
            forward.normalize().cross(0, 1, 0, right).normalize().mul(dX / dL * 100);
            forward.mul(-dY / dL * 100).add(right);
            velocity.add(forward);
            forward.normalize();

            float radians = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, forward.x)));

            if(forward.z > 0) {
                radians = (float)Math.PI * 2 - radians;
            }
            getRotation().identity().rotate(radians, 0, 1, 0);

            moving = true;
        }
        if(game.isKeyDown(GLFW.GLFW_KEY_SPACE) && jumpAmount > 0 && onGround) {
            velocity.y = jumpAmount;
            if(jump == null) {
                jump = game.getAssets().load(IO.file("assets/jump.wav"));
                jump.setVolume(0.25f);
            }
            jump.play(false);
        }
        if(onGround) {
            boolean set = !mesh.isSequence(66, 67, 5, false);

            if(!set) {
                set = mesh.isDone();
            }
            if(set) {
                if(moving) {
                    mesh.setSequence(40, 45, 10, true);
                } else {
                    mesh.setSequence(0, 39, 9, true);
                }
            }
        } else {
            mesh.setSequence(66, 67, 5, false);
        }

        velocity.y -= 2000 * game.getElapsedTime();

        velocity.mul(game.getElapsedTime(), delta).mulDirection(groundMatrix);

        groundMatrix.identity();
        groundNormal.zero();

        if(delta.length() > 0.0000001) {
                        
            onGround = false;            

            if(delta.length() > RADIUS * 0.5f) {
                delta.normalize(RADIUS * 0.5f);
            }
            getPosition().add(delta);

            for(int i = 0; i != 3; i++) {
                DualTextureMesh hit = null;

                time[0] = RADIUS;
                resolvedNormal.zero();

                for(DualTextureMesh cMesh : Collidables.meshes) {
                    for(int j = 0; j != cMesh.getPipeline().getIndexCount(); ) {
                        int i1 = cMesh.getPipeline().getIndex(j++);
                        int i2 = cMesh.getPipeline().getIndex(j++);
                        int i3 = cMesh.getPipeline().getIndex(j++);
        
                        triangle.getP1().set(
                            cMesh.getPipeline().getVertexComponent(i1, 0),
                            cMesh.getPipeline().getVertexComponent(i1, 1),
                            cMesh.getPipeline().getVertexComponent(i1, 2)
                        );
        
                        triangle.getP2().set(
                            cMesh.getPipeline().getVertexComponent(i2, 0),
                            cMesh.getPipeline().getVertexComponent(i2, 1),
                            cMesh.getPipeline().getVertexComponent(i2, 2)
                        );
        
                        triangle.getP3().set(
                            cMesh.getPipeline().getVertexComponent(i3, 0),
                            cMesh.getPipeline().getVertexComponent(i3, 1),
                            cMesh.getPipeline().getVertexComponent(i3, 2)
                        );
                        triangle.transform(cMesh.getModel());

                        float t = time[0];

                        triangle.getNormal().negate(direction);
                        origin.set(getPosition());

                        if(triangle.intersectsPlane(origin, direction, time)) {
                            point.set(direction).mul(time[0]).add(origin);
                            if(triangle.contains(point, 0)) {
                                resolvedNormal.set(triangle.getNormal());
                                resolvedPosition.set(resolvedNormal).mul(RADIUS).add(point);
                                hit = cMesh;
                            } else {
                                time[0] = t;
                                triangle.closestPoint(getPosition(), point);
                                if(point.distance(getPosition()) < time[0]) {
                                    time[0] = point.distance(getPosition());
                                    getPosition().sub(point, resolvedNormal).normalize();
                                    resolvedPosition.set(resolvedNormal).mul(RADIUS).add(point);
                                    hit = cMesh;
                                }
                            }
                        }
                    }
                }
                if(hit != null) {
                    if(Math.acos(Math.max(-0.999f, Math.min(0.999f, resolvedNormal.dot(0, 1, 0)))) < Math.PI / 3) {
                        onGround = true;
                        velocity.y = 0;
                        groundNormal.add(resolvedNormal);
                    }
                    if(Math.acos(Math.max(-0.999f, Math.min(0.999f, resolvedNormal.dot(0, -1, 0)))) < Math.PI / 6) {
                        velocity.y = 0;
                    }
                    getPosition().set(resolvedPosition);
                } else {
                    break;
                }
            }

            if(onGround) {
                groundNormal.normalize(up);
                right.set(1, 0, 0);
                right.cross(up, forward).normalize();
                up.cross(forward, right).normalize();
                groundMatrix.set(
                    right.x, right.y, right.z, 0,
                    up.x, up.y, up.z, 0,
                    forward.x, forward.y, forward.z, 0,
                    0, 0, 0, 1
                );
            }
        }

        camera.getTarget().set(getPosition());
        camera.getTarget().add(cameraOffset, camera.getPosition());

        camera.collide(offset, RADIUS);
    }
}
