package org.jgl3;

import org.joml.Vector4f;

public class DualTexturePipeline extends MeshPipeline {
    
    public static final int COMPONENTS = 7;

    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;
    private int uTexture2, uTexture2Enabled;
    private int uColor;

    public DualTexturePipeline() throws Exception {
        super(
            new Pipeline(
                IO.readAllBytes(LightPipeline.class, "/glsl/DualTextureVertex.glsl"),
                IO.readAllBytes(LightPipeline.class, "/glsl/DualTextureFragment.glsl"),
                "vsInPosition", 3, 
                "vsInTextureCoordinate", 2,
                "vsInTextureCoordinate2", 2
            )
        );

        getPipeline().setColorLocations("fsOutColor");

        uProjection = getPipeline().getUniform("uProjection");
        uView = getPipeline().getUniform("uView");
        uModel = getPipeline().getUniform("uModel");
        uTexture = getPipeline().getUniform("uTexture");
        uTextureEnabled = getPipeline().getUniform("uTextureEnabled");
        uTexture2 = getPipeline().getUniform("uTexture2");
        uTexture2Enabled = getPipeline().getUniform("uTexture2Enabled");
        uColor = getPipeline().getUniform("uColor");
    }

    @Override
    public final int getComponents() {
        return COMPONENTS;
    }

    public void setColor(float r, float g, float b, float a) {
        getPipeline().set(uColor, r, g, b, a);
    }

    public void setColor(Vector4f color) {
        getPipeline().set(uColor, color);
    }


    public void setTexture(Texture texture) {
        getPipeline().set(uTextureEnabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture, 0, texture);
        }
    }

    public void setTexture2(Texture texture2) {
        getPipeline().set(uTexture2Enabled, texture2 != null);
        if(texture2 != null) {
            getPipeline().set(uTexture2, 1, texture2);
        }
    }
    
    public void setTransform(Camera camera, Node node) {
        getPipeline().set(uProjection, camera.getProjection());
        getPipeline().set(uView, camera.getModel());
        getPipeline().set(uModel, node.getModel());
    }
}
