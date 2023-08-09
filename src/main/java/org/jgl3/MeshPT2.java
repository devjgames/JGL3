package org.jgl3;

import java.util.Vector;


public class MeshPT2 extends Mesh {
    
    private Texture texture2 = null;
    
    public MeshPT2(DualTexturePipeline pipeline) {
        super(pipeline);
    }

    public final DualTexturePipeline getPipeline() {
        return (DualTexturePipeline)getMeshPipeline();
    }

    public final Texture getTexture2() {
        return texture2;
    }

    public final void setTexture2(Texture texture2) {
        this.texture2 = texture2;
    }

    @Override
    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception {
        getState().bind(lastState);

        getPipeline().begin();
        getPipeline().setTexture(getTexture());
        getPipeline().setTexture2(getTexture2());
        getPipeline().setColor(getColor());
        getPipeline().setTransform(camera, this);
        getPipeline().render();
        getPipeline().end();

        return getState();
    }
}
