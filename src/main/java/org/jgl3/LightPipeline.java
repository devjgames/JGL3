package org.jgl3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.GeometryUtils;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LightPipeline extends MeshPipeline {

    public static final int COMPONENTS = 8;
    public static final int MAX_LIGHTS = 16;
    public static final int AMBIENT_LIGHT = 0;
    public static final int DIRECTIONAL_LIGHT = 1;
    public static final int POINT_LIGHT = 2;

    private final Pipeline pipeline;
    private final Vector3f normal = new Vector3f();
    private int[] uLightVector = new int[MAX_LIGHTS];
    private int[] uLightColor = new int[MAX_LIGHTS];
    private int[] uLightRange = new int[MAX_LIGHTS];
    private int[] uLightType = new int[MAX_LIGHTS];
    private int uLightCount;
    private int uProjection, uView, uModel, uModelIT;
    private int uColor;
    private  int uTexture, uTextureEnabled;
    private final Matrix4f it = new Matrix4f();

    public LightPipeline() throws Exception {
        this(
            new Pipeline(
                IO.readAllBytes(LightPipeline.class, "/org/jgl3/glsl/VertexLightVertex.glsl"),
                IO.readAllBytes(LightPipeline.class, "/org/jgl3/glsl/VertexLightFragment.glsl"),
                "vsInPosition", 3, 
                "vsInTextureCoordinate", 2,
                "vsInNormal", 3
            )
        );

        pipeline.setColorLocations("fsOutColor");

        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightVector[i] = pipeline.getUniform("uLightVector[" + i +  "]");
            uLightColor[i] = pipeline.getUniform("uLightColor[" + i  + "]");
            uLightRange[i] = pipeline.getUniform("uLightRange[" + i + "]");
            uLightType[i] = pipeline.getUniform("uLightType[" + i + "]");
        }
        uLightCount = pipeline.getUniform("uLightCount");
        uProjection = pipeline.getUniform("uProjection");
        uView = pipeline.getUniform("uView");
        uModel = pipeline.getUniform("uModel");
        uModelIT = pipeline.getUniform("uModelIT");
        uColor = pipeline.getUniform("uColor");
        uTexture = pipeline.getUniform("uTexture");
        uTextureEnabled = pipeline.getUniform("uTextureEnabled");
    }

    protected LightPipeline(Pipeline pipeline) {
        super(3, 2, 3);

        this.pipeline = pipeline;
    }

    protected final Pipeline getPipeline() {
        return pipeline;
    }

    @Override
    public final int getComponents() {
        return COMPONENTS;
    }

    public final void push(float x, float y, float z, float s, float t, float nx, float ny, float nz) {
        FloatBuffer vBuf = getVertexBuffer();

        vBuf = setVertexBuffer(VertexBuffer.ensure(vBuf, 30000 * getComponents()));
        vBuf.put(x);
        vBuf.put(y);
        vBuf.put(z);
        vBuf.put(s);
        vBuf.put(t);
        vBuf.put(nx);
        vBuf.put(ny);
        vBuf.put(nz);

        push();
    }

    public final void calcNormals() {
        IntBuffer iBuf = getIndexBuffer();
        FloatBuffer vBuf = getVertexBuffer();

        for(int i = 0; i != vBuf.position(); i += COMPONENTS) {
            vBuf.put(i + 5, 0);
            vBuf.put(i + 6, 0);
            vBuf.put(i + 7, 0);
        }
        for(int i = 0; i != iBuf.limit(); ) {
            int i1 = iBuf.get(i++);
            int i2 = iBuf.get(i++);
            int i3 = iBuf.get(i++);
            float x1 = vBuf.get(i1 * COMPONENTS + 0);
            float y1 = vBuf.get(i1 * COMPONENTS + 1);
            float z1 = vBuf.get(i1 * COMPONENTS + 2);
            float x2 = vBuf.get(i2 * COMPONENTS + 0);
            float y2 = vBuf.get(i2 * COMPONENTS + 1);
            float z2 = vBuf.get(i2 * COMPONENTS + 2);
            float x3 = vBuf.get(i3 * COMPONENTS + 0);
            float y3 = vBuf.get(i3 * COMPONENTS + 1);
            float z3 = vBuf.get(i3 * COMPONENTS + 2);

            GeometryUtils.normal(x1, y1, z1, x2, y2, z2, x3, y3, z3, normal);

            vBuf.put(i1 * COMPONENTS + 5, vBuf.get(i1 * COMPONENTS + 5) + normal.x);
            vBuf.put(i1 * COMPONENTS + 6, vBuf.get(i1 * COMPONENTS + 6) + normal.y);
            vBuf.put(i1 * COMPONENTS + 7, vBuf.get(i1 * COMPONENTS + 7) + normal.z);

            vBuf.put(i2 * COMPONENTS + 5, vBuf.get(i2 * COMPONENTS + 5) + normal.x);
            vBuf.put(i2 * COMPONENTS + 6, vBuf.get(i2 * COMPONENTS + 6) + normal.y);
            vBuf.put(i2 * COMPONENTS + 7, vBuf.get(i2 * COMPONENTS + 7) + normal.z);

            vBuf.put(i3 * COMPONENTS + 5, vBuf.get(i3 * COMPONENTS + 5) + normal.x);
            vBuf.put(i3 * COMPONENTS + 6, vBuf.get(i3 * COMPONENTS + 6) + normal.y);
            vBuf.put(i3 * COMPONENTS + 7, vBuf.get(i3 * COMPONENTS + 7) + normal.z);
        }
        for(int i = 0; i != vBuf.position(); i += COMPONENTS) {
            float x = vBuf.get(i + 5);
            float y = vBuf.get(i + 6);
            float z = vBuf.get(i + 7);
            float l = Vector3f.length(x, y, z);

            vBuf.put(i + 5, x / l);
            vBuf.put(i + 6, y / l);
            vBuf.put(i + 7, z / l);
        }
    }
    
    public final void begin() throws Exception {
        pipeline.begin();
        getVAO().begin(pipeline.getVertexAttributes());
    }

    public void setTexture(Texture texture) {
        getPipeline().set(uTextureEnabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture, 0, texture);
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
        getPipeline().set(uModelIT, it.set(node.getModel()).invert().transpose());
    }

    public void setLights(Vector<Light> lights) {
        int count = Math.min(MAX_LIGHTS, lights.size());

        getPipeline().set(uLightCount, count);

        for(int i = 0; i != count; i++) {
            Light light = lights.get(i);

            if(light instanceof AmbientLight) {
                getPipeline().set(uLightType[i], AMBIENT_LIGHT);
            } else if(light instanceof DirectionalLight) {
                getPipeline().set(uLightType[i], DIRECTIONAL_LIGHT);
                getPipeline().set(uLightVector[i], ((DirectionalLight)light).getLightDirection());
            } else if(light instanceof PointLight) {
                getPipeline().set(uLightType[i], POINT_LIGHT);
                getPipeline().set(uLightVector[i], light.getAbsolutePosition());
                getPipeline().set(uLightRange[i], ((PointLight)light).getRange());
            } else {
                continue;
            }
            getPipeline().set(uLightColor[i], light.getColor());
        }
    }

    public final void end() {
        getVAO().end();
        pipeline.end();
    }

    @Override
    public final void destroy() throws Exception {
        pipeline.destroy();
        super.destroy();
    }
}
