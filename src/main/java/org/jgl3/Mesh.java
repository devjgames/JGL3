package org.jgl3;

import java.util.Vector;

import org.joml.Vector4f;

public class Mesh extends NodeState {
    
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private final MeshPipeline pipeline;
    private Texture texture = null;
    
    public Mesh(MeshPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public final MeshPipeline getPipeline() {
        return pipeline;
    }

    public final Vector4f getColor() {
        return color;
    }

    public final Texture getTexture() {
        return texture;
    }

    public final void setTexture(Texture texture) {
        this.texture = texture;
    }

    @Override
    public final boolean isRenderable() {
        return true;
    }

    @Override
    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception {
        getState().bind(lastState);

        getPipeline().begin();
        getPipeline().setTexture(getTexture());
        getPipeline().setColor(getColor());
        getPipeline().setTransform(camera, this);
        getPipeline().setLights(lights);
        getPipeline().render();
        getPipeline().end();

        return getState();
    }
}
