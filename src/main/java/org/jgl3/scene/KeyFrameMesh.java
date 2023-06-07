package org.jgl3.scene;

import java.io.File;
import java.util.Vector;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.Renderer;
import org.jgl3.Triangle;

public final class KeyFrameMesh implements Renderable {
    
    private final File file;
    private final Vector<KeyFrame> frames = new Vector<>();
    private int start = 0;
    private int end = 0;
    private int frame = 0;
    private int speed = 0;
    private boolean looping = false;
    private boolean done = true;
    private float amount = 0;
    private final BoundingBox bounds = new BoundingBox();

    public KeyFrameMesh(File file, Vector<KeyFrame> frames) throws Exception {
        this.file = file;
        this.frames.addAll(frames);
        reset();
    }

    @Override
    public File getFile() {
        return file;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getSpeed() {
        return speed;
    }

    public boolean isLooping() {
        return looping;
    }

    public int getFrame() {
        return frame;
    }

    public float getAmount() {
        return amount;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public KeyFrame getFrame(int i) {
        return frames.get(i);
    }

    public boolean isDone() {
        return done;
    }

    public void reset() {
        frame = start;
        amount = 0;
        done = start == end;
        bounds.set(frames.get(frame).getBounds());
    }

    public boolean isSequence(int start, int end, int speed, boolean looping) {
        return start == this.start && end == this.end && speed == this.speed && looping == this.looping;
    }

    public void setSequence(int start, int end, int speed, boolean looping) {
        if(!isSequence(start, end, speed, looping)) {
            if(start >= 0 && start < frames.size() && end >= 0 && end < frames.size() && start <= end && speed >= 0) {
                this.start = start;
                this.end = end;
                this.speed = speed;
                this.looping = looping;
                reset();
            }
        }
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public int getTriangleCount() {
        return frames.get(0).getVertices().length / 8 / 3;
    }

    @Override
    public Triangle getTriangle(int i, Triangle triangle) {
        KeyFrame f1 = frames.get(frame);
        KeyFrame f2;

        if(frame == end) {
            f2 = frames.get(start);
        } else {
            f2 = frames.get(frame + 1);
        }

        float[] v1 = f1.getVertices();
        float[] v2 = f2.getVertices();
        int n = 8;

        i *= 3;

        float x1 = v1[(i + 0) * n + 0] + amount * (v2[(i + 0) * n + 0] - v1[(i + 0) * n + 0]);
        float y1 = v1[(i + 0) * n + 1] + amount * (v2[(i + 0) * n + 1] - v1[(i + 0) * n + 1]);
        float z1 = v1[(i + 0) * n + 2] + amount * (v2[(i + 0) * n + 2] - v1[(i + 0) * n + 2]);
        float x2 = v1[(i + 1) * n + 0] + amount * (v2[(i + 1) * n + 0] - v1[(i + 1) * n + 0]);
        float y2 = v1[(i + 1) * n + 1] + amount * (v2[(i + 1) * n + 1] - v1[(i + 1) * n + 1]);
        float z2 = v1[(i + 1) * n + 2] + amount * (v2[(i + 1) * n + 2] - v1[(i + 1) * n + 2]);
        float x3 = v1[(i + 2) * n + 0] + amount * (v2[(i + 2) * n + 0] - v1[(i + 2) * n + 0]);
        float y3 = v1[(i + 2) * n + 1] + amount * (v2[(i + 2) * n + 1] - v1[(i + 2) * n + 1]);
        float z3 = v1[(i + 2) * n + 2] + amount * (v2[(i + 2) * n + 2] - v1[(i + 2) * n + 2]);

        triangle.getP1().set(x1, y1, z1);
        triangle.getP2().set(x2, y2, z2);
        triangle.getP3().set(x3, y3, z3);

        return triangle.calcPlane();
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
        if(isDone()) {
            return;
        }

        Game game = Game.getInstance();

        amount += speed * game.getElapsedTime();
        if(amount >= 1) {
            if(looping) {
                if(frame == end) {
                    frame = start;
                } else {
                    frame++;
                }
                amount = 0;
            } else if(frame == end - 1) {
                amount = 1;
                done = true;
            } else {
                frame++;
                amount = 0;
            }
        }

        KeyFrame f1 = frames.get(frame);
        KeyFrame f2;

        if(frame == end) {
            f2 = frames.get(start);
        } else {
            f2 = frames.get(frame + 1);
        }

        f1.getBounds().getMin().lerp(f2.getBounds().getMin(), amount, bounds.getMin());
        f1.getBounds().getMax().lerp(f2.getBounds().getMax(), amount, bounds.getMax());
    }

    @Override
    public void render(Scene scene, Node node) throws Exception {
        KeyFrame f1 = frames.get(frame);
        KeyFrame f2;
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();

        if(frame == end) {
            f2 = frames.get(start);
        } else {
            f2 = frames.get(frame + 1);
        }

        float[] v1 = f1.getVertices();
        float[] v2 = f2.getVertices();

        renderer.beginTriangles();

        for(int i = 0; i != v1.length; i += 8) {
            float x = v1[i + 0] + amount * (v2[i + 0] - v1[i + 0]);
            float y = v1[i + 1] + amount * (v2[i + 1] - v1[i + 1]);
            float z = v1[i + 2] + amount * (v2[i + 2] - v1[i + 2]);
            float s = v1[i + 3];
            float t = v1[i + 4];
            float a = v1[i + 5] + amount * (v2[i + 5] - v1[i + 5]);
            float b = v1[i + 6] + amount * (v2[i + 6] - v1[i + 6]);
            float c = v1[i + 7] + amount * (v2[i + 7] - v1[i + 7]);

            renderer.push(x, y, z, s, t, 0, 0, a, b, c, 1, 1, 1, 1);
        }
        renderer.endTriangles();
    }

    @Override
    public Renderable newInstance() throws Exception {
        return new KeyFrameMesh(file, frames);
    }
}
