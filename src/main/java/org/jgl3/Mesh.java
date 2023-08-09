package org.jgl3;

import org.joml.Vector4f;

public abstract class Mesh extends NodeState {
    
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private final MeshPipeline pipeline;
    private Texture texture = null;
    
    public Mesh(MeshPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public final MeshPipeline getMeshPipeline() {
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
}
