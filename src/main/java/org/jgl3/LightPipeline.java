package org.jgl3;

import java.util.Vector;

import org.joml.Matrix4f;

public class LightPipeline extends MeshPipeline {

    public static final int COMPONENTS = 8;
    public static final int MAX_LIGHTS = 16;
    public static final int AMBIENT_LIGHT = 0;
    public static final int DIRECTIONAL_LIGHT = 1;
    public static final int POINT_LIGHT = 2;

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
        super(
            new Pipeline(
                IO.readAllBytes(LightPipeline.class, "/org/jgl3/glsl/VertexLightVertex.glsl"),
                IO.readAllBytes(LightPipeline.class, "/org/jgl3/glsl/VertexLightFragment.glsl"),
                "vsInPosition", 3, 
                "vsInTextureCoordinate", 2,
                "vsInNormal", 3
            )
        );

        getPipeline().setColorLocations("fsOutColor");

        for(int i = 0; i != MAX_LIGHTS; i++) {
            uLightVector[i] = getPipeline().getUniform("uLightVector[" + i +  "]");
            uLightColor[i] = getPipeline().getUniform("uLightColor[" + i  + "]");
            uLightRange[i] = getPipeline().getUniform("uLightRange[" + i + "]");
            uLightType[i] = getPipeline().getUniform("uLightType[" + i + "]");
        }
        uLightCount = getPipeline().getUniform("uLightCount");
        uProjection = getPipeline().getUniform("uProjection");
        uView = getPipeline().getUniform("uView");
        uModel = getPipeline().getUniform("uModel");
        uModelIT = getPipeline().getUniform("uModelIT");
        uColor = getPipeline().getUniform("uColor");
        uTexture = getPipeline().getUniform("uTexture");
        uTextureEnabled = getPipeline().getUniform("uTextureEnabled");
    }

    @Override
    public final int getComponents() {
        return COMPONENTS;
    }

    @Override
    public void setTexture(Texture texture) {
        getPipeline().set(uTextureEnabled, texture != null);
        if(texture != null) {
            getPipeline().set(uTexture, 0, texture);
        }
    }

    @Override
    public void setColor(float r, float g, float b, float a) {
        getPipeline().set(uColor, r, g, b, a);
    }
    
    @Override
    public void setTransform(Camera camera, Node node) {
        getPipeline().set(uProjection, camera.getProjection());
        getPipeline().set(uView, camera.getModel());
        getPipeline().set(uModel, node.getModel());
        getPipeline().set(uModelIT, it.set(node.getModel()).invert().transpose());
    }

    @Override
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
}
