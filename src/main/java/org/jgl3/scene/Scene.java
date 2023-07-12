package org.jgl3.scene;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.CullState;
import org.jgl3.DepthState;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Log;
import org.jgl3.Renderer;
import org.joml.Vector4f;

public final class Scene implements Serializable {

    private static final long serialVersionUID = 1234567L;

    public static final int ASSET_TAG = 1;

    private static final Vector<Node> renderables = new Vector<>();
    private static final Vector<Node> lights = new Vector<>();
    
    private final Node root = new Node();
    private final Vector4f backgroundColor = new Vector4f(0.25f, 0.25f, 0.25f, 1);
    private int snap = 1;
    private final Camera camera = new Camera();
    private int nodesRendered = 0;
    private int trianglesRendered = 0;
    private int collidableTriangles = 0;
    private boolean drawLights = true;
    private boolean drawAxis = true;
    private transient Node ui = null;
    private DepthState lastDepthState = null;
    private CullState lastCullState = null;
    private BlendState lastBlendState = null;
    private transient Scene me;
    private float aoStrength = 2;
    private float aoLength = 32;
    private float sampleRadius = 32;
    private int sampleCount = 64;
    private int lightMapWidth = 128;
    private int lightMapHeight = 128;
    private boolean lightMapLambert = true;
    private boolean textureEnabled = true;

    public Scene() {
        me = this;
    }

    public int getNodesRenderer() {
        return nodesRendered;
    }

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public int getCollidableTriangles() {
        return collidableTriangles;
    }

    public float getAOStrength() {
        return aoStrength;
    }

    public Scene setAOStrength(float strength) {
        aoStrength = strength;
        return this;
    }

    public float getAOLength() {
        return aoLength;
    }

    public Scene setAOLength(float length) {
        aoLength = length;
        return this;
    }

    public float getSampleRadius() {
        return sampleRadius;
    }

    public Scene setSampleRadius(float radius) {
        this.sampleRadius = radius;
        return this;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public Scene setSampleCount(int count) {
        sampleCount = count;
        return this;
    }

    public int getLightMapWidth() {
        return lightMapWidth;
    }

    public Scene setLightMapWidth(int width) {
        lightMapWidth = width;
        return this;
    }

    public int getLightMapHeight() {
        return lightMapHeight;
    }

    public Scene setLightMapHeight(int height) {
        lightMapHeight = height;
        return this;
    }

    public boolean isLightMapLambert() {
        return lightMapLambert;
    }

    public Scene setLightMapLambert(boolean lambert) {
        lightMapLambert = lambert;
        return this;
    }

    public boolean getDrawLights() {
        return drawLights;
    }

    public Scene setDrawLights(boolean drawLights) {
        this.drawLights = drawLights;
        return this;
    }

    public Node getRoot() {
        return root;
    }

    public Vector4f getBackgroundColor() {
        return backgroundColor;
    }

    public int getSnap() {
        return  snap;
    }

    public Scene setSnap(int snap) {
        this.snap = snap;
        return this;
    }

    public Camera getCamera() {
        return camera;
    }

    public boolean getDrawAxis() {
        return drawAxis;
    }

    public Scene setDrawAxis(boolean drawAxis) {
        this.drawAxis = drawAxis;
        return this;
    }

    public boolean isTextureEnabled() {
        return textureEnabled;
    }

    public Scene setTextureEnabled(boolean enabled) {
        textureEnabled = enabled;
        return this;
    }

    public Scene loadUI() throws Exception {
        Log.put(1, "Loading ui node ...");
        ui = Game.getInstance().getAssets().load(IO.file("assets/ui/ui.obj"));
        ui.getTexture().toLinear(true);
        return this;
    }

    public Node getUI() {
        return ui;
    }

    public Scene removeUI() {
        ui = null;
        return this;
    }

    public void updateAnimators() throws Exception {
        root.traverse((n) -> {
            Animator animator = n.getAnimator();

            if(animator != null) {
                animator.animate(me, n);
            }
            return true;
        });
    }

    public void render(float aspectRatio) throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();

        camera.calcTransforms(aspectRatio);

        nodesRendered = 0;
        trianglesRendered = 0;
        collidableTriangles = 0;

        root.traverse((n) -> {
            Renderable renderable = n.getRenderable();

            if(renderable != null) {
                renderable.update(this, n);
            }

            if(n.isCollidable()) {
                collidableTriangles += n.getTriangleCount();
            }

            if(n.isVisible()) {
                if(n.hasMesh() || renderable != null) {
                    if(camera.getFrustum().testAab(n.getBounds().getMin(), n.getBounds().getMax())) {
                        renderables.add(n);
                    }
                }
                if(n.isLight()) {
                    lights.add(n);
                }
                return true;
            }
            return false;
        });

        root.calcBoundsAndTransform(camera);

        renderables.sort((a, b) -> {
            if (a == b) {
                return 0;
            } else if (a.getZOrder() == b.getZOrder()) {
                float da = a.getAbsolutePosition().distance(camera.getEye());
                float db = b.getAbsolutePosition().distance(camera.getEye());
    
                return Float.compare(db, da);
            } else {
                return Integer.compare(a.getZOrder(), b.getZOrder());
            }
        });
        lights.sort((a, b) -> {
            if(a == b) {
                return 0;
            }

            float d1 = a.getAbsolutePosition().distance(camera.getTarget());
            float d2 = b.getAbsolutePosition().distance(camera.getTarget());

            return Float.compare(d1, d2);
        });

        GFX.clear(backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);

        renderer.begin();

        renderer.setProjection(camera.getProjection());
        renderer.setView(camera.getView());

        for(int i = 0; i != Renderer.MAX_LIGHTS; i++) {

            if(i < lights.size()) {
                Node light = lights.get(i);

                renderer.setLightPosition(i, light.getAbsolutePosition());
                renderer.setLightColor(i, light.getLightColor());
                renderer.setLightRadius(i, light.getLightRadius());
            }
        };

        renderer.setLightCount(Math.min(Renderer.MAX_LIGHTS, lights.size()));

        lastDepthState = null;
        lastBlendState = null;
        lastCullState = null;

        int i = 0;

        for(; i != renderables.size(); i++) {
            Node renderable = renderables.get(i);

            if(renderable.getZOrder() >= 0) {
                break;
            }
            render(game, renderer, renderable);
        }

        renderer.setVertexColorEnabled(false);
        renderer.setLightingEnabled(false);
        renderer.setTexture2(null);
        renderer.setTexture(null);
        renderer.setLayerColor(0, 0, 0, 0);
        renderer.setWarp(false);
        renderer.setTextureUnit(0);

        GFX.setDepthState(lastDepthState = DepthState.READWRITE);
        GFX.setBlendState(lastBlendState = BlendState.OPAQUE);
        GFX.setCullState(lastCullState = CullState.BACK);

        if(ui != null) {
            renderer.setColor(1, 1, 1, 1);
            renderer.setColor(ui.getColor());
            renderer.setTexture(ui.getTexture());
            if(drawLights) {
                for(Node light : lights) {
                    ui.getScale().set(2, 2, 2);
                    ui.getPosition().set(light.getAbsolutePosition());
                    ui.calcBoundsAndTransform(camera);
                    renderer.setModel(ui.getModel());
                    ui.render(this);
                }
            }
            if(drawAxis) {
                ui.getScale().set(2, 2, 2);
                ui.getPosition().set(camera.getTarget());
                ui.calcBoundsAndTransform(camera);
                renderer.setModel(ui.getModel());
                ui.render(this);
            }
        }

        for(; i != renderables.size(); i++) {
            render(game, renderer, renderables.get(i));
        }

        renderables.clear();
        lights.clear();
    }

