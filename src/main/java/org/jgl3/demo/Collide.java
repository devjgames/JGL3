package org.jgl3.demo;

import java.io.File;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.Sound;
import org.jgl3.scene.Collider;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.Renderable;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Collide extends Demo {

    private final File file;
    private final int jump;
    private final float minY;
    private Scene scene = null;
    private Node playerNode = null;
    private final Collider collider = new Collider();
    private Sound jumpSound, painSound;
    private boolean dead;
    private final Vector3f start = new Vector3f();
    private final boolean xMoveOnly;

    public Collide(File file, int jump, float minY, boolean xMoveOnly) {
        this.file = file;
        this.jump = jump;
        this.minY = minY;
        this.xMoveOnly = xMoveOnly;
        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2) {
                kill();
            }
        });
    }

    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();

        scene = Scene.load(file);
        playerNode = scene.getRoot().find("hero", true);
        start.set(playerNode.getPosition());
        jumpSound = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jumpSound.setVolume(0.25f);
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        dead = false;
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
                Font font = game.getUI().getFont();
        Renderer renderer = game.getRenderer();
        int s = game.getScale();

        scene.render(game.getAspectRatio());
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS = " + game.getFrameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "NOD = " + scene.getNodesRenderer() + "\n" +
            "TRI = " + scene.getTrianglesRendered() + "\n" +
            "COL = " + scene.getCollidableTriangles() + "\n" +
            "TST = " + collider.getTested() + "\n" +
            "ESC = Quit", 
            5 * s, 5 * s, 5 * s, 1, 1, 1, 1
            );
        renderer.endTriangles();
        renderer.end();

        scene.getRoot().traverse((n) -> { 
            Editor.fireLight(n);
            return true;
        });

        if(dead) {
            if(!painSound.isPlaying()) {
                Vector3f offset = scene.getCamera().getOffset();

                scene.getCamera().getTarget().set(
                    playerNode.getPosition().set(start)).add(offset, scene.getCamera().getEye()
                    );
                dead = false;
            }
        } else {
            collider.move(scene, playerNode, 3, 100, jump, jumpSound, xMoveOnly);

            if(playerNode.getPosition().y < minY) {
                kill();
            }
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    private void kill() {
        if(playerNode.getChildCount() != 0) {
            Renderable renderable = playerNode.getChild(0).getRenderable();

            if(renderable instanceof KeyFrameMesh) {
                KeyFrameMesh mesh = (KeyFrameMesh)renderable;

                mesh.setSequence(66, 71, 7, false);
                mesh.reset();
            }
        }
        painSound.play(false);
        dead = true;
    }

    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
