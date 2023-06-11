package org.jgl3.scene;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.OctTree;
import org.jgl3.Sound;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public final class Collider {
    
    public static interface ContactListener {
        void contactMade(Triangle triangle) throws Exception;
    }

    private final Vector3f position = new Vector3f();
    private final Vector3f velocity = new Vector3f();
    private final Vector3f delta = new Vector3f();
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f groundNormal = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final Vector3f normal = new Vector3f();
    private final Vector3f resolvedPosition = new Vector3f();
    private final Vector3f r = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f f = new Vector3f();
    private final float[] time = new float[1];
    private final Matrix4f groundMatrix = new Matrix4f();
    private final Matrix4f toUnitMatrix = new Matrix4f();
    private final Matrix4f inverseToUnitMatrix = new Matrix4f();
    private final Vector3f radii = new Vector3f(8, 16, 8);
    private float gravity = 2000;
    private float groundSlope = 45;
    private float roofSlope = 45;
    private float intersectionBuffer = 0;
    private int intersectionBits = 0xFF;
    private boolean onGround = false;
    private boolean hitRoof = false;
    private ContactListener contactListener = null;
    private final Triangle triangle = new Triangle();
    private final BoundingBox bounds = new BoundingBox();
    private Triangle hit = null;
    private final Triangle triangle2 = new Triangle();
    private final Triangle tTriangle = new Triangle();
    private int loopCount = 3;
    private int tested = 0;

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getVelocity() {
        return velocity;
    }

    public Vector3f getOrigin() {
        return origin;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public float getTime() {
        return time[0];
    }

    public Collider setTime(float time) {
        this.time[0] = time;
        return this;
    }

    public Vector3f getRadii() {
        return radii;
    }

    public float getGravity() {
        return gravity;
    }

    public Collider setGravity(float gravity) {
        this.gravity = gravity;
        return this;
    }

    public float getGroundSlope() {
        return groundSlope;
    }

    public Collider setGroundSlope(float slope) {
        groundSlope = slope;
        return this;
    }

    public float getRoofSlope() {
        return roofSlope;
    }

    public Collider setRoofSlope(float slope) {
        roofSlope = slope;
        return this;
    }

    public float getIntersectionBuffer() {
        return intersectionBuffer;
    }

    public Collider setIntersectionBuffer(float buffer) {
        intersectionBuffer = buffer;
        return this;
    }

    public int getIntersectionBits() {
        return intersectionBits;
    }

    public Collider setIntersectionBits(int bits) {
        intersectionBits = bits;
        return this;
    }

    public boolean isOnGround() {
        return onGround;
    }

    public boolean getHitRoof() {
        return hitRoof;
    }

    public ContactListener getContactListener() {
        return contactListener;
    }

    public Collider setContactListener(ContactListener listener) {
        contactListener = listener;
        return this;
    }

    public int getLoopCount() {
        return loopCount;
    }

    public Collider setLoopCount(int count) {
        loopCount = count;
        return this;
    }

    public int getTested() {
        return tested;
    }

    public Triangle intersect(Node node) throws Exception {
        hit = null;
        node.traverse((n) -> {
            if(n.isCollidable()) {
                OctTree octTree = n.getOctTree();

                bounds.clear();
                bounds.add(origin);
                bounds.add(point.set(direction).mul(time[0]).add(origin));

                if(octTree == null) {
                    if(bounds.touches(n.getBounds())) {
                        for(int i = 0; i != n.getTriangleCount(); i++) {
                            intersect(n.getTriangle(i, triangle));
                        }
                    }
                } else {
                    octTree.traverse(bounds, (triangle) -> {
                        intersect(triangle);
                    });
                }
            }
            return true;
        });
        return hit;
    }

    public boolean move(Scene scene, Node node, int speed, int jump, Sound jumpSound, boolean xMoveOnly) throws Exception {
        Game game = Game.getInstance();
        boolean moving = true;

        setIntersectionBuffer(1);

        if(game.isKeyDown(GLFW.GLFW_KEY_SPACE) && isOnGround() && jump > 0) {
            jumpSound.play(false);
            getVelocity().y = jump;
        }

        getVelocity().mul(0, 1, 0);
        if(xMoveOnly) {
            if(game.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                node.getRotation().identity().rotate((float)Math.toRadians(0), 0, 1, 0);
                getVelocity().x = speed;
            } else if(game.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                node.getRotation().identity().rotate((float)Math.toRadians(180), 0, 1, 0);
                getVelocity().x = -speed;
            } else {
                node.getRotation().identity().rotate((float)Math.toRadians(-90), 0, 1, 0);
                moving = false;
            }
        } else {
            scene.getCamera().getOffset().mul(-1, 0, -1, f).normalize();
            f.cross(0, 1, 0, r).normalize();
            if(game.isKeyDown(GLFW.GLFW_KEY_RIGHT)) {
                f.zero();
                r.mul(speed);
            } else if(game.isKeyDown(GLFW.GLFW_KEY_LEFT)) {
                f.zero();
                r.mul(-speed);
            } else if(game.isKeyDown(GLFW.GLFW_KEY_UP)) {
                f.mul(speed);
                r.zero();
            } else if(game.isKeyDown(GLFW.GLFW_KEY_DOWN)) {
                f.mul(-speed);
                r.zero();
            } else {
                moving = false;
                f.zero();
                r.zero();
            }
            getVelocity().add(f.add(r));
            if(moving) {
                f.normalize();
                float a = (float)Math.acos(Math.max(-0.999f, Math.min(0.999f, f.x)));
                if(f.z > 0) {
                    a = (float)Math.PI * 2 - a;
                }
                node.getRotation().identity().rotate(a, 0, 1, 0);
            }
        }
        getPosition().set(node.getPosition());
        collide(scene.getRoot());
        if(xMoveOnly) {
            getPosition().mul(1, 1, 0);
        }
        node.getPosition().set(getPosition());

        return moving;
    }


    public Collider collide(Node node) throws Exception {
        Game game = Game.getInstance();

        velocity.y -= gravity * game.getElapsedTime();
        velocity.mul(game.getElapsedTime(), delta).mulDirection(groundMatrix);
        if(delta.length() < 0.0000001) {
            return this;
        }

        toUnitMatrix.identity().scale(1 / radii.x, 1 / radii.y, 1  / radii.z);
        toUnitMatrix.invert(inverseToUnitMatrix);

        delta.mulDirection(toUnitMatrix);
        position.mulPosition(toUnitMatrix);

        if(delta.length() > 0.5f) {
            delta.normalize().mul(0.5f);
        }
        position.add(delta);
        onGround = false;
        hitRoof = false;
        groundNormal.zero();
        groundMatrix.identity();

        tested = 0;

        for(int i = 0; i < loopCount; i++) {
            bounds.getMin().set(position).sub(1, 1, 1);
            bounds.getMax().set(position).add(1, 1, 1);
            bounds.transform(inverseToUnitMatrix);
            time[0] = 1;
            hit = null;
            node.traverse((n) -> {
                if(n.isCollidable() && bounds.touches(n.getBounds())) {
                    OctTree octTree = n.getOctTree();

                    if(octTree == null) {
                        for(int j = 0; j != n.getTriangleCount(); j++) {
                            collide(n.getTriangle(j, triangle));
                        }
                    } else {
                        octTree.traverse(bounds, (triangle) -> {
                            collide(triangle);
                        });
                    }
                }
                return true;
            });
            if(hit != null) {
                normal.mulDirection(inverseToUnitMatrix).normalize();
                if((float)Math.toDegrees(Math.acos(Math.max(-0.99f, Math.min(0.99f, normal.dot(0, 1, 0))))) < groundSlope) {
                    onGround = true;
                    groundNormal.add(normal);
                }
                if((float)Math.toDegrees(Math.acos(Math.max(-0.99f, Math.min(0.99f, normal.dot(0, -1, 0))))) < roofSlope) {
                    hitRoof = true;
                }
                position.set(resolvedPosition);
                if(contactListener != null) {
                    contactListener.contactMade(hit);
                }
            } else {
                break;
            }
        }
        if(onGround || hitRoof) {
            if(onGround) {
                groundNormal.normalize(u);
                r.set(1, 0, 0);
                r.cross(u, f).normalize();
                u.cross(f, r).normalize();
                groundMatrix.set(
                    r.x, r.y, r.z, 0,
                    u.x, u.y, u.z, 0,
                    f.x, f.y, f.z, 0,
                    0, 0, 0, 1
                );
            }
            velocity.y = 0;
        }
        position.mulPosition(inverseToUnitMatrix);

        return this;
    }

    private void intersect(Triangle triangle) {
        if((triangle.getTag() & intersectionBits) != 0) {
            if(triangle.intersects(origin, direction, intersectionBuffer, time)) {
                hit = triangle2.set(triangle);
            }
        } 
    }

    private void collide(Triangle triangle) {
        float t = time[0];

        tTriangle.set(triangle).transform(toUnitMatrix);
        triangle = tTriangle;

        triangle.getNormal().negate(direction);
        if(triangle.intersectsPlane(position, direction, time)) {
            point.set(direction).mul(time[0]).add(position);
            if(triangle.contains(point, 0)) {
                normal.set(triangle.getNormal());
                resolvedPosition.set(point).add(direction.set(triangle.getNormal()));
                hit = triangle2.set(triangle);
            } else {
                time[0] = t;
                triangle.closestPoint(position, point);
                position.sub(point, direction);
                if(direction.length() > 0.0000001 && direction.length() < time[0]) {
                    time[0] = direction.length();
                    direction.normalize(normal);
                    resolvedPosition.set(point).add(direction.normalize());
                    hit = triangle2.set(triangle);
                }
            }
        }
        tested++;
    } 
}
