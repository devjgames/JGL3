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
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Collide extends Demo {

    private final File file;
    private Scene scene = null;
    private Node playerNode = null;
    private KeyFrameMesh mesh = null;
    private final Collider collider = new Collider();
    private Sound jumpSound, painSound;
    private boolean dead;
    private final Vector3f start = new Vector3f();
    private final Vector3f startOffset = new Vector3f();
    private final int offsetLength = 150;
    private final int jump;
    private final LightMapper lightMapper;

    public Collide(File file, int jump, LightMapper lightMapper) {
        this.file = file;
        this.jump = jump;
        this.lightMapper = lightMapper;
        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2) {
                kill();
            }
        });
    }

    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();

        scene = Scene.load(file, lightMapper);
        playerNode = scene.getRoot().find("hero", true);
        mesh = (KeyFrameMesh)playerNode.getChild(0).getRenderable();
        start.set(playerNode.getPosition());
        jumpSound = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jumpSound.setVolume(0.25f);
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        Vector3f offset = scene.getCamera().getOffset();

        offset.normalize(offsetLength);
        startOffset.set(offset);
        scene.getCamera().getTarget().set(start);
        scene.getCamera().getTarget().add(offset, scene.getCamera().getEye());

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
                scene.getCamera().getTarget().set(start).add(startOffset, scene.getCamera().getEye());
                scene.getCamera().getUp().set(0, 1, 0);
                dead = false;
            }
        } else {
            collider.move(scene, playerNode, offsetLength, 3, 100, jump, jumpSound, true);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    private void kill() {
        mesh.setSequence(66, 69, 7, false);
        mesh.reset();
        painSound.play(false);
        dead = true;
    }

    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
