package org.jgl3;

import java.util.Vector;

public class MeshPTN extends Mesh {

    public MeshPTN(LightPipeline pipeline) {
        super(pipeline);
    }

    public final LightPipeline getPipeline() {
        return (LightPipeline)getMeshPipeline();
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
