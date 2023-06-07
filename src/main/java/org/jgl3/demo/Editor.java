package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.CullState;
import org.jgl3.DepthState;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.PixelFormat;
import org.jgl3.RenderTarget;
import org.jgl3.Renderer;
import org.jgl3.Triangle;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeLoader;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.joml.Vector3f;

public class Editor extends Demo {

    private static final int ZOOM = 0;
    private static final int ROT = 1;
    private static final int PANXZ = 2;
    private static final int PANY = 3;
    private static final int SEL = 4;
    private static final int MOVXZ = 5;
    private static final int MOVY = 6;
    private static final int ROTY = 7;

    private static final int ADD_SCENE = 0;
    private static final int SCENES = 1;
    private static final int SCENE_EDITOR = 2;
    private static final int NODE_EDITOR = 3;
    private static final int RENDERABLES = 4;

    private Scene scene = null;
    private RenderTarget renderTarget = null;
    private int mode = 0;
    private String[] modes = new String[] {
        "Zoom",
        "Rot", 
        "PanXZ",
        "PanY",
        "Sel",
        "MovXZ",
        "MovY",
        "RotY"
    };
    private int selScene = -1;
    private int selRenderable = -1;
    private int editor = -1;
    private String sceneName = "";
    private boolean resetNodeEditor = false;
    private boolean resetSceneEditor = false;
    private File sceneFile = null;
    private Node selection = null;
    private final Vector<File> renderableFiles = new Vector<>();
    private final Vector<String> renderableNames = new Vector<>();
    private final Vector<String> sceneNames = new Vector<>();
    private boolean down = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final BoundingBox bounds = new BoundingBox();
    private final Triangle triangle = new Triangle();

    @Override
    public void init() throws Exception {
        scene = null;
        renderTarget = null;
        mode = 0;
        selScene = -1;
        selRenderable = -1;
        editor = -1;
        sceneName = "";
        resetNodeEditor = false;
        resetSceneEditor = false;
        sceneFile = null;
        selection = null;
        down = false;

        renderableFiles.clear();
        renderableNames.clear();
        popuplateRenderables(IO.file("assets"));
        renderableFiles.sort((a, b) -> a.getPath().compareTo(b.getPath()));
        for(File file : renderableFiles) {
            renderableNames.add(file.getName());
        }
        populateScenes();
    }

