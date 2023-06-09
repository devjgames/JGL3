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
import org.jgl3.Renderer;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeLoader;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Renderable;
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
    private int selMatCap = -1;
    private int editor = -1;
    private String sceneName = "";
    private boolean resetNodeEditor = false;
    private boolean resetSceneEditor = false;
    private File sceneFile = null;
    private Node selection = null;
    private final Vector<File> renderableFiles = new Vector<>();
    private final Vector<String> renderableNames = new Vector<>();
    private final Vector<String> sceneNames = new Vector<>();
    private final Vector<String> matCapNames = new Vector<>();
    private boolean down = false;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final BoundingBox bounds = new BoundingBox();
    private final Triangle triangle = new Triangle();
    private Vector<String> extensions = new Vector<>();
    private Node clipboard = null;

    @Override
    public void init() throws Exception {
        scene = null;
        mode = 0;
        selScene = -1;
        selRenderable = -1;
        selMatCap = -1;
        editor = -1;
        sceneName = "";
        resetNodeEditor = false;
        resetSceneEditor = false;
        sceneFile = null;
        selection = null;
        clipboard = null;
        down = false;

        extensions = NodeLoader.extensions();

        matCapNames.clear();
        
        File[] files = IO.file("assets/matcap").listFiles();

        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".png")) {
                    matCapNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
        matCapNames.sort((a, b) -> a.compareTo(b));
        matCapNames.insertElementAt("NONE", 0);

        renderableFiles.clear();
        renderableNames.clear();
        popuplateRenderables(IO.file("assets"));
        renderableFiles.sort((a, b) -> a.getName().compareTo(b.getName()));
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
        Font font = ui.getFont();
        int s = game.getScale();
        int h = game.getHeight();
        boolean quit = false;
        Object result;

        GFX.clear(0.2f, 0.2f, 0.2f, 1);
        if(scene != null) {
            scene.render(game.getAspectRatio());
        } else {
            renderer.begin();
        }
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push("FPS = " + game.getFrameRate(), 5 * s, h - font.getCharHeight() * font.getScale() -  5 * s, 0, 1, 1, 1, 1);
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
                    scene.getRoot().addChild(selection);
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
                scene.getRoot().addChild(selection);
                editor = NODE_EDITOR;
                resetNodeEditor = true;
            }
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
                        parent.detachFromParent();
                    } else {
                        selection.detachFromParent();
                    }
                    editor = -1;
                    selection = null;
                }
            }
        } 
        ui.addRow(5);
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
                sceneFile = file;
                selection = null;
                clipboard = null;
                editor = -1;
            }
            selScene = -2;
        } else if(editor == RENDERABLES) {
            if((result = ui.list("Editor.renderables.list", 0, renderableNames, 25, 20, selRenderable)) != null) {
                Node node = new Node();
                File file = renderableFiles.get((Integer)result);

                if(IO.extension(file).equals(".obj")) {
                    node = NodeLoader.load(file, true);
                    if(node.getChildCount() == 1) {
                        node.getChild(0).getPosition().set(scene.getCamera().getTarget());
                    }
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
                        File weaponFile = IO.file(mesh.getFile().getParentFile(), IO.fileNameWithOutExtension(mesh.getFile()) + "-weapon.md2");
                        File weaponTextureFile = IO.file(weaponFile.getParentFile(), IO.fileNameWithOutExtension(weaponFile) + ".png");

                        node.getPosition().y -= mesh.getFrame(0).getBounds().getMin().z;
                        node.getPosition().y -= 16;
                        node.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
                        if(weaponFile.exists()) {
                            Node weapon = new Node();

                            weapon.setRenderable(game.getAssets().load(weaponFile));
                            weapon.setRenderable(weapon.getRenderable().newInstance());
                            if(weaponTextureFile.exists()) {
                                weapon.setTexture(game.getAssets().load(weaponTextureFile));
                            }
                            node.addChild(weapon);
                        }
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
            Renderable renderable = selection.getRenderable();

            if(resetNodeEditor) {
                Texture matCap = selection.getMatCap();
                
                selMatCap = 0;
                if(matCap != null) {
                    if(matCap.getFile() != null) {
                        selMatCap = matCapNames.indexOf(IO.fileNameWithOutExtension(matCap.getFile()));
                    }
                }
            }

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
            if(selection.hasMesh() || renderable != null) {
                ui.addRow(5);
                ui.textField("Editor.node.editor.ambient.color.field", 0, "Ambient", selection.getAmbientColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.diffuse.color.field", 0, "Diffuse", selection.getDiffuseColor(), resetNodeEditor, 20);
                ui.addRow(5);
                ui.textField("Editor.node.editor.color.field", 0, "Color", selection.getColor(), resetNodeEditor, 20);
                ui.addRow(5);
                if((result = ui.list("Editor.node.matcap.list", 0, matCapNames, 25, 4, selMatCap)) != null) {
                    String name = matCapNames.get((Integer)result);

                    if(name.equals("NONE")) {
                        selection.setMatCap(null);
                    } else {
                        selection.setMatCap(game.getAssets().load(IO.file(IO.file("assets/matcap"), name + ".png")));
                    }
                }
                ui.addRow(5);
                if((result = ui.textField("Editor.node.editor.z.order.field", 0, "Z Order", selection.getZOrder(), resetNodeEditor, 10)) != null) {
                    selection.setZOrder((Integer)result);
                }
                if(selection.hasMesh() || renderable != null) {
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
                }
                ui.addRow(5);
                if(ui.button("Editor.node.editor.lighting.enabled.button", 0, "Lit", selection.isLightingEnabled())) {
                    selection.setLightingEnabled(!selection.isLightingEnabled());
                }
                if(selection.getTexture() != null) {
                    if(ui.button("Editor.node.editor.textureLinear.button", 5, "Linear", selection.isTextureLinear())) {
                        selection.setTextureLinear(!selection.isTextureLinear());
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
                if(renderable instanceof KeyFrameMesh) {
                    KeyFrameMesh mesh = (KeyFrameMesh)renderable;

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
                    ui.addRow(5);
                    if(ui.button("Editor.mesh.clear.tex.button", 0, "Clear Texture", false)) {
                        selection.setTexture(null);
                    }
                } else if(selection.hasMesh()) {
                    if(ui.button("Editor.mesh.warp.enabled.button", 0, "Warp Enabled", selection.isWarpEnabled())) {
                        selection.setWarpEnabled(!selection.isWarpEnabled());
                    }
                    ui.addRow(5);
                    ui.textField("Editor.mesh.warp.amplitude.field", 0, "Warp amplitude", selection.getWarpAmplitude(), resetNodeEditor, 15);
                    ui.addRow(5);
                    if((result = ui.textField("Editor.mesh.warp.speed.field", 0, "Warp Speed", selection.getWarpSpeed(), resetNodeEditor, 15)) != null) {
                        selection.setWarpSpeed((Float)result);
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
            selMatCap = -2;
        }
        boolean handled = ui.end();
        renderer.end();

        if(scene != null) {
            if(!handled) {
                if(game.isButtonDown(0)) {
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
                            int w = game.getWidth();
                            int x = game.getMouseX();
                            int y = h - game.getMouseY() - 1;

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
                                    Node uiNode = scene.getUI();

                                    uiNode.getPosition().set(n.getAbsolutePosition());
                                    uiNode.getScale().set(2, 2, 2);
                                    uiNode.calcBoundsAndTransform(scene.getCamera());
                                    if(bounds.touches(uiNode.getBounds())) {
                                        for(int i = 0; i != uiNode.getTriangleCount(); i++) {
                                            uiNode.getTriangle(i, triangle);
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
                        int snap = Math.max(1, scene.getSnap());
                        Vector3f p = selection.getPosition();

                        p.x = (int)Math.floor(p.x / snap) * snap;
                        p.y = (int)Math.floor(p.y / snap) * snap;
                        p.z = (int)Math.floor(p.z / snap) * snap;
                    }
                    down = false;
                }
            }
            scene.getRoot().traverse((n) -> {
                updateNode(n);
                return true;
            });
        }
        return !quit;
    }

    public void updateNode(Node node) throws Exception {
        fireLight(node);
    }

    public static void fireLight(Node node) {
        Renderable renderable = node.getRenderable();

        if(node.getName().equals("fire-light") && renderable instanceof ParticleSystem) {
            ParticleSystem particles = (ParticleSystem)renderable;

            particles.getPosition().y = (float)Math.sin(Game.getInstance().getTotalTime() * 2) * 30;
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

                    if(extensions.contains(extension) || extension.equals(".md2") || extension.equals(".par")) {
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
