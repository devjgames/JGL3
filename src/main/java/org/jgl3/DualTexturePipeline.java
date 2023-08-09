package org.jgl3;

import java.nio.FloatBuffer;

import org.joml.Vector4f;

public class DualTexturePipeline extends MeshPipeline {

    public static final int COMPONENTS = 7;

    private final Pipeline pipeline;
    private int uProjection, uView, uModel;
    private int uTexture, uTextureEnabled;
    private int uTexture2, uTexture2Enabled;
    private int uColor;

    public DualTexturePipeline() throws Exception {

        this(
            new Pipeline(
                IO.readAllBytes(DualTexturePipeline.class, "/org/jgl3/glsl/DualTextureVertex.glsl"),
                IO.readAllBytes(DualTexturePipeline.class, "/org/jgl3/glsl/DualTextureFragment.glsl"),
                "vsInPosition", 3, 
                "vsInTextureCoordinate", 2,
                "vsInTextureCoordinate2", 2
            )
        );
        

        pipeline.setColorLocations("fsOutColor");

        uProjection = pipeline.getUniform("uProjection");
        uView = pipeline.getUniform("uView");
        uModel = pipeline.getUniform("uModel");
        uTexture = pipeline.getUniform("uTexture");
        uTextureEnabled = pipeline.getUniform("uTextureEnabled");
        uTexture2 = pipeline.getUniform("uTexture2");
        uTexture2Enabled = pipeline.getUniform("uTexture2Enabled");
        uColor = pipeline.getUniform("uColor");
    }

    protected DualTexturePipeline(Pipeline pipeline) {
        super(3, 2, 2);

        this.pipeline = pipeline;
    }

    protected final Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public final int getComponents() {
        return COMPONENTS;
    }

    public final void push(float x, float y, float z, float s, float t, float u, float v) {
        FloatBuffer vBuf = getVertexBuffer();

        vBuf = setVertexBuffer(VertexBuffer.ensure(vBuf, 30000 * COMPONENTS));
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(s);
        vBuf.put(t);
        vBuf.put(u);
        vBuf.put(v);
        
        push();
    }

    public final void begin() throws Exception {
        getPipeline().begin();
        getVAO().begin(getPipeline().getVertexAttributes());
    }

    public void setTexture(Texture texture) {
       getPipeline().set(uTextureEnabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture, 0, texture);
        }
    }

    public void setTexture2(Texture texture) {
        getPipeline().set(uTexture2Enabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture2, 1, texture);
        }
    }

    public void setColor(float r, float g, float b, float a) {
        getPipeline().set(uColor, r, g, b, a);
    }

    public void setColor(Vector4f color) {
        getPipeline().set(uColor, color);
    }
    
    public void setTransform(Camera camera, Node node) {
        getPipeline().set(uProjection, camera.getProjection());
        getPipeline().set(uView, camera.getModel());
        getPipeline().set(uModel, node.getModel());
    }

    public final void end() {
        getVAO().end();
        getPipeline().end();
    }

    @Override
    public final void destroy() throws Exception {
        pipeline.destroy();

        super.destroy();
    }
}
