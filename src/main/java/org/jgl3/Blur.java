package org.jgl3;

public class Blur extends Resource {

    public static final int SAMPLE_COUNT = 8;
    
    private final Pipeline pipeline;
    private final VertexArray vao;
    private final RenderTarget renderTarget1;
    private final RenderTarget renderTarget2;
    private final int uTexture, uPixelSize;
    private final int[] uOffset = new int[SAMPLE_COUNT];
    private final float[] offset = new float[SAMPLE_COUNT];

    public Blur(int width, int height) throws Exception {
        pipeline = new Pipeline(
            IO.readAllBytes(Dilate.class, "/org/jgl3/glsl/ScreenVertexShader.glsl"),
            IO.readAllBytes(Dilate.class, "/org/jgl3/glsl/BlurFragmentShader.glsl"),
            "vsPosition", 2
        );
        pipeline.setColorLocations("fsOutColor");
        vao = new VertexArray(false, 2);
        vao.getVBO().buffer(new float[] { -1, -1, +1, -1, +1, +1, +1, +1, -1, +1, -1, -1 }, VertexUsage.STATIC);
        renderTarget1 = new RenderTarget(width, height, PixelFormat.COLOR);
        renderTarget2 = new RenderTarget(width, height, PixelFormat.COLOR);
        uTexture = pipeline.getUniform("uTexture");
        uPixelSize = pipeline.getUniform("uPixelSize");
        for(int i = 0; i != SAMPLE_COUNT; i++) {
            offset[i] = i + 1;
            uOffset[i] = pipeline.getUniform("uOffset[" + i + "]");
        }
    }

    public int getWidth() {
        return renderTarget1.getWidth();
    }

    public int getHeight() {
        return renderTarget1.getHeight();
    }

    public Texture getTexture() {
        return renderTarget2.getTexture(0);
    }

    public float getOffset(int i) {
        return offset[i];
    }

    public void setOffset(int i, float x) {
        offset[i] = x;
    }

    public void scaleOffset(float s) {
        for(int i = 0; i != SAMPLE_COUNT; i++) {
            offset[i] = (i + 1) * s;
        }
    }
    
    public Texture process(Texture texure) throws Exception {
        renderTarget1.begin();
        GFX.clear(0, 0, 0, 0);
        pipeline.begin();
        vao.begin();
        pipeline.set(uTexture, 0, texure);
        for(int i = 0; i != SAMPLE_COUNT; i++) {
            pipeline.set(uOffset[i], offset[i], 0);
        }
        pipeline.set(uPixelSize, 1.0f / renderTarget1.getWidth(), 1.0f / renderTarget1.getHeight());
        vao.draw(0, 6);
        vao.end();
        pipeline.end();
        renderTarget1.end();

        renderTarget2.begin();
        GFX.clear(0, 0, 0, 0);
        pipeline.begin();
        vao.begin();
        pipeline.set(uTexture, 0, renderTarget1.getTexture(0));
        for(int i = 0; i != SAMPLE_COUNT; i++) {
            pipeline.set(uOffset[i], 0, offset[i]);
        }
        pipeline.set(uPixelSize, 1.0f / renderTarget2.getWidth(), 1.0f / renderTarget2.getHeight());
        vao.draw(0, 6);
        vao.end();
        pipeline.end();
        renderTarget2.end();

        return getTexture();
    }

    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        vao.destroy();
        renderTarget1.destroy();
        renderTarget2.destroy();
        super.destroy();
    }
}
