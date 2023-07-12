package org.jgl3.demo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.CullState;
import org.jgl3.DepthState;
import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.RenderTarget;
import org.jgl3.Renderer;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.jgl3.scene.Animator;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.Renderable;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Editor extends Demo {

    private static Editor instance = null;

    public static Editor getInstance() {
        return instance;
    }

    public static interface Tools {
        Node handleUI(boolean reset) throws Exception;
    }

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
    private static final int TEXTURE = 5;
    private static final int ANIMATOR = 6;
    private static final int TOOLS = 7;

    private Scene scene = null;
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
    private int selTexture = -1;
    private int selAnimator = -1;
    private int editor = -1;
    private String sceneName = "";
    private boolean resetNodeEditor = false;
    private boolean resetSceneEditor = false;
    private boolean resetToolsEditor = false;
    private File sceneFile = null;
    private Node selection = null;
    private final Vector<String> extensions = new Vector<>();
    private final Vector<File> renderableFiles = new Vector<>();
    private final Vector<String> renderableNames = new Vector<>();
    private final Vector<String> sceneNames = new Vector<>();
    private final Vector<String> textureNames = new Vector<>();
    private final Vector<File> textureFiles = new Vector<>();
    private final Vector<String> animatorNames = new Vector<>();
    private final Vector<File> animatorFiles = new Vector<>();
    private boolean down = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final BoundingBox bounds = new BoundingBox();
    private final Matrix4f matrix = new Matrix4f();
    private final Triangle triangle = new Triangle();
    private Node clipboard = null;
    private RenderTarget renderTarget = null;
    private final Tools tools;

    public Editor(Tools tools) {
        this.tools = tools;

        instance = this;
    }

    public Scene getScene() {
        return scene;
    }

    public Node getSelection() {
        return selection;
    }

    public File getSceneFile() {
        return sceneFile;
    }

    @Override
    public void init() throws Exception {
        scene = null;
        mode = 0;
        selScene = -1;
        selRenderable = -1;
        selAnimator = -1;
        editor = -1;
        sceneName = "";
        resetNodeEditor = false;
        resetSceneEditor = false;
        resetToolsEditor = false;
        sceneFile = null;
        selection = null;
        clipboard = null;
        down = false;
        renderTarget = null;

        extensions.clear();
        extensions.addAll(Game.getInstance().getAssets().getExtensionsForType(Scene.ASSET_TAG));

        renderableFiles.clear();
        renderableNames.clear();
        textureFiles.clear();
        textureNames.clear();
        animatorFiles.clear();
        animatorNames.clear();
        populateFileLists(IO.file("assets"));
        renderableFiles.sort((a, b) -> a.getName().compareTo(b.getName()));
        for(File file : renderableFiles) {
            renderableNames.add(file.getName());
        }
        textureFiles.sort((a, b) -> a.getName().compareTo(b.getName()));
        for(File file : textureFiles) {
            textureNames.add(IO.fileNameWithOutExtension(file));
        }
        animatorFiles.sort((a, b) -> a.getName().compareTo(b.getName()));
        for(File file : animatorFiles) {
            animatorNames.add(IO.fileNameWithOutExtension(file));
        }
        populateScenes();
    }

    @Override
    public boolean run() throws Exception { 
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        UIManager ui = UIManager.getInstance();
        Font font = ui.getFont();
        int s = game.getScale();
        int h = game.getHeight();
        boolean quit = false;
        File loadSceneFile = null;
        Object result;
        float c = (UIManager.BACKGROUND.x == 0) ? 0.15f : 0.2f;

        if(renderTarget != null) {
            renderTarget.begin();
            scene.render(renderTarget.getAspectRatio());
            renderer.end();
            renderTarget.end();
        }

        GFX.clear(c, c, c, 1);
        renderer.begin();
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push("FPS=" + game.getFrameRate() + ((scene != null) ? ", TRI=" + scene.getTrianglesRendered() : ""), 5 * s, h - font.getCharHeight() * font.getScale() -  5 * s, 0, 1, 1, 1, 1);
        renderer.endTriangles();
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
            if(ui.button("Editor.save.scene.button", 5, "Save", false)) {
                Scene.save(scene, sceneFile);
            }
            if(ui.button("Editor.edit.scene.button", 5, "Edit", editor == SCENE_EDITOR)) {
                editor = SCENE_EDITOR;
                resetSceneEditor = true;
            }
            if(clipboard != null) {
                if(ui.button("Editor.paste.node.button", 5, "Paste", false)) {
                    Node node = copy(clipboard);

                    selection = node;
                    scene.getRoot().addChild(scene, selection);
                    editor = NODE_EDITOR;
                    resetNodeEditor = true;
                }
            }
            if(ui.button("Editor.add.renderable.button", 5, "+Renderable", editor == RENDERABLES)) {
                editor = RENDERABLES;
                selRenderable = -1;
            }
            if(ui.button("Editor.add.light.button", 5, "+Light", quit)) {
                selection = new Node();
                selection.setLight(true);
                selection.setName("light");
                scene.getRoot().addChild(scene, selection);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            if(ui.button("Editor.zero.targ.button", 5, "Z Targ", false)) {
                Vector3f x = scene.getCamera().getOffset();

                scene.getCamera().getTarget().zero();
                scene.getCamera().getTarget().add(x, scene.getCamera().getEye());
            }
            for(int i = 0; i != modes.length; i++) {
                if(ui.button("Editor.mode.button." + i, 5, modes[i], i == mode)) {
                    mode = i;
                } 
            }
            if(ui.button("Editor.tools.button", 5, "Tools", editor == TOOLS)) {
                editor = TOOLS;
                resetToolsEditor = true;
            }
            if(ui.button("Editor.dark.button", 5, "Dark", UIManager.BACKGROUND.x == 0)) {
                float x = UIManager.BACKGROUND.x;

                UIManager.BACKGROUND.set(UIManager.FOREGROUND);
                UIManager.FOREGROUND.set(x, x, x, 1);
            }
            ui.addRow(5);
            renderTarget = ui.beginView("Editor.scene.view", 0, 100, 100, renderTarget, (editor == -1) ? 5 : 275, (selection == null) ? 10 + 12 : 50);
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
                        int th = renderTarget.getHeight();
                        int tw = renderTarget.getWidth();
                        int x = ui.getViewMouseX();
                        int y = th - ui.getViewMouseY() - 1;

                        scene.getCamera().unProject(x, y, 0, 0, 0, tw, th, origin);
                        scene.getCamera().unProject(x, y, 1, 0, 0, tw, th, direction);
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
                                Node uiNode = scene.getUI();

                                uiNode.getPosition().set(n.getAbsolutePosition());
                                uiNode.getScale().set(2, 2, 2);
                                uiNode.calcBoundsAndTransform(scene.getCamera());
                                if(bounds.touches(uiNode.getBounds())) {
                                    for(int i = 0; i != uiNode.getTriangleCount(); i++) {
                                        uiNode.getTriangle(i, triangle).transform(matrix);
                                        if(triangle.getNormal().dot(direction) < 0) {
                                            if(triangle.intersects(origin, direction, 0, time)) {
                                                selection = n;
                                            }
                                        }
                                    }
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
                    int snap = Math.max(0, scene.getSnap());

                    if(snap > 0) {
                        Vector3f p = selection.getPosition();

                        p.x = (int)Math.floor(p.x / snap) * snap;
                        p.y = (int)Math.floor(p.y / snap) * snap;
                        p.z = (int)Math.floor(p.z / snap) * snap;
                    }
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
                    Vector3f offset = scene.getCamera().getOffset();

                    scene.getCamera().getTarget().set(selection.getPosition());
                    scene.getCamera().getTarget().add(offset, scene.getCamera().getEye());
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
                if(ui.button("Editor.copy.node.button", 5, "Copy", false)) {
                    clipboard = copy(selection);
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
                        parent.detachFromParent(scene);
                    } else {
                        selection.detachFromParent(scene);
                    }
                    editor = -1;
                    selection = null;
                }
            }
        } 
        if(scene != null) {
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
                loadSceneFile = IO.file(IO.file("assets/scenes"), sceneNames.get((Integer)result) + ".scn");
            }
            selScene = -2;
        } else if(editor == TEXTURE) {
            if((result = ui.list("Editor.textures.list", 0, textureNames, 25, 10, selTexture)) != null) {
                selection.setTexture(game.getAssets().load(textureFiles.get((Integer)result)));
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            selTexture = -2;
        } else if(editor == ANIMATOR) {
            if((result = ui.list("Editor.animator.list", 0, animatorNames, 25, 10, selAnimator)) != null) {
                Animator animator = game.getAssets().load(animatorFiles.get((Integer)result));

                selection.setAnimator(scene, animator);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            selAnimator = -2;
        } else if(editor == RENDERABLES) {
            if((result = ui.list("Editor.renderables.list", 0, renderableNames, 25, 20, selRenderable)) != null) {
                File file = renderableFiles.get((Integer)result);
                Object asset = game.getAssets().load(file);
                Node node;

                if(asset instanceof Node) {
                    node = copy((Node)asset);
                } else {
                    File textureFile = IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".png");

                    node = new Node();

                    node.setRenderable(game.getAssets().load(file));
                    node.setRenderable(node.getRenderable().newInstance());
                    if(textureFile.exists()) {
                        node.setTexture(game.getAssets().load(textureFile));
                    } else {
                        textureFile = IO.file(file.getParentFile(), "colors.png");
                        if(textureFile.exists()) {
                            node.setTexture(game.getAssets().load(textureFile));
                        }
                    }
                    if(node.getTexture() != null) {
                        node.setTextureLinear(false);
                        node.setClampTextureToEdge(false);
                    }
                    if(IO.extension(file).equals(".md2")) {
                        Node parent = new Node();
                        KeyFrameMesh mesh = (KeyFrameMesh)node.getRenderable();

                        node.getPosition().y -= mesh.getFrame(0).getBounds().getMin().z;
                        node.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
                        parent.addChild(scene, node);
                        node = parent;
                    } else if(IO.extension(file).equals(".par")) {
                        node.setVertexColorEnabled(true);
                    }
                    node.setName(IO.fileNameWithOutExtension(file));
                }
                selection = node;
                scene.getRoot().addChild(scene, selection);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            selRenderable = -2;
        } else if(editor == TOOLS) {
            Node node = tools.handleUI(resetToolsEditor);
            if(node != selection) {
                selection = node;
                if(selection == null) {
                    editor = -1;
                } else {
                    editor = NODE_EDITOR;
                    resetNodeEditor = true;
                }
            }
            resetToolsEditor = false;
        } else if(editor == SCENE_EDITOR) {
            if((result = ui.textField("Editor.scene.editor.snap.field", 0, "Snap", scene.getSnap(), resetSceneEditor, 6)) != null) {
                scene.setSnap(Math.max(0, (Integer)result));
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
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.ao.strength.field", 0, "AO Strength", scene.getAOStrength(), resetSceneEditor, 10)) != null) {
                scene.setAOStrength((Float)result);
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.ao.length.field", 0, "AO Length", scene.getAOLength(), resetSceneEditor, 10)) != null) {
                scene.setAOLength((Float)result);
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.sample.radius.field", 0, "Sample Radius", scene.getSampleRadius(), resetSceneEditor, 10)) != null) {
                scene.setSampleRadius((Float)result);
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.sample.count.field", 0, "Sample Count", scene.getSampleCount(), resetSceneEditor, 10)) != null) {
                scene.setSampleCount((Integer)result);
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.lm.width.field", 0, "LM Width", scene.getLightMapWidth(), resetSceneEditor, 10)) != null) {
                scene.setLightMapWidth(Math.max(64, Math.min(4096, (Integer)result)));
            }
            ui.addRow(5);
            if((result = ui.textField("Editor.scene.lm.height.field", 0, "LM Height", scene.getLightMapHeight(), resetSceneEditor, 10)) != null) {
                scene.setLightMapHeight(Math.max(64, Math.min(4096, (Integer)result)));
            }
            ui.addRow(5);
            if(ui.button("Editor.scene.lm.lambert.button", 0, "Light Map Lambert", scene.isLightMapLambert())) {
                scene.setLightMapLambert(!scene.isLightMapLambert());
            }
            ui.addRow(5);
            if(ui.button("Editor.scene.map.button", 0, "Map", false)) {
                File mapFile = IO.file(sceneFile.getParentFile(), IO.fileNameWithOutExtension(sceneFile) + ".png");
                LightMapper lightMapper = new LightMapper();

                lightMapper.map(scene, mapFile, true);
            }
            if(ui.button("Editor.scene.map.clear.button", 5, "Clear Map", false)) {
                scene.getRoot().traverse((n) -> {
                    n.setTexture2(null);
                    return true;
                });
            }
            Node node = scene.getRoot().find((n) -> {
                Texture texture = n.getTexture2();
                if(texture != null) {
                    return true;
                }
                return false;
            }, true);
            boolean linear = (node == null) ? true : node.isTexture2Linear();
            if(ui.button("Editor.scene.map.linear.button", 5, "Linear", linear)) {
                scene.getRoot().traverse((n) -> {
                    n.setTexture2Linear(!n.isTexture2Linear());
                    return true;
                });
            }
            ui.addRow(5);
            if(ui.button("Editor.scene.texture.enabled", 0, "Texture Enabled", scene.isTextureEnabled())) {
                scene.setTextureEnabled(!scene.isTextureEnabled());
            }
            resetSceneEditor = false;
        } else if(editor == NODE_EDITOR) {
            Renderable renderable = selection.getRenderable();

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
            ui.addRow(5);
            if(ui.button("Editor.node.visible.button", 0, "Visible",selection.isVisible())) {
                selection.setVisible(!selection.isVisible());
            }
            if(ui.button("Editor.node.set.animator.button", 5, "Set Animator", false)) {
                Animator animator = selection.getAnimator();

                selAnimator = -1;
                if(animator != null) {
                    selAnimator = animatorFiles.indexOf(animator.getFile());
                }
                editor = ANIMATOR;
            }
            if(selection.getAnimator() != null) {
                ui.addRow(5);
                if(ui.button("Editor.node.init.animator.button", 0, "Init Animator", false)) {
                    selection.getAnimator().init(scene, selection);
                }
                if(ui.button("Editor.node.clear.animator.button", 5, "Clear Animator", false)) {
                    selection.setAnimator(scene, null);
                }
            }
            if(renderable != null || (selection.getVertexCount() != 0 && selection.getIndexCount() != 0)) {
                ui.addRow(5);
                ui.textField("Editor.node.editor.ambient.color.field", 0, "Ambient", selection.getAmbientColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.diffuse.color.field", 0, "Diffuse", selection.getDiffuseColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.color.field", 0, "Color", selection.getColor(), resetNodeEditor, 20);
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.z.order.field", 0, "Z Order", selection.getZOrder(), resetNodeEditor, 10)) != null) {
                    selection.setZOrder((Integer)result);
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.triangle.tag.field", 0, "Triangle Tag", selection.getTriangleTag(), resetNodeEditor, 10)) != null) {
                    selection.setTriangleTag((Integer)result);
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
                ui.addRow(5);
                if(ui.button("Editor.node.set.tex.button", 0, "Set Tex", false)) {
                    Texture texture = selection.getTexture();

                    selTexture = -1;
                    if(texture != null) {
                        selTexture = textureFiles.indexOf(texture.getFile());
                    }
                    editor = TEXTURE;
                }
                if(selection.getTexture() != null) {
                    if(ui.button("Editor.node.clear.tex.button", 5, "Clear Tex", false)) {
                        selection.setTexture(null);
                    }
                    ui.addRow(5);
                    if(ui.button("Editor.node.editor.textureLinear.button", 0, "Linear", selection.isTextureLinear())) {
                        selection.setTextureLinear(!selection.isTextureLinear());
                        scene.getRoot().traverse((n) -> {
                            Texture texture = n.getTexture();

                            if(texture == selection.getTexture() && texture != null) {
                                n.setTextureLinear(selection.isTextureLinear());
                            }
                            return true;
                        });
                    }
                    ui.addRow(5);
                    if((result = ui.textField("Editor.node.tex.unit.field", 0, "Tex Unit", selection.getTextureUnit(), resetNodeEditor, 10)) != null) {
                        selection.setTextureUnit((Integer)result);
                    }
                }
                if(selection.hasMesh()) {
                    ui.addRow(5);
                    if(ui.button("Editor.node.light.map.enabled.button", 0, "L Map", selection.isLightMapEnabled())) {
                        selection.setLightMapEnabled(!selection.isLightMapEnabled());
                    }
                    if(ui.button("Editor.node.casts.shadow.button", 5, "C Shadow", selection.getCastsShadow())) {
                        selection.setCastsShadow(!selection.getCastsShadow());
                    }
                    if(ui.button("Editor.node.receives.shadow.button", 5, "R Shadow", selection.getReceivesShadow())) {
                        selection.setReceivesShadow(!selection.getReceivesShadow());
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
                ui.addRow(5);
                if(ui.button("Editor.node.warp.enabled.button", 0, "Warp Enabled", selection.isWarpEnabled())) {
                    selection.setWarpEnabled(!selection.isWarpEnabled());
                }
                ui.addRow(5);
                ui.textField("Editor.node.warp.amplitude.field", 0, "Warp amplitude", selection.getWarpAmplitude(), resetNodeEditor, 15);
                ui.addRow(5);
                if((result = ui.textField("Editor.node.warp.speed.field", 0, "Warp Speed", selection.getWarpSpeed(), resetNodeEditor, 15)) != null) {
                    selection.setWarpSpeed((Float)result);
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.warp.freq.field", 0, "Warp Frequency", selection.getWarpFrequency(), resetNodeEditor, 15)) != null) {
                    selection.setWarpFrequency((Float)result);
                }
                if(renderable instanceof KeyFrameMesh) {
                    KeyFrameMesh mesh = (KeyFrameMesh)renderable;

                    ui.addRow(5);
                    if((result = ui.textField("Editor.node.seq.field", 0, "Sequence", "" + mesh.getStart() + " " + mesh.getEnd() + " " + mesh.getSpeed(), resetNodeEditor, 15)) != null) {
                        String[] tokens = ((String)result).split("\\s+");

                        if(tokens.length == 3) {
                            try {
                                int start = Integer.parseInt(tokens[0]);
                                int end = Integer.parseInt(tokens[1]);
                                int speed = Integer.parseInt(tokens[2]);

                                mesh.setSequence(start, end, speed, true);

                                scene.getRoot().traverse((n) -> {
                                    Renderable r = n.getRenderable();

                                    if(r instanceof KeyFrameMesh) {
                                        KeyFrameMesh m = (KeyFrameMesh)r;

                                        m.reset();
                                    }
                                    return true;
                                });
                            } catch(Exception ex) {
                            }
                        }
                    }
                } 
            } else if(selection.isLight()) {
                ui.addRow(5);
                ui.textField("Editor.node.editor.light.color.field", 0, "Color", selection.getLightColor(), resetNodeEditor, 20);
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.light.radius.field", 0, "Radius", selection.getLightRadius(), resetNodeEditor, 10)) != null) {
                    selection.setLightRadius((Float)result);
                }
            } 
            resetNodeEditor = false;
        }
        ui.end();
        renderer.end();

        if(loadSceneFile != null) {
            File file = loadSceneFile;

            renderTarget = null;
            game.getAssets().clear();
            scene = null;
            sceneFile = null;
            scene = Scene.load(file, new LightMapper());
            scene.loadUI();
            sceneFile = file;
            selection = null;
            clipboard = null;
            editor = -1;
        }

        if(scene != null) {
            scene.updateAnimators();
        }

        return !quit;
    }
    
    private void populateFileLists(File directory) {
        File[] files = directory.listFiles();

        if(files != null) {
            for(File file : files) {
                if(!file.isDirectory()) {
                    String extension = IO.extension(file);

                    if(extensions.contains(extension)) {
                        renderableFiles.add(file);
                    } else if(
                        extension.equals(".png") ||
                        extension.equals(".jpg") ||
                        extension.equals(".tex")
                    ) {
                        textureFiles.add(file);
                    } else if(extension.equals(".ani")) {
                        animatorFiles.add(file);
                    }
                }
            }
            for(File file : files) {
                if(file.isDirectory()) {
                    populateFileLists(file);
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

    private Node copy(Node node) throws Exception {
        ObjectOutputStream out = null;
        ObjectInputStream in = null;
        ByteArrayOutputStream outBytes = null;
        Node copy = null;
        try {
            out = new ObjectOutputStream(outBytes = new ByteArrayOutputStream(1000));
            out.writeObject(node);
            try {
                in = new ObjectInputStream(new ByteArrayInputStream(outBytes.toByteArray()));
                copy = (Node)in.readObject();
            } finally {
                if(in != null) {
                    in.close();
                }
            }
        } finally {
            if(out != null) {
                out.close();
            }
        }
        return copy;
    }
}
