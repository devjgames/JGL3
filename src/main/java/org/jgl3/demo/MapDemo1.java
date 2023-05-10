package org.jgl3.demo;

import java.io.File;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Log;
import org.jgl3.Sound;
import org.jgl3.scene.Collider;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeLoader;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class MapDemo1 extends Demo {

    private final File FILE = IO.file("assets/scenes/map1.scn");
    private final float OFFSET_LENGTH = 100;

    private Editor editor = null;
    private Scene scene = null;
    private Node player = null;
    private final Collider collider = new Collider();
    private Sound jump = null;
    private Sound pain = null;
    private boolean dead = false;
    private float seconds = 0;
    private final Vector3f startPosition = new Vector3f();
    private final Vector3f startOffset = new Vector3f();
    private final Vector3f startUp = new Vector3f();

    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();

        Scene.create(FILE);
        scene = Scene.load(FILE);
        if(scene.getRoot().getChildCount() == 0) {
            Log.put(1, "Creating scene - " + FILE + " ...");
            
            Node node = NodeLoader.load(IO.file("assets/maps/map1.obj"));

            node.traverse((n) -> {
                n.setLightMapEnabled(true);
                n.setCollidable(true);
                if(n.getName().equals("lava")) {
                    n.getAmbientColor().set(1, 1, 1, 1);
                    n.getDiffuseColor().set(0, 0, 0, 1);
                    n.setTriangleTag(2);
                } else {
                    n.getAmbientColor().set(0.2f, 0.2f, 0.2f, 1);
                }
                return true;
            });
            scene.getRoot().addChild(node);

            Editor.addPlayer(scene, collider).getPosition().y = 50;
        }
        editor = new Editor();
        player = scene.getRoot().find("player", false);

        scene.getRoot().find("lava", true).calcBaseVertices();

        jump = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jump.setVolume(0.25f);

        pain = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        pain.setVolume(0.25f);

        dead = false;
        seconds = 0;

        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2 && !editor.isVisible()) {
                pain.play(false);
                dead = true;
                seconds = 2;
                ((KeyFrameMesh)player.getChild(0).getRenderable()).setSequence(66, 67, 7, false);
            }
        });
        startPosition.set(player.getPosition());
        startOffset.set(scene.getCamera().getOffset()).normalize(OFFSET_LENGTH);
        startUp.set(scene.getCamera().getUp());
        scene.getCamera().getTarget().set(startPosition);
        scene.getCamera().getTarget().add(startOffset, scene.getCamera().getEye());
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        
        editor.run(scene, player, collider, FILE);

        scene.getRoot().traverse((n) -> {
            if(!editor.isVisible()) {
                n.warp();
            }
            if(n.getRenderable() instanceof ParticleSystem) {
                ParticleSystem particles = (ParticleSystem)n.getRenderable();

                particles.getPosition().y = (float)Math.sin(Game.getInstance().getTotalTime() * 2) * 25;
            }
            return true;
        });

        if(dead) {
            seconds -= game.getElapsedTime();
            if(seconds <= 0) {
                dead = false;
                player.getPosition().set(startPosition);
                scene.getCamera().getTarget().set(startPosition);
                scene.getCamera().getUp().set(startUp);
                scene.getCamera().getTarget().add(startOffset, scene.getCamera().getEye());
            }
        } else if(editor.isPlayerMode()) {
            collider.move(scene, player, OFFSET_LENGTH, 3, 100, 1000, jump, true);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
