package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.BoundingBox;
import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.RenderTarget;
import org.jgl3.Renderer;
import org.jgl3.Triangle;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.joml.Matrix4f;
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
    private static final int NODE = 8;

    private static final int ADD_SCENE = 0;
    private static final int SCENES = 1;
    private static final int NODE_EDITOR = 2;
    private static final int NODE_TYPE_EDITOR = 3;

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
        "RotY",
        "Node"
    };
    private int selScene = -1;
    private int selNodeType = -1;
    private int editor = -1;
    private String sceneName = "";
    private boolean resetNodeEditor = false;
    private boolean resetSnap = false;
    private File sceneFile = null;
    private Node selection = null;
    private final Vector<String> sceneNames = new Vector<>();
    private final Vector<Class<? extends Node>> nodeTypes = new Vector<>();
    private final Vector<String> nodeTypeNames = new Vector<>();
    private boolean down = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final BoundingBox bounds = new BoundingBox();
    private final Matrix4f matrix = new Matrix4f();
    private final Triangle triangle = new Triangle();
    private RenderTarget renderTarget = null;
    private boolean nodeEditorHasUI = false;

    @SuppressWarnings("unchecked")
    public Editor(String ... nodeTypes) throws Exception {
        for(String typeName : nodeTypes) {
            Class<? extends Node> type = (Class<? extends Node>)Class.forName(typeName);
            this.nodeTypes.add(type);
            nodeTypeNames.add(type.getSimpleName());
        }
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
        selNodeType = -1;
        editor = -1;
        sceneName = "";
        resetNodeEditor = false;
        sceneFile = null;
        selection = null;
        down = false;
        renderTarget = null;
        nodeEditorHasUI = false;
        resetSnap = false;

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
                scene.save(sceneFile);
            }
            if(ui.button("Editor.add.node.button", 5, "+Node", editor == NODE_TYPE_EDITOR)) {
                editor = NODE_TYPE_EDITOR;
                selNodeType = -1;
            }
            if(ui.button("Editor.zero.targ.button", 5, "Z Targ", false)) {
                Vector3f x = scene.getCamera().getOffset();

                scene.getCamera().getTarget().zero();
                scene.getCamera().getTarget().add(x, scene.getCamera().getEye());
            }
            if((result = ui.textField("Editor.snap.field", 5, "Snap", scene.getSnap(), resetSnap, 5)) != null) {
                scene.setSnap((Integer)result);
            }
            resetSnap = false;
            for(int i = 0; i != modes.length; i++) {
                if(ui.button("Editor.mode.button." + i, 5, modes[i], i == mode)) {
                    mode = i;
                } 
            }
            if(ui.button("Editor.dark.button", 5, "Dark", UIManager.BACKGROUND.x == 0)) {
                float x = UIManager.BACKGROUND.x;

                UIManager.BACKGROUND.set(UIManager.FOREGROUND);
                UIManager.FOREGROUND.set(x, x, x, 1);
            }
            ui.addRow(5);
            int rightAnchor = 5;
            int bottomAnchor = ui.getFont().getCharHeight() + 10;
            if(editor != -1) {
                if(editor == NODE_EDITOR) {
                    if(nodeEditorHasUI) {
                        rightAnchor = 275;
                    }
                } else {
                    rightAnchor = 275;
                }
            }
            if(selection != null) {
                bottomAnchor += 28;
            }
            renderTarget = ui.beginView("Editor.scene.view", 0, 100, 100, renderTarget, rightAnchor, bottomAnchor);
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

                        for(int i = 0; i != scene.getRoot().getChildCount(); i++) {
                            Node node = scene.getRoot().getChild(i);
                            
                            node.traverse((n) -> {
                                bounds.clear();
                                bounds.add(origin);
                                bounds.add(point.set(direction).mul(time[0]).add(origin));
                                if(bounds.touches(n.getBounds())) {
                                    for(int j = 0; j != n.getTriangleCount(); j++) {
                                        n.getTriangle(j, triangle);
                                        if(triangle.getNormal().dot(direction) < 0) {
                                            if(triangle.intersects(origin, direction, 0, time)) {
                                                selection = node;
                                            }
                                        }
                                    }
                                }
                                if(n.isLight()) {
                                    Node uiNode = scene.getUI();

                                    uiNode.getPosition().set(n.getAbsolutePosition());
                                    uiNode.calcBoundsAndTransform(scene.getCamera());
                                    if(bounds.touches(uiNode.getBounds())) {
                                        for(int j = 0; j != uiNode.getTriangleCount(); j++) {
                                            uiNode.getTriangle(j, triangle).transform(matrix);
                                            if(triangle.getNormal().dot(direction) < 0) {
                                                if(triangle.intersects(origin, direction, 0, time)) {
                                                    selection = node;
                                                }
                                            }
                                        }
                                    }
                                }
                                return true;
                            });
                        }
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
                    } else if(mode == NODE) {
                        selection.handleMouse(scene);
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
                if(ui.button("Editor.delete.node.button", 5, "-Node", false)) {
                    selection.detachFromParent();
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
                if(Scene.create(IO.file(IO.file("assets/scenes"), sceneName + ".txt"), true)) {
                    populateScenes();
                    editor = -1;
                }
            }
        } else if(editor == SCENES) {
            if((result = ui.list("Editor.scenes.list", 0, sceneNames, 15, 10, selScene)) != null) {
                loadSceneFile = IO.file(IO.file("assets/scenes"), sceneNames.get((Integer)result) + ".txt");
            }
            selScene = -2;
        } else if(editor == NODE_EDITOR) {
            nodeEditorHasUI = selection.handleUI(scene, resetNodeEditor);
            resetNodeEditor = false;
        } else if(editor == NODE_TYPE_EDITOR) {
            if((result = ui.list("Editor.node.type.list", 0, nodeTypeNames, 20, 8, selNodeType)) != null) {
                Node node = (Node)nodeTypes.get((Integer)result).getConstructors()[0].newInstance();

                scene.getRoot().addChild(node);
                node.init(scene);
                selection = node;
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
            selNodeType = -2;
        }
        ui.end();
        renderer.end();

        if(loadSceneFile != null) {
            File file = loadSceneFile;

            renderTarget = null;
            game.getAssets().clear();
            scene = null;
            sceneFile = null;
            scene = Scene.load(file, true, new LightMapper());
            scene.loadUI();
            sceneFile = file;
            selection = null;
            editor = -1;
            resetSnap = true;
        }

        if(scene != null) {
            scene.update();
        }

        return !quit;
    }

    private void populateScenes() {
        File[] files = IO.file("assets/scenes").listFiles();

        sceneNames.clear();
        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".txt")) {
                    sceneNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
        sceneNames.sort((a, b) -> a.compareTo(b));
    }
}
