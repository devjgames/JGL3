package org.jgl3;

import java.util.Vector;

import org.joml.Vector4f;

public final class DualTextureMesh extends NodeState {
    
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private Texture texture = null;
    private Texture texture2 = null;
    private final DualTexturePipeline pipeline;

    public DualTextureMesh() throws Exception {
        pipeline = Game.getInstance().getAssets().manage(new DualTexturePipeline());
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

    public Texture getTexture2() {
        return texture2;
    }

    public void setTexture2(Texture texture2) {
        this.texture2 = texture2;
    }

    public DualTexturePipeline getPipeline() {
        return pipeline;
    }

    @Override
    public boolean isRenderable() {
        return true;
    }

    @Override
    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception  {

        if(getPipeline().getFaceCount() == 0 || getPipeline().getVertexCount() == 0) {
            return lastState;
        }

        getState().bind(lastState);

        pipeline.begin();
        pipeline.setTexture(texture);
        pipeline.setTexture2(texture2);
        pipeline.setColor(color);
        pipeline.setTransform(camera, this);
        pipeline.render(pipeline.getIndexCount());
        pipeline.end();

        return getState();
    }
}
