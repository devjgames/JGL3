package org.jgl3;

public class Dilate extends Resource {
    
    private final Pipeline pipeline;
    private final VertexArray vao;
    private final RenderTarget renderTarget;
    private final int uTexture, uWidth, uPixelSize;

    public Dilate(int width, int height) throws Exception {
        pipeline = new Pipeline(
            IO.readAllBytes(Dilate.class, "/org/jgl3/glsl/ScreenVertexShader.glsl"),
            IO.readAllBytes(Dilate.class, "/org/jgl3/glsl/DilateFragmentShader.glsl"),
            "vsPosition", 2
        );
        pipeline.setColorLocations("fsOutColor");
        vao = new VertexArray(false, 2);
        vao.getVBO().buffer(new float[] { -1, -1, +1, -1, +1, +1, +1, +1, -1, +1, -1, -1 }, VertexUsage.STATIC);
        renderTarget = new RenderTarget(width, height, PixelFormat.COLOR);
        uTexture = pipeline.getUniform("uTexture");
        uWidth = pipeline.getUniform("uWidth");
        uPixelSize = pipeline.getUniform("uPixelSize");
    }

    public int getWidth() {
        return renderTarget.getWidth();
    }

    public int getHeight() {
        return renderTarget.getHeight();
    }

    public Texture getTexture() {
        return renderTarget.getTexture(0);
    }
    
    public Texture process(Texture texure, float width) throws Exception {
        renderTarget.begin();
        GFX.clear(0, 0, 0, 0);
        pipeline.begin();
        vao.begin();
        pipeline.set(uTexture, 0, texure);
        pipeline.set(uWidth, width);
        pipeline.set(uPixelSize, 1.0f / renderTarget.getWidth(), 1.0f / renderTarget.getHeight());
        vao.draw(0, 6);
        vao.end();
        pipeline.end();
        renderTarget.end();

        return getTexture();
    }

    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        vao.destroy();
        renderTarget.destroy();
        super.destroy();
    }
}
