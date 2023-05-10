package org.jgl3.demo;

import java.io.File;

import org.jgl3.DepthState;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.scene.Collider;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeLoader;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Renderable;
import org.jgl3.scene.Scene;
import org.lwjgl.glfw.GLFW;

public class MapDemo2 extends Demo {

    private static final File FILE = IO.file("assets/scenes/map2.scn");

    private Editor editor = null;
    private Scene scene = null;
    private Node player = null;
    private final Collider collider = new Collider();

    @Override
    public void init() throws Exception {
        Scene.create(FILE);

        editor = new Editor();
        scene = Scene.load(FILE);
        if(scene.getRoot().getChildCount() == 0) {
            Node node = NodeLoader.load(IO.file("assets/maps/map2.obj"));

            for(int i = 0; i != node.getChildCount(); i++) {
                Node child = node.getChild(i);

                if(child.getName().equals("rock1")) {
                    child.getAmbientColor().set(0.3f, 0.3f, 0.3f, 1);
                } else {
                    child.getAmbientColor().set(0.5f, 0.5f, 0.5f, 1);
                }
                child.setLightMapEnabled(true);
                child.setCollidable(true);
            }
            scene.getRoot().addChild(node);

            Editor.addPlayer(scene, collider)
                .getPosition().y = 200;

            node = new Node();
            node.setLight(true);
            node.setLightRadius(20000);
            node.getLightColor().set(1, 0.5f, 0, 1);
            node.getPosition().set(135, 432, 18);
            node.setLightSampleRadius(64);
            scene.getRoot().addChild(node);

            node = NodeLoader.load(IO.file("assets/sky.obj"));
            node.setDepthState(DepthState.NONE);
            node.setFollowEye(true);
            node.setZOrder(-1000);
            scene.getRoot().addChild(node);
        }
        player = scene.getRoot().find("player", false);
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();

        editor.run(scene, player, collider, FILE);

        scene.getRoot().traverse((n) -> {
            Renderable renderable = n.getRenderable();

            if(renderable instanceof ParticleSystem) {
                ParticleSystem particles = (ParticleSystem)renderable;
                float time = Game.getInstance().getTotalTime();

                particles.getPosition().y = (float)Math.sin(time * 2) * 25;
            }
            return true;
        });

        if(editor.isPlayerMode()) {
            collider.move(scene, player, 100, 3, 100, 0, null, true);
        }
        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
