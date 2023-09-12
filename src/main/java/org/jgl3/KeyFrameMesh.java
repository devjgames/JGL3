package org.jgl3;

import java.util.Vector;

import org.joml.Vector4f;

public final class KeyFrameMesh extends NodeState {
    
    private final Vector<KeyFrame> frames = new Vector<>();
    private int start = 0;
    private int end = 0;
    private int frame = 0;
    private int speed = 0;
    private boolean looping = false;
    private boolean done = true;
    private float amount = 0;
    private final BoundingBox bounds = new BoundingBox();
    private final LightPipeline pipeline;
    private final Vector4f ambientColor = new Vector4f(0, 0, 0, 1);
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private Texture texture = null;

    public KeyFrameMesh(Vector<KeyFrame> frames) throws Exception {
        this.frames.addAll(frames);
        reset();

        pipeline = Game.getInstance().getAssets().manage(new LightPipeline());

        KeyFrame f1 = frames.get(0);

        for(int i = 0; i != f1.getVertices().length / pipeline.getComponents(); i += 3) {
            pipeline.pushFace(i, i + 1, i + 2);
        }
        pipeline.bufferIndices(VertexUsage.STATIC, true);
    }

    public BoundingBox getBound() {
        return bounds;
    }

    public Vector4f getAmbientColor() {
        return ambientColor;
    }

    public Vector4f getColor() {
        return color;
    }

    public Texture getTexture() {
        return texture;
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public LightPipeline getPipeline() {
        return pipeline;
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
    public void update(Scene scene) throws Exception {
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
    public boolean isRenderable() {
        return true;
    }

    @Override
    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception  {
        KeyFrame f1 = frames.get(frame);
        KeyFrame f2;

        if(frame == end) {
            f2 = frames.get(start);
        } else {
            f2 = frames.get(frame + 1);
        }

        float[] v1 = f1.getVertices();
        float[] v2 = f2.getVertices();

        pipeline.clearVertices();
        for(int i = 0; i != v1.length; i += 8) {
            float x = v1[i + 0] + amount * (v2[i + 0] - v1[i + 0]);
            float y = v1[i + 1] + amount * (v2[i + 1] - v1[i + 1]);
            float z = v1[i + 2] + amount * (v2[i + 2] - v1[i + 2]);
            float s = v1[i + 3];
            float t = v1[i + 4];
            float a = v1[i + 5] + amount * (v2[i + 5] - v1[i + 5]);
            float b = v1[i + 6] + amount * (v2[i + 6] - v1[i + 6]);
            float c = v1[i + 7] + amount * (v2[i + 7] - v1[i + 7]);

            pipeline.pushVertex(x, y, z, s, t, a, b, c);
        }
        pipeline.bufferVertices(VertexUsage.DYNAMIC, true);

        getState().bind(lastState);

        pipeline.begin();
        pipeline.setTexture(texture);
        pipeline.setColor(color);
        pipeline.setAmbientColor(ambientColor);
        pipeline.setTransform(camera, this);
        pipeline.setLights(lights);
        pipeline.render(pipeline.getIndexCount());
        pipeline.end();

        return getState();
    }

    public KeyFrameMesh newInstance() throws Exception {
        KeyFrameMesh mesh = new KeyFrameMesh(frames);

        mesh.setTexture(getTexture());
        mesh.getColor().set(getColor());
        mesh.getAmbientColor().set(getAmbientColor());
        mesh.setSequence(getStart(), getEnd(), getSpeed(), isLooping());

        mesh.getState().setDepthState(getState().getDepthState());
        mesh.getState().setCullState(getState().getCullState());
        mesh.getState().setBlendState(getState().getBlendState());

        return mesh;
    }
}
