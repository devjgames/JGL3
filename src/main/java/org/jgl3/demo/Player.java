package org.jgl3.demo;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Sound;
import org.jgl3.scene.Animator;
import org.jgl3.scene.Collider;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;

public class Player extends Animator {
    
    private final Collider collider = new Collider();
    private Sound painSound;
    private boolean dead;
    private final Vector3f start = new Vector3f();
    private final Vector3f startDirection = new Vector3f();

    public Player() throws Exception {
        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2) {
                kill();
            }
        });
    }

    @Override
    public void init(Scene scene, Node node) throws Exception {
        Demo demo = App.getDemo();

        if(demo instanceof Editor) {
            return;
        }

        Game game = Game.getInstance();

        node.clearCompiledMesh();
        node.clearMesh();
        node.getRotation().getColumn(0, startDirection);
        start.set(node.getPosition());

        scene.getCamera().getEye().set(start);
        scene.getCamera().getEye().add(startDirection, scene.getCamera().getTarget());
        scene.getCamera().getUp().set(0, 1, 0);

        start.set(node.getPosition());
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        dead = false;

        if(demo instanceof ScenePlayer) {
            ((ScenePlayer)demo).setInfo("Press left & right mouse buttons to move forwards & backwards");
        }

        game.enableFPSMouse();
    }

    @Override
    public void animate(Scene scene, Node node) throws Exception {
        Demo demo = App.getDemo();

        if(demo instanceof Editor) {
            return;
        }

        if(dead) {
            if(!painSound.isPlaying()) {
                scene.getCamera().getEye().set(start);
                scene.getCamera().getEye().add(startDirection, scene.getCamera().getTarget());
                scene.getCamera().getUp().set(0, 1, 0);
                dead = false;
            }
        } else {
            collider.move(scene);

            if(demo instanceof ScenePlayer) {
                ((ScenePlayer)demo).setTested(collider.getTested());
            }
        }
    }

    private void kill() {
        painSound.play(false);
        dead = true;
    }
}
