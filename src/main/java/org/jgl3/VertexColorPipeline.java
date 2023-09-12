package org.jgl3;

public class VertexColorPipeline extends MeshPipeline {
    
    public static final int COMPONENTS = 9;

    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;

    public VertexColorPipeline() throws Exception {
        super(
            new Pipeline(
                IO.readAllBytes(LightPipeline.class, "/glsl/VertexColorVertex.glsl"),
                IO.readAllBytes(LightPipeline.class, "/glsl/VertexColorFragment.glsl"),
                "vsInPosition", 3, 
                "vsInTextureCoordinate", 2,
                "vsInColor", 4
            )
        );

        getPipeline().setColorLocations("fsOutColor");

        uProjection = getPipeline().getUniform("uProjection");
        uView = getPipeline().getUniform("uView");
        uModel = getPipeline().getUniform("uModel");
        uTexture = getPipeline().getUniform("uTexture");
        uTextureEnabled = getPipeline().getUniform("uTextureEnabled");
    }

    @Override
    public final int getComponents() {
        return COMPONENTS;
    }

    public void setTexture(Texture texture) {
        getPipeline().set(uTextureEnabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture, 0, texture);
        }
    }
    
    public void setTransform(Camera camera, Node node) {
        getPipeline().set(uProjection, camera.getProjection());
        getPipeline().set(uView, camera.getModel());
        getPipeline().set(uModel, node.getModel());
    }

}
