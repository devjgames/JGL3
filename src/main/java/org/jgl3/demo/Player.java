package org.jgl3.demo;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Sound;
import org.jgl3.scene.ArgumentReader;
import org.jgl3.scene.ArgumentWriter;
import org.jgl3.scene.Collider;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeBuilder;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;

public class Player extends Node {
    
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
    public Node init(Scene scene) throws Exception {
        Demo demo = App.getDemo();

        if(demo instanceof Editor) {
            addChild(NodeBuilder.load(IO.file("assets/ui/ui.obj")).build());
            getChild(0).setTextureLinear(true);
            return this;
        }

        Game game = Game.getInstance();

        getRotation().getColumn(0, startDirection);
        start.set(getPosition());

        scene.getCamera().getEye().set(start);
        scene.getCamera().getEye().add(startDirection, scene.getCamera().getTarget());
        scene.getCamera().getUp().set(0, 1, 0);

        start.set(getPosition());
        painSound = game.getAssets().load(IO.file("assets/sound/pain.wav"));
        painSound.setVolume(0.25f);

        dead = false;

        if(demo instanceof ScenePlayer) {
            ((ScenePlayer)demo).setInfo("Press left & right mouse buttons to move forwards & backwards");
        }

        game.enableFPSMouse();

        return this;
    }

    @Override
    public Node update(Scene scene) throws Exception {
        Demo demo = App.getDemo();

        if(demo instanceof Editor) {
            return this;
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
        return this;
    }

    @Override
    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {

        writer.write(getPosition());
        writer.write(getRotation());

        return this;
    }

    @Override
    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {

        reader.readVector3f(getPosition());
        reader.readRotation(getRotation());

        return this;
    }

    private void kill() {
        painSound.play(false);
        dead = true;
    }
}
