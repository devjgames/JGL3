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

    private static final Vector<Node> renderables = new Vector<>();
    private static final Vector<Node> lights = new Vector<>();
    
    private final Node root = new Node();
    private final Vector4f backgroundColor = new Vector4f(0.25f, 0.25f, 0.25f, 1);
    private int snap = 1;
    private final Camera camera = new Camera();
    private int trianglesRendered = 0;
    private int collidableTriangles = 0;
    private boolean drawLights = true;
    private boolean drawAxis = true;
    private transient Node ui = null;
    private DepthState lastDepthState = null;
    private CullState lastCullState = null;
    private BlendState lastBlendState = null;

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public int getCollidableTriangles() {
        return collidableTriangles;
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

    public Scene loadUI() throws Exception {
        Log.put(1, "Loading ui node ...");
        ui = NodeLoader.load(IO.file("assets/ui/ui.obj"));
        return this;
    }

    public Scene removeUI() {
        ui = null;
        return this;
    }

    public void render(float aspectRatio) throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();

        camera.calcTransforms(aspectRatio);

        trianglesRendered = 0;
        collidableTriangles = 0;

        root.traverse((n) -> {
            if(n.isVisible()) {
                Renderable renderable = n.getRenderable();
                if(renderable != null || n.hasMesh()) {
                    if(renderable != null) {
                        renderable.update(this, n);
                    }
                    renderables.add(n);
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
        renderer.setLayerColor(0, 0, 0, 0);

        GFX.setDepthState(lastDepthState = DepthState.READWRITE);
        GFX.setBlendState(lastBlendState = BlendState.OPAQUE);
        GFX.setCullState(lastCullState = CullState.BACK);

        if(ui != null) {
            renderer.setTexture(ui.getTexture());
            if(drawLights) {
                for(Node light : lights) {
                    ui.getPosition().set(light.getAbsolutePosition());
                    ui.calcBoundsAndTransform(camera);
                    renderer.setModel(ui.getModel());
                    ui.renderMesh();
                }
            }
            if(drawAxis) {
                ui.getPosition().set(camera.getTarget());
                ui.calcBoundsAndTransform(camera);
                renderer.setModel(ui.getModel());
                ui.renderMesh();
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
        renderer.setTexture(renderable.getTexture());
        renderer.setTexture2(renderable.getTexture2());
        renderer.setAmbientColor(renderable.getAmbientColor());
        renderer.setDiffuseColor(renderable.getDiffuseColor());
        renderer.setColor(renderable.getColor());
        renderer.setLayerColor(renderable.getLayerColor());

        if(lastDepthState != renderable.getDepthState()) {
            GFX.setDepthState(lastDepthState = renderable.getDepthState());
        }
        if(lastCullState != renderable.getCullState()) {
            GFX.setCullState(lastCullState = renderable.getCullState());
        }
        if(lastBlendState != renderable.getBlendState()) {
            GFX.setBlendState(lastBlendState = renderable.getBlendState());
        }

        if(!renderable.isLightingEnabled() && renderable.hasMesh()) {
            for(int i = 0; i != renderable.getVertexCount(); i++) {
                for(int j = 10; j != 14; j++) {
                    renderable.setVertexComponent(i, j, 1);
                }
            }
        }

        int n = renderable.getTriangleCount();
        if(renderable.isCollidable()) {
            collidableTriangles += n;
        }
        if(renderable.hasMesh()) {
            renderable.renderMesh();
        } else {
            renderable.getRenderable().render(this, renderable);
        }
        trianglesRendered += n;
    }

    @Override
    public String toString() {
        return "Scene";
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

    public static Scene load(File file) throws Exception {
        ObjectInputStream input = null;
        Scene scene = null;

        try {
            input = new ObjectInputStream(new FileInputStream(file));
            scene = (Scene)input.readObject();
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