    @Override
    public boolean run() throws Exception { 
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        UIManager ui = game.getUI();
        boolean quit = false;
        Object result;

        if(renderTarget != null) {
            renderTarget.begin();
            scene.render(renderTarget.getAspectRatio());
            renderer.end();
            renderTarget.end();
        }

        GFX.clear(0.2f, 0.2f, 0.2f, 1);
        renderer.begin();
        ui.begin();
        if(ui.button("Editor.quit.button", 0, "Quit", false)) {
            quit = true;
        }
        if(ui.button("Editor.plus.scene.button", 5, "+Scene", editor == ADD_SCENE)) {
            editor = ADD_SCENE;
        }
        if(ui.button("Editor.scenes.button", 5, "Scenes", editor == SCENES)) {
            editor = SCENES;
            selScene = -1;
        }
        if(scene != null) {
            if(ui.button("Editor.save.scene.button", 5, "Save Scene", false)) {
                Scene.save(scene, sceneFile);
            }
            if(ui.button("Editor.edit.scene.button", 5, "Edit Scene", editor == SCENE_EDITOR)) {
                editor = SCENE_EDITOR;
                resetSceneEditor = true;
            }
            if(ui.button("Editor.add.renderable.button", 5, "+Renderable", editor == RENDERABLES)) {
                editor = RENDERABLES;
                selRenderable = -1;
            }
            if(ui.button("Editor.add.light.button", 5, "+Light", quit)) {
                selection = new Node();
                selection.setLight(true);
                selection.setName("light");
                scene.getRoot().addChild(selection);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            for(int i = 0; i != modes.length; i++) {
                if(ui.button("Editor.mode.button." + i, 5, modes[i], i == mode)) {
                    mode = i;
                } 
            }
            ui.addRow(5);
            ui.beginView("Editor.scene.view", 0, renderTarget.getTexture(0));
            if(ui.isViewButtonDown()) {
                if(mode == ZOOM) {
                    scene.getCamera().zoom(game.getDY());
                } else if(mode == ROT) {
                    scene.getCamera().rotateAroundTarget(-game.getDX() * 0.025f, game.getDY() * 0.025f);
                } else if(mode == PANXZ) {
                    scene.getCamera().move(scene.getCamera().getTarget(), game.getDX(), -game.getDY());
                } else if(mode == PANY) {
                    scene.getCamera().move(scene.getCamera().getTarget(), -game.getDY());
                } else if(mode == SEL) {
                    if(!down) {
                        int w = renderTarget.getWidth();
                        int h = renderTarget.getHeight();
                        int x = ui.getViewMouseX();
                        int y = h - ui.getViewMouseY() - 1;

                        scene.getCamera().unProject(x, y, 0, 0, 0, w, h, origin);
                        scene.getCamera().unProject(x, y, 1, 0, 0, w, h, direction);
                        direction.sub(origin).normalize();
                        time[0] = Float.MAX_VALUE;

                        selection = null;

                        scene.getRoot().traverse((n) -> {
                            bounds.clear();
                            bounds.add(origin);
                            bounds.add(point.set(direction).mul(time[0]).add(origin));
                            if(bounds.touches(n.getBounds())) {
                                for(int i = 0; i != n.getTriangleCount(); i++) {
                                    n.getTriangle(i, triangle);
                                    if(triangle.getNormal().dot(direction) < 0) {
                                        if(triangle.intersects(origin, direction, 0, time)) {
                                            selection = n;
                                        }
                                    }
                                }
                            }
                            if(n.isLight()) {
                                bounds.getMin().set(n.getAbsolutePosition()).sub(8, 8, 8);
                                bounds.getMax().set(n.getAbsolutePosition()).add(8, 8, 8);
                                if(bounds.intersects(origin, direction, time)) {
                                    selection = n;
                                }
                            }
                            return true;
                        });
                        if(selection == null) {
                            editor = -1;
                        } else {
                            editor = NODE_EDITOR;
                            resetNodeEditor = true;
                        }
                    }
                } else if(selection != null) {
                    if(mode == MOVXZ) {
                        scene.getCamera().move(selection.getPosition(), game.getDX(), -game.getDY());
                    } else if(mode == MOVY) {
                        scene.getCamera().move(selection.getPosition(), -game.getDY());
                    } else if(mode == ROTY && !selection.isLight()) {
                        selection.getRotation().rotate(game.getDX() * 0.025f, 0, 1, 0);
                    }
                }
                down = true;
            } else {
                if(down && selection != null && (mode == MOVXZ || mode == MOVY)) {
                    int snap = Math.max(1, scene.getSnap());
                    Vector3f p = selection.getPosition();

                    p.x = (int)Math.floor(p.x / snap) * snap;
                    p.y = (int)Math.floor(p.y / snap) * snap;
                    p.z = (int)Math.floor(p.z / snap) * snap;
                }
                down = false;
            }
            ui.endView();
            ui.addRow(5);
            if(selection != null) {
                if(ui.button("Editor.pos.to.target.button", 0, "Pos To Target", false)) {
                    selection.getPosition().set(scene.getCamera().getTarget());
                }
                if(ui.button("Editor.target.to.pos.button", 5, "Target To Pos", false)) {
                    scene.getCamera().getTarget().set(selection.getPosition());
                }
                if(ui.button("Editor.zero.pos.button", 5, "Zero Pos", false)) {
                    selection.getPosition().zero();
                }
                if(ui.button("Editor.rot.45.button", 5, "Rot 45", false)) {
                    selection.getRotation().rotate((float)Math.toRadians(45), 0, 1, 0);
                }
                if(ui.button("Editor.zero.rot.button", 5, "Zero Rot", false)) {
                    selection.getRotation().identity();
                }
                if(selection.getParent() != scene.getRoot()) {
                    if(ui.button("Editor.to.parent.button", 5, "To Parent", false)) {
                        selection = selection.getParent();
                        resetNodeEditor = true;
                        editor = NODE_EDITOR;
                    }
                }
                if(ui.button("Editor.delete.node.button", 5, "-Node", false)) {
                    Node parent = selection.getParent();

                    if(parent != scene.getRoot() && parent.getChildCount() == 1) {
                        parent.detachFromParent();
                    } else {
                        selection.detachFromParent();
                    }
                    editor = -1;
                    selection = null;
                }
            }
            ui.moveRightOf("Editor.scene.view", 5);
        } else {
            ui.addRow(5);
        }
        if(editor == ADD_SCENE) {
            if((result = ui.textField("Editor.scene.name.field", 0, "Name", "", false, 10)) != null) {
                sceneName = (String)result;
            }
            ui.addRow(5);
            if(ui.button("Editor.add.scene.button", 0, "Add", false)) {
                File file = IO.file(IO.file("assets/scenes"), sceneName + ".scn");

                if(!file.exists()) {
                    Scene.save(new Scene(), file);
                    populateScenes();
                    editor = -1;
                }
            }
        } else if(editor == SCENES) {
            if((result = ui.list("Editor.scenes.list", 0, sceneNames, 15, 10, selScene)) != null) {
                File file = IO.file(IO.file("assets/scenes"), sceneNames.get((Integer)result) + ".scn");

                game.getAssets().clear();
                scene = null;
                sceneFile = null;
                scene = Scene.load(file);
                scene.loadUI();
                createRenderTarget();
                sceneFile = file;
                selection = null;
                editor = -1;
            }
            selScene = -2;
        } else if(editor == RENDERABLES) {
            if((result = ui.list("Editor.renderables.list", 0, renderableNames, 25, 20, selRenderable)) != null) {
                Node node = new Node();
                File file = renderableFiles.get((Integer)result);

                if(IO.extension(file).equals(".obj")) {
                    node = NodeLoader.load(file);
                } else {
                    File textureFile = IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".png");

                    node.setRenderable(game.getAssets().load(file));
                    node.setRenderable(node.getRenderable().newInstance());
                    if(textureFile.exists()) {
                        node.setTexture(game.getAssets().load(textureFile));
                    }
                    if(IO.extension(file).equals(".md2")) {
                        Node parent = new Node();
                        KeyFrameMesh mesh = (KeyFrameMesh)node.getRenderable();

                        node.getPosition().y -= mesh.getFrame(0).getBounds().getMin().z;
                        node.getPosition().y -= 16;
                        node.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
                        node.getAmbientColor().set(0.2f, 0.2f, 0.6f, 1);
                        node.setLightingEnabled(true);
                        parent.addChild(node);
                        node = parent;
                    } else if(IO.extension(file).equals(".par")) {
                        node.setBlendState(BlendState.ADDITIVE);
                        node.setDepthState(DepthState.READONLY);
                        node.setVertexColorEnabled(true);
                        node.setZOrder(100);
                    }
                    node.setName(IO.fileNameWithOutExtension(file));
                }
                initNode(file, node);
                selection = node;
                scene.getRoot().addChild(selection);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            selRenderable = -2;
        } else if(editor == SCENE_EDITOR) {
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.editor.snap.field", 0, "Snap", scene.getSnap(), resetSceneEditor, 6)) != null) {
                scene.setSnap(Math.max(1, (Integer)result));
            }
            ui.addRow(5);
            if(ui.button("Editor.scene.editor.draw.lights.button", 0, "Draw Lights", scene.getDrawLights())) {
                scene.setDrawLights(!scene.getDrawLights());
            }
            if(ui.button("Editor.scene.editor.draw.axis.button", 5, "Draw Axis", scene.getDrawAxis())) {
                scene.setDrawAxis(!scene.getDrawAxis());
            }
            ui.addRow(5);
            ui.textField("Editor.scene.editor.background.color.field", 0, "Background", scene.getBackgroundColor(), resetSceneEditor, 20);
            resetSceneEditor = false;
        } else if(editor == NODE_EDITOR) {
            if((result = ui.textField("Editor.node.editor.name.field", 0, "Name", selection.getName(), resetNodeEditor, 20)) != null) {
                selection.setName((String)result);
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.node.editor.tag.field", 0, "Tag", selection.getTag(), resetNodeEditor, 20)) != null) {
                selection.setTag((String)result);
            }
            ui.addRow(5);
            ui.textField("Editor.node.editor.position.field", 0, "Position", selection.getPosition(), resetNodeEditor, 20);
            ui.addRow(5);
            ui.textField("Editor.node.editor.scale.field", 0, "Scale", selection.getScale(), resetNodeEditor, 20);
            if(selection.hasMesh()) {
                ui.addRow(5);
                ui.textField("Editor.node.editor.ambient.color.field", 0, "Ambient", selection.getAmbientColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.diffuse.color.field", 0, "Diffuse", selection.getDiffuseColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.color.field", 0, "Color", selection.getColor(), resetNodeEditor, 20);
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.triangle.tag.field", 0, "Triangle Tag", selection.getTriangleTag(), resetNodeEditor, 10)) != null) {
                    selection.setTriangleTag((Integer)result);
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.z.order.field", 0, "Z Order", selection.getZOrder(), resetNodeEditor, 10)) != null) {
                    selection.setZOrder((Integer)result);
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.collidable.button", 0, "Collidable", selection.isCollidable())) {
                    selection.setCollidable(!selection.isCollidable());
                }
                if(ui.button("Editor.node.editor.dynamic.button", 5, "Dynamic", selection.isDynamic())) {
                    selection.setDynamic(!selection.isDynamic());
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.lighting.enabled.button", 0, "Lit", selection.isLightingEnabled())) {
                    selection.setLightingEnabled(!selection.isLightingEnabled());
                }
                if(ui.button("Editor.node.editor.light.map.enabled.button", 5, "Light Mapped", selection.isLightMapEnabled())) {
                    selection.setLightMapEnabled(!selection.isLightMapEnabled());
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.casts.shadow.button", 0, "Casts Shadow", selection.getCastsShadow())) {
                    selection.setCastsShadow(!selection.getCastsShadow());
                }
                if(ui.button("Editor.node.editor.receives.shadow.button", 5, "Receives Shadow", selection.getReceivesShadow())) {
                    selection.setReceivesShadow(!selection.getReceivesShadow());
                }
                ui.addRow(5);
                if(selection.getTexture() != null) {
                    if(ui.button("Editor.node.editor.textureLinear.button", 0, "Linear", selection.isTextureLinear())) {
                        selection.setTextureLinear(!selection.isTextureLinear());
                        if(selection.isTextureLinear()) {
                            selection.getTexture().toLinear(false);
                        } else {
                            selection.getTexture().toNearest(false);
                        }
                    }
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.opaque.button", 0, "Opaque", selection.getBlendState() == BlendState.OPAQUE)) {
                    selection.setBlendState(BlendState.OPAQUE);
                    selection.setDepthState(DepthState.READWRITE);
                }
                if(ui.button("Editor.node.editor.additive.button", 5, "Additive", selection.getBlendState() == BlendState.ADDITIVE)) {
                    selection.setBlendState(BlendState.ADDITIVE);
                    selection.setDepthState(DepthState.READONLY);
                }
                if(ui.button("Editor.node.editor.alpha.button", 5, "Alpha", selection.getBlendState() == BlendState.ALPHA)) {
                    selection.setBlendState(BlendState.ALPHA);
                    selection.setDepthState(DepthState.READONLY);
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.cull.enabled.button", 0, "Cull Enabled", selection.getCullState() != CullState.NONE)) {
                    selection.setCullState((selection.getCullState() == CullState.NONE) ? CullState.BACK : CullState.NONE);
                }
            } else if(selection.isLight()) {
                ui.addRow(5);
                ui.textField("Editor.node.editor.light.color.field", 0, "Color", selection.getLightColor(), resetNodeEditor, 20);
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.light.radius.field", 0, "Radius", selection.getLightRadius(), resetNodeEditor, 10)) != null) {
                    selection.setLightRadius((Float)result);
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.light.sample.radius.field", 0, "Sample Radius", selection.getLightSampleRadius(), resetNodeEditor, 10)) != null) {
                    selection.setLightSampleRadius((Float)result);
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.light.sample.count.field", 0, "Samples", selection.getLightSampleCount(), resetNodeEditor, 10)) != null) {
                    selection.setLightSampleCount(Math.max(1, (Integer)result));
                }
            } 
            resetNodeEditor = false;
        }
        ui.end();
        renderer.end();

        if(scene != null) {
            scene.getRoot().traverse((n) -> {
                updateNode(n);
                return true;
            });
        }

        return !quit;
    }

    protected void updateNode(Node node) throws Exception {
        if(node.getRenderable() instanceof KeyFrameMesh) {
            KeyFrameMesh mesh = (KeyFrameMesh)node.getRenderable();

            mesh.setSequence(0, 39, 9, true);
        } else if(node.getName().equals("fire-light")) {
            ParticleSystem particles = (ParticleSystem)node.getRenderable();

            particles.getPosition().y = (float)Math.sin(Game.getInstance().getTotalTime() * 2) * 25;
        }
    }

    protected void initNode(File file, Node node) throws Exception {
    }
    
    private void popuplateRenderables(File directory) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                if(!file.isDirectory()) {
                    String extension = IO.extension(file);

                    if(extension.equals(".obj") || extension.equals(".md2") || extension.equals(".par")) {
                        renderableFiles.add(file);
                    }
                }
            }
            for(File file : files) {
                if(file.isDirectory()) {
                    popuplateRenderables(file);
                }
            }
        }
    }

    private void populateScenes() {
        File[] files = IO.file("assets/scenes").listFiles();

        sceneNames.clear();
        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".scn")) {
                    sceneNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
        sceneNames.sort((a, b) -> a.compareTo(b));
    }

    private void createRenderTarget() throws Exception {
        Game game = Game.getInstance();
        int s = game.getScale();

        renderTarget = game.getAssets().manage(new RenderTarget(700 * s, 500 * s, PixelFormat.COLOR));
    }
}