    private void render(Game game, Renderer renderer, Node renderable) throws Exception {
        renderer.setModel(renderable.getModel());
        renderer.setLightingEnabled(renderable.isLightingEnabled());
        renderer.setVertexColorEnabled(renderable.isVertexColorEnabled());
        renderer.setTexture((textureEnabled) ? renderable.getTexture() : null);
        renderer.setTexture2(renderable.getTexture2());
        renderer.setAmbientColor(renderable.getAmbientColor());
        renderer.setDiffuseColor(renderable.getDiffuseColor());
        renderer.setColor(renderable.getColor());
        renderer.setLayerColor(renderable.getLayerColor());
        renderer.setWarp(renderable.isWarpEnabled());
        renderer.setWarpAmplitude(renderable.getWarpAmplitude());
        renderer.setWarpTime(game.getTotalTime() * renderable.getWarpSpeed());
        renderer.setWarpFrequency(renderable.getWarpFrequency());
        renderer.setTextureUnit(renderable.getTextureUnit());

        if(lastDepthState != renderable.getDepthState()) {
            GFX.setDepthState(lastDepthState = renderable.getDepthState());
        }
        if(lastCullState != renderable.getCullState()) {
            GFX.setCullState(lastCullState = renderable.getCullState());
        }
        if(lastBlendState != renderable.getBlendState()) {
            GFX.setBlendState(lastBlendState = renderable.getBlendState());
        }

        renderable.render(this);
        trianglesRendered += renderable.getTriangleCount();
        nodesRendered++;
    }

    @Override
    public String toString() {
        return "Scene";
    }

    private void initAnimators() throws Exception {
        getRoot().traverse((n) -> {
            Animator animator = n.getAnimator();

            if(animator != null) {
                animator.init(this, n);
            }
            return true;
        });
    }

    public static void save(Scene scene, File file) throws Exception {
        ObjectOutputStream output = null;

        try {
            output = new ObjectOutputStream(new FileOutputStream(file));
            output.writeObject(scene);
        } finally {
            if(output != null) {
                output.close();
            }
        }
    }

    public static Scene load(File file, LightMapper lightMapper) throws Exception {
        ObjectInputStream input = null;
        Scene scene = null;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            scene = (Scene)input.readObject();
            scene.me = scene;
            scene.initAnimators();

            lightMapper.map(scene, IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".png"), false);
        } finally {
            if(input != null) {
                input.close();
            }
        }
        return scene;
    }

    public static void create(File file) throws Exception {  
        if(!file.exists()) {
            Log.put(1, "Creating scene - " + file.getPath());
            save(new Scene(), file);
        }
    }

}
