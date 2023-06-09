package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.Scene;
import org.lwjgl.glfw.GLFW;

public class Island extends Demo {

    private Scene scene = null;
    
    @Override
    public void init() throws Exception {

        scene = Scene.load(IO.file("assets/scenes/island.scn"));
        scene.getRoot().traverse((n) -> {
            if(n.getName().startsWith("water")) {
                n.getLastChild().allocWarp();
                n.getLastChild().setWarpSpeed(4);
                n.getLastChild().setWarpAmplitudeX(4);
                n.getLastChild().setWarpAmplitudeZ(4);
                n.getLastChild().setWarpAmplitudeY(4);
            }
            return true;
        });
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
            "ESC = Quit", 
            5 * s, 5 * s, 5 * s, 1, 1, 1, 1
            );
        renderer.endTriangles();
        renderer.end();

        scene.getRoot().traverse((n) -> {
            n.warp();
            return true;
        });

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
}
