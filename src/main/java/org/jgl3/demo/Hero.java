package org.jgl3.demo;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Sound;
import org.jgl3.scene.Animator;
import org.jgl3.scene.Collider;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;

public class Hero extends Animator {
    
    private KeyFrameMesh mesh = null;
    private final Collider collider = new Collider();
    private Sound jumpSound, painSound;
    private boolean dead;
    private final Vector3f start = new Vector3f();
    private final Vector3f startOffset = new Vector3f();
    private int jump;
    private int baseLength, height;

    public Hero() throws Exception {
        collider.setContactListener((tri) -> {
            if(tri.getTag() == 2) {
                kill();
            }
        });
    }

    @Override
    public void init(Scene scene, Node node) throws Exception {
        if(App.getDemo() instanceof Editor) {
            return;
        }

        Game game = Game.getInstance();

        jump = Integer.parseInt(getToken(1));
        baseLength = Integer.parseInt(getToken(2));
        height = Integer.parseInt(getToken(3));

        mesh = (KeyFrameMesh)node.getChild(0).getRenderable();
        start.set(node.getPosition());
        jumpSound = game.getAssets().load(IO.file("assets/sound/jump.wav"));
        jumpSound.setVolume(0.25f);
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        Vector3f offset = scene.getCamera().getOffset();

        offset.mul(1, 0, 1).normalize(baseLength);
        offset.y = height;
        startOffset.set(offset);
        scene.getCamera().getTarget().set(start);
        scene.getCamera().getTarget().add(offset, scene.getCamera().getEye());
        scene.getCamera().getUp().set(0, 1, 0);

        dead = false;
    }

    @Override
    public void animate(Scene scene, Node node) throws Exception {
        if(App.getDemo() instanceof Editor) {
            return;
        }

        if(dead) {
            if(!painSound.isPlaying()) {
                node.getPosition().set(start);
                scene.getCamera().getTarget().set(start).add(startOffset, scene.getCamera().getEye());
                scene.getCamera().getUp().set(0, 1, 0);
                dead = false;
            }
        } else {
            collider.move(scene, node, 100, jump, jumpSound, baseLength, height);

            Demo demo = App.getDemo();

            if(demo instanceof Player) {
                ((Player)demo).setTested(collider.getTested());
            }
        }
    }

    private void kill() {
        mesh.setSequence(66, 69, 7, false);
        mesh.reset();
        painSound.play(false);
        dead = true;
    }
}
