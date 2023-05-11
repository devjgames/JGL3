package org.jgl3.demo;

import java.io.File;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.DepthState;
import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.Collider;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Editor {

    public static Node addPlayer(Scene scene, Collider collider) throws Exception {
        Game game = Game.getInstance();
        Node node = new Node();
        Node child = new Node();
        KeyFrameMesh mesh = game.getAssets().load(IO.file("assets/md2/hero.md2"));

        mesh.setSequence(0, 39, 10, true);

        child.setRenderable(mesh);
        child.getAmbientColor().set(0.2f, 0.2f, 0.6f, 1);
        child.setTexture(game.getAssets().load(IO.file("assets/md2/hero.png")));
        child.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
        child.getPosition().y -= mesh.getFrame(0).getBounds().getMin().z;
        child.getPosition().y -= collider.getRadius();
        child.setLightingEnabled(true);

        node.addChild(child);
        node.setName("player");

        scene.getRoot().addChild(node);

        return node;
    }

    private boolean reset = false;
    private boolean visible = false;
    private boolean down = false;
    private boolean handled = false;
    private Node selection = null;
    private final Vector3f origin = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector3f point = new Vector3f();
    private final float[] time = new float[1];
    private final BoundingBox bounds = new BoundingBox();

    private static final int PLAYER = 0;
    private static final int SEL = 1;
    private static final int DEL = 2;
    private static final int MOVXZ = 3;
    private static final int MOVY = 4;

    private final String[] modes = {
        "PLAYER",
        "SEL",
        "DEL",
        "MOVXZ",
        "MOVY"
    };
    private int mode = PLAYER;
    
    public boolean isVisible() {
        return visible;
    }

    public boolean isPlayerMode() {
        return mode == PLAYER && !handled;
    }
    
    public void run(Scene scene, Node player, Collider collider, File sceneFile) throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        UIManager ui = game.getUI();
        Font font = ui.getFont();
        int h = game.getHeight();
        int s = game.getScale();
        int ch = font.getCharHeight() * font.getScale();
        boolean hide = false;
        Object result;

        if(game.isKeyDown(GLFW.GLFW_KEY_E) && !visible) {
            scene.setDrawAxis(false);
            scene.loadUI();
            scene.getRoot().traverse((n) -> {
                n.resetBaseVertices();
                n.clearOctTree();
                return true;
            });
            visible = true;
        }

        scene.render(game.getAspectRatio());
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS=" + game.getFrameRate() +
            ", RES=" + Resource.getInstances() + 
            ", TRI=" + scene.getTrianglesRendered() + 
            ", COL=" + scene.getCollidableTriangles() + 
            ", TST=" + collider.getTested() +
            ", ESC=Quit" +
            ((visible) ? "" : ", E=Edit"),
            5 * s, h - 5 * s - ch, 5 * s,
            1, 1, 1, 1
        );
        renderer.endTriangles();
        if(!visible) {
            renderer.end();
            return;
        }
        ui.begin();
        if(ui.button("Editor.quit.button", 0, "Quit", false)) {
            mode = PLAYER;
            reset = false;
            down = false;
            selection = null;
            hide = true;
        }
        if(ui.button("Editor.save.button", 5, "Save", false)) {
            scene.getRoot().traverse((n) -> {
                n.resetBaseVertices();
                return true;
            });
            Scene.save(scene, sceneFile);
        }
        if(ui.button("Editor.map.button", 5, "Map", false)) {
            File lmFile = IO.file(sceneFile.getParentFile(), IO.fileNameWithOutExtension(sceneFile) + ".png");

            new LightMapper().light(lmFile, scene, true);
        }
        if(ui.button("Editor.add.light.button", 5, "+Light", false)) {
            Node node = new Node();

            node.setLight(true);
            node.getPosition().set(player.getPosition()).add(0, 50, 0);
            scene.getRoot().addChild(node);

            selection = node;
            reset = true;
        }
        if(ui.button("Editor.add.fire.light.button", 5, "+FireLight", false)) {
            Node node = new Node();
            ParticleSystem particles = game.getAssets().load(IO.file("assets/fire-light.par"));

            particles = (ParticleSystem)particles.newInstance();
            node.setRenderable(particles);
            node.setTexture(game.getAssets().load(IO.file("assets/maps/particle.png")));
            node.setVertexColorEnabled(true);
            node.setZOrder(100);
            node.setDepthState(DepthState.READONLY);
            node.setBlendState(BlendState.ADDITIVE);
            node.getPosition().set(player.getPosition()).add(0, 50, 0);
            scene.getRoot().addChild(node);

            selection = node;
            reset = true;
        }
        for(int i = 0; i != modes.length; i++) {
            if(ui.button("Editor.modes.button." + i, 5, modes[i], mode == i)) {
                mode = i;
            }
        }
        if(selection != null) {
            if(reset) {
                Vector3f p = selection.getPosition();

                p.x = (int)p.x;
                p.y = (int)p.y;
                p.z = (int)p.z;
            }
            ui.addRow(5);
            ui.textField("Editor.position.field", 0, "Position", selection.getPosition(), reset, 20);
            
            if(selection.isLight()) {
                ui.addRow(5);
                ui.textField("Editor.light.color.field", 0, "Color", selection.getLightColor(), reset, 20);

                ui.addRow(5);
                if((result = ui.textField("Editor.light.radius.field", 0, "Radius", selection.getLightRadius(), reset, 10)) != null) {
                    selection.setLightRadius((Float)result);
                }

                ui.addRow(5);
                if((result = ui.textField("Editor.light.sample.radius.field", 0, "Sample Radius", selection.getLightSampleRadius(), reset, 10)) != null) {
                    selection.setLightSampleRadius((Float)result);
                }

                ui.addRow(5);
                if((result = ui.textField("Editor.light.sample.count.field", 0, "Sample Count", selection.getLightSampleCount(), reset, 10)) != null) {
                    selection.setLightSampleCount((Integer)result);
                }
            }
            reset = false;
        }
        handled = ui.end();
        renderer.end();

        if(hide) {
            visible = false;
            scene.removeUI();
        } else if(!handled) {
            if(game.isButtonDown(0)) {
                if(mode == SEL || mode == DEL) {
                    if(!down) {
                        scene.getCamera().unProject(0, game, origin);
                        scene.getCamera().unProject(1, game, direction);
                        direction.sub(origin).normalize();
                        time[0] = Float.MAX_VALUE;
                        selection = null;
                        scene.getRoot().traverse((n) -> {
                            bounds.clear();
                            bounds.add(origin);
                            bounds.add(point.set(direction).mul(time[0]).add(origin));
                            if(n.getBounds().touches(bounds)) {
                                if(n.getRenderable() instanceof ParticleSystem) {
                                    if(n.getBounds().intersects(origin, direction, time)) {
                                        selection = n;
                                    }
                                }
                            }
                            if(n.isLight()) {
                                bounds.clear();
                                bounds.getMin().set(n.getAbsolutePosition()).sub(8, 8, 8);
                                bounds.getMax().set(n.getAbsolutePosition()).add(8, 8, 8);
                                if(bounds.intersects(origin, direction, time)) {
                                    selection = n;
                                }
                            }
                            return true;
                        });
                        if(selection != null) {
                            if(mode == DEL) {
                                selection.detachFromParent();
                                selection = null;
                            } else {
                                reset = true;
                            }
                        }
                    }
                } else if(selection != null) {
                    if(mode == MOVXZ) {
                        scene.getCamera().move(selection.getPosition(), game.getDX(), -game.getDY());
                    } else if(mode == MOVY) {
                        scene.getCamera().move(selection.getPosition(), -game.getDY());
                    }
                }
                down = true;
            } else {
                down = false;
            }
        }
    }
}
