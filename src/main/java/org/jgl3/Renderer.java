package org.jgl3;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

public final class Renderer extends Resource {

    public static final int MAX_LIGHTS = 8;
    public static final int COMPONENTS = 14;
    
    private final Pipeline pipeline;
    private final VertexArray vao;
    private final int[] uLightPosition = new int[MAX_LIGHTS];
    private final int[] uLightColor = new int[MAX_LIGHTS];
    private final int[] uLightRadius = new int[MAX_LIGHTS];
    private final int uLightCount, uLightingEnabled, uVertexColorEnabled;
    private final int uAmbientColor, uDiffuseColor, uColor;
    private final int uWarpAmplitude, uWarpTime, uWarp, uWarpFrequency;
    private final int uProjection, uView, uModel, uModelIT;
    private final int uTexture, uTextureEnabled;
    private final int uTexture2, uTexture2Enabled;
    private final int uLayerColor;
    private final int uTextureUnit;
    private final Matrix4f temp = new Matrix4f();
    private FloatBuffer vBuf = BufferUtils.createFloatBuffer(COMPONENTS * 3);
    private Texture texture = null;
    private Font font = null;

    public Renderer() throws Exception {
        pipeline = new Pipeline(
            IO.readAllBytes(Renderer.class, "/org/jgl3/glsl/VertexShader.glsl"), 
            IO.readAllBytes(Renderer.class, "/org/jgl3/glsl/FragmentShader.glsl"),
            "vsInPosition", 3,
            "vsInTextureCoordinate", 2,
            "vsInTextureCoordinate2", 2,
            "vsInNormal", 3,
            "vsInColor", 4
        );
        pipeline.setColorLocations("fsOutColor1", "fsOutColor2", "fsOutColor3");
        vao = new VertexArray(false, 3, 2, 2, 3, 4);
        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightPosition[i] = pipeline.getUniform("uLightPosition[" + i + "]");
            uLightColor[i] = pipeline.getUniform("uLightColor[" + i + "]");
            uLightRadius[i] = pipeline.getUniform("uLightRadius[" + i + "]");
        }
        uLightCount = pipeline.getUniform("uLightCount");
        uLightingEnabled = pipeline.getUniform("uLightingEnabled");
        uVertexColorEnabled = pipeline.getUniform("uVertexColorEnabled");
        uAmbientColor = pipeline.getUniform("uAmbientColor");
        uDiffuseColor = pipeline.getUniform("uDiffuseColor");
        uColor = pipeline.getUniform("uColor");
        uWarpAmplitude = pipeline.getUniform("uWarpAmplitude");
        uWarpTime = pipeline.getUniform("uWarpTime");
        uWarp = pipeline.getUniform("uWarp");
        uWarpFrequency = pipeline.getUniform("uWarpFrequency");
        uProjection = pipeline.getUniform("uProjection");
        uView = pipeline.getUniform("uView");
        uModel = pipeline.getUniform("uModel");
        uModelIT = pipeline.getUniform("uModelIT");
        uTexture = pipeline.getUniform("uTexture");
        uTextureEnabled = pipeline.getUniform("uTextureEnabled");
        uTexture2 = pipeline.getUniform("uTexture2");
        uTexture2Enabled = pipeline.getUniform("uTexture2Enabled");
        uLayerColor = pipeline.getUniform("uLayerColor");
        uTextureUnit = pipeline.getUniform("uTextureUnit");
    }

    public void begin() throws Exception {
        pipeline.begin();
        vao.begin();

        setLightCount(0);
        setVertexColorEnabled(false);
        setLightingEnabled(false);
        setAmbientColor(0, 0, 0, 0);
        setDiffuseColor(1, 1, 1, 1);
        setColor(1, 1, 1, 1);
        setWarp(false);
        setWarpAmplitude(0, 0, 0);
        setWarpTime(0);
        setLayerColor(0, 0, 0, 0);
        setProjection(temp.identity());
        setView(temp);
        setModel(temp);
        setTexture(null);
        setTexture2(null);
        setTextureUnit(0);

        texture = null;
        font = null;
    }

    public void setTextureUnit(int unit) {
        pipeline.set(uTextureUnit, unit);
    }

    public void setLightPosition(int i, float x, float y, float z) {
        pipeline.set(uLightPosition[i], x, y, z);
    }

    public void setLightPosition(int i, Vector3f position) {
        pipeline.set(uLightPosition[i], position);
    }

    public void setLightColor(int i, float r, float g, float b, float a) {
        pipeline.set(uLightColor[i], r, g, b, a);
    }

    public void setLightColor(int i, Vector4f color) {
        pipeline.set(uLightColor[i], color);
    }

    public void setLightRadius(int i, float radius) {
        pipeline.set(uLightRadius[i], radius);
    }

    public void setLightCount(int count) {
        pipeline.set(uLightCount, Math.max(0, Math.min(MAX_LIGHTS, count)));
    }

    public void setLightingEnabled(boolean enabled) {
        pipeline.set(uLightingEnabled, enabled);
    }

    public void setVertexColorEnabled(boolean enabled) {
        pipeline.set(uVertexColorEnabled, enabled);
    }

    public void setAmbientColor(float r, float g, float b, float a) {
        pipeline.set(uAmbientColor, r, g, b, a);
    }

    public void setAmbientColor(Vector4f color) {
        pipeline.set(uAmbientColor, color);
    }

    public void setDiffuseColor(float r, float g, float b, float a) {
        pipeline.set(uDiffuseColor, r, g, b, a);
    }

    public void setDiffuseColor(Vector4f color) {
        pipeline.set(uDiffuseColor, color);
    }

    public void setColor(float r, float g, float b, float a) {
        pipeline.set(uColor, r, g, b, a);
    }

    public void setColor(Vector4f color) {
        pipeline.set(uColor, color);
    }

    public void setWarpAmplitude(float x, float y, float z) {
        pipeline.set(uWarpAmplitude, x, y, z);
    }

    public void setWarpAmplitude(Vector3f amplitude) {
        pipeline.set(uWarpAmplitude, amplitude);
    }

    public void setWarpTime(float time) {
        pipeline.set(uWarpTime, time);
    }

    public void setWarp(boolean warp) {
        pipeline.set(uWarp, warp);
    }

    public void setWarpFrequency(float frequency) {
        pipeline.set(uWarpFrequency, frequency);
    }

    public void setLayerColor(float r, float g, float b, float a) {
        pipeline.set(uLayerColor, r, g, b, a);
    }

    public void setLayerColor(Vector4f color) {
        pipeline.set(uLayerColor, color);
    }

    public void setProjection(Matrix4f projection) {
        pipeline.set(uProjection, projection);
    }

    public void setView(Matrix4f view) {
        pipeline.set(uView, view);
    }

    public void setModel(Matrix4f model) {
        pipeline.set(uModel, model);
        pipeline.set(uModelIT, model.invert(temp).transpose());
    }

    public void setTexture(Texture texture) {
        pipeline.set(uTextureEnabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture, 0, texture);
        }
        this.texture = texture;
        font = null;
    }

    public void setFont(Font font) {
        setTexture(font.getTexture());
        this.font = font;
    }

    public void setTexture2(Texture texture) {
        pipeline.set(uTexture2Enabled, texture != null);
        if(texture != null) {
            pipeline.set(uTexture2, 1, texture);
        }
    }

    public void initSprites() {
        Game game = Game.getInstance();

        initSprites(game.getWidth(), game.getHeight());
    }

    public void initSprites(int w, int h) {
        setProjection(temp.identity().ortho(0, w, h, 0, -1, 1));
        setView(temp.identity());
        setModel(temp);
        setLightingEnabled(false);
        setVertexColorEnabled(true);
        setTexture(null);
        setTexture2(null);
        setWarp(false);
        setColor(1, 1, 1, 1);
        setTextureUnit(0);
        font = null;

        GFX.setDepthState(DepthState.NONE);
        GFX.setCullState(CullState.NONE);
        GFX.setBlendState(BlendState.ALPHA);
    }

    public void beginTriangles() {
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);
    }

    public void render(float[] vertices) throws Exception {
        beginTriangles();
        if(vertices.length > vBuf.capacity()) {
            Log.put(1, "Renderer.render() increasing buffer capacity ...");

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vertices.length);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        vBuf.put(vertices);
        endTriangles();
    }

    public void push(float x, float y, float z, float s, float t, float u, float v, float nx, float ny, float nz, float r, float g, float b, float a) {
        if(vBuf.capacity() == vBuf.position()) {
            Log.put(1, "Increasing renderer buffer capacity ...");

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vBuf.capacity() + 3000 * COMPONENTS);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }

        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(s);
        vBuf.put(t);
        vBuf.put(u);
        vBuf.put(v);
        vBuf.put(nx);
        vBuf.put(ny);
        vBuf.put(nz);
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

        push(dx1, dy1, 0, sx1, sy1, 0, 0, 0, 0, 0, r, g, b, a);
        push(dx1, dy2, 0, sx1, sy2, 0, 0, 0, 0, 0, r, g, b, a);
        push(dx2, dy2, 0, sx2, sy2, 0, 0, 0, 0, 0, r, g, b, a);
        push(dx2, dy2, 0, sx2, sy2, 0, 0, 0, 0, 0, r, g, b, a);
        push(dx2, dy1, 0, sx2, sy1, 0, 0, 0, 0, 0, r, g, b, a);
        push(dx1, dy1, 0, sx1, sy1, 0, 0, 0, 0, 0, r, g, b, a);
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
                y += lineSpacing + ch * s;
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

    public void endTriangles() throws Exception {
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
