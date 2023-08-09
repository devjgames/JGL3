package org.jgl3;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;

public final class SpritePipeline extends Resource {

    public static final int COMPONENTS = 8;
    
    private final Pipeline pipeline;
    private final VertexArray vao;
    private final int uProjection, uTexture;
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(COMPONENTS * 3);
    private Texture texture = null;
    private Font font = null;
    private final Matrix4f projection = new Matrix4f();

    public SpritePipeline() throws Exception {
        pipeline = new Pipeline(
            IO.readAllBytes(SpritePipeline.class, "/org/jgl3/glsl/SpriteVertex.glsl"), 
            IO.readAllBytes(SpritePipeline.class, "/org/jgl3/glsl/SpriteFragment.glsl"),
            "vsInPosition", 2,
            "vsInTextureCoordinate", 2,
            "vsInColor", 4
        );
        pipeline.setColorLocations("fsOutColor");
        vao = new VertexArray(false, 2, 2, 4);
        uProjection = pipeline.getUniform("uProjection");
        uTexture = pipeline.getUniform("uTexture");
    }

    public void begin(Size size) throws Exception {
        int w = size.getWidth();
        int h = size.getHeight();
        
        pipeline.begin();
        vao.begin();

        pipeline.set(uProjection, projection.identity().ortho(0, w, h, 0, -1, 1));

        texture = null;
        font = null;

        GFX.setDepthState(DepthState.NONE);
        GFX.setCullState(CullState.NONE);
        GFX.setBlendState(BlendState.ALPHA);
    }

    public void beginSprite(Font font) {
        beginSprite(font.getTexture());
        this.font = font;
    }

    public void beginSprite(Texture texture) {
        pipeline.set(uTexture, 0, texture);
        this.texture = texture;

        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }


    public void push(float x, float y, float s, float t, float r, float g, float b, float a) {
        vBuf = VertexBuffer.ensure(vBuf, 3000 * COMPONENTS);
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(s);
        vBuf.put(t);
        vBuf.put(r);
        vBuf.put(g);
        vBuf.put(b);
        vBuf.put(a);
    }

    public void push(int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, float r, float g, float b, float a, boolean flip) {
        if(texture == null) {
            return;
        }

        float tw = texture.getWidth();
        float th = texture.getHeight();
        float sx1 = sx / tw;
        float sy1 = sy / th;
        float sx2 = (sx + sw) / tw;
        float sy2 = (sy + sh) / th;
        float dx1 = dx;
        float dy1 = dy;
        float dx2 = dx + dw;
        float dy2 = dy + dh;

        if(flip) {
            float t = sy1;

            sy1 = sy2;
            sy2 = t;
        }

        push(dx1, dy1, sx1, sy1, r, g, b, a);
        push(dx1, dy2, sx1, sy2, r, g, b, a);
        push(dx2, dy2, sx2, sy2, r, g, b, a);
        push(dx2, dy2, sx2, sy2, r, g, b, a);
        push(dx2, dy1, sx2, sy1, r, g, b, a);
        push(dx1, dy1, sx1, sy1, r, g, b, a);
    }

    public void push(String text, int x, int y, int lineSpacing, float r, float g, float b, float a) {
        if(font == null) {
            return;
        }

        int sx = x;
        int s = font.getScale();
        int cols = font.getColumns();
        int cw = font.getCharWidth();
        int ch = font.getCharHeight();

        for(int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);

            if(c == '\n') {
                x = sx;
                y += lineSpacing * s + ch * s;
            } else {
                int j = (int)c - (int)' ';

                if(j >= 0 && j < 100) {
                    int col = j % cols;
                    int row = j / cols;

                    push(col * cw, row * ch, cw, ch, x, y, cw * s, ch * s, r, g, b, a, false);
                    x += cw * s;
                }
            }
        }
    }

    public void endSprite() throws Exception {
        int count = vBuf.position() / COMPONENTS;

        if(count != 0) {
            vBuf.flip();
            vao.getVBO().buffer(vBuf, VertexUsage.DYNAMIC);
            vao.draw(0, count);
        }
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void end() {
        vao.end();
        pipeline.end();
    }

    @Override
    public void destroy() throws Exception {
        pipeline.destroy();
        vao.destroy();
        super.destroy();
    }
}
