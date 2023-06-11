package org.jgl3.demo;

import java.io.File;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.Sound;
import org.jgl3.scene.Collider;
import org.jgl3.scene.Node;
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
        Node node = new Node();

        scene = Scene.load(file);
        playerNode = scene.getRoot().find("hero", true);
        playerNode.detachFromParent();
        playerNode.getRotation().identity();
        node.addChild(playerNode);
        node.getPosition().set(playerNode.getPosition());
        playerNode.getPosition().zero();
        playerNode = node;
        scene.getRoot().addChild(playerNode);
        start.set(playerNode.getPosition());
        jumpSound = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jumpSound.setVolume(0.25f);
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        Vector3f offset = scene.getCamera().getOffset();

        offset.x = 0;
        scene.getCamera().getTarget().add(offset, scene.getCamera().getEye());
        scene.getCamera().getUp().set(0, 1, 0);

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

        if(dead) {
            if(!painSound.isPlaying()) {
                playerNode.getPosition().set(start);
                playerNode.getChild(0).getRotation().identity();
                dead = false;
            }
        } else {
            if(collider.move(scene, playerNode, 100, jump, jumpSound, xMoveOnly)) {
                playerNode.getChild(0).getRotation().rotate((float)Math.toRadians(-360) * game.getElapsedTime(), 0, 0, 1);
            }

            if(playerNode.getPosition().y < minY) {
                kill();
            }
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    private void kill() {
        painSound.play(false);
        dead = true;
    }

    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
