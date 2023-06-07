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
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class Player extends Demo {

    private static final float OFFSET_LENGTH = 100;
    private static final float HEIGHT = 10;

    private final File file;
    private Scene scene = null;
    private final Collider collider = new Collider();
    private Node playerNode;
    private KeyFrameMesh playerMesh;
    private KeyFrameMesh weaponMesh;
    private final Vector3f startPosition = new Vector3f();
    private final Vector3f startOffset = new Vector3f();
    private int jump = 0;
    private Sound jumpSound = null;
    private Sound painSound = null;
    private float seconds = -2;
    private boolean dead = false;
    private boolean freeRotate = false;

    public Player(File file) {
        this.file = file;
    }

    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();
        Node weaponNode = new Node();
        String[] tokens;

        scene = Scene.load(file);
        playerNode = scene.getRoot().find("hero", true);
        playerMesh = (KeyFrameMesh)playerNode.getChild(0).getRenderable();
        weaponMesh = game.getAssets().load(IO.file(playerMesh.getFile().getParentFile(), "hero-weapon.md2"));
        weaponMesh = (KeyFrameMesh)weaponMesh.newInstance();
        weaponNode.setRenderable(weaponMesh);
        weaponNode.getAmbientColor().set(0.2f, 0.2f, 0.6f, 1);
        weaponNode.setLightingEnabled(true);
        weaponNode.setTexture(game.getAssets().load(IO.file(weaponMesh.getFile().getParentFile(), "hero-weapon.png")));
        weaponNode.getRotation().set(playerNode.getChild(0).getRotation());
        weaponNode.getPosition().set(playerNode.getChild(0).getPosition());
        weaponMesh.setSequence(0, 39, 10, true);
        playerNode.addChild(weaponNode);
        startPosition.set(playerNode.getPosition());
        startOffset.set(scene.getCamera().getOffset()).normalize(OFFSET_LENGTH);
        scene.getCamera().getTarget().set(startPosition);
        scene.getCamera().getTarget().add(startOffset, scene.getCamera().getEye());
        tokens = playerNode.getTag().split("\\s+");
        jump = Integer.parseInt(tokens[0]);
        freeRotate = Boolean.parseBoolean(tokens[1]);
        jumpSound = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jumpSound.setVolume(0.25f);
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);
        seconds = -2;
        dead = false;

        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2) {
                painSound.play(false);
                seconds = 2;
                dead = true;
                playerMesh.setSequence(66, 67, 7, false);
                weaponMesh.setSequence(66, 67, 7, false);
            }
        });
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        Font font = game.getUI().getFont();
        int s = game.getScale();
        Renderer renderer = game.getRenderer();

        scene.render(game.getAspectRatio());
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS = " + game.getFrameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + scene.getTrianglesRendered() + "\n" +
            "COL = " + scene.getCollidableTriangles() + "\n" +
            "TST = " + collider.getTested() + "\n" +
            "ESC = Quit", 10 * s, 10 * s, 5 * s, 1, 1, 1, 1
            );
        renderer.endTriangles();
        renderer.end();

        scene.getRoot().traverse((n) -> {
            n.warp();
            if(n.getName().equals("fire-light")) {
                ParticleSystem particles = (ParticleSystem)n.getRenderable();
    
                particles.getPosition().y = (float)Math.sin(Game.getInstance().getTotalTime() * 2) * 25;
            }
            return true;
        });

        if(dead) {
            seconds -= game.getElapsedTime();
            if(seconds < 0) {
                playerNode.getPosition().set(startPosition);
                scene.getCamera().getTarget().set(startPosition);
                scene.getCamera().getTarget().add(startOffset, scene.getCamera().getEye());
                scene.getCamera().getUp().set(0, 1, 0);
                seconds = -2;
                dead = false;
            }
            return true;
        }

        collider.move(scene, playerNode, OFFSET_LENGTH, HEIGHT, 3, 100, jump, jumpSound, freeRotate);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
