package org.jgl3.demo;

import java.io.File;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.Scene;
import org.lwjgl.glfw.GLFW;

public class Player extends Demo {

    private final File file;
    private Scene scene = null;
    private int tested = 0;

    public Player(File file) {
        this.file = file;
    }

    public void setTested(int tested) {
        this.tested = tested;
    }

    @Override
    public void init() throws Exception {
        scene = Scene.load(file);

        tested = 0;
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
            "TST = " + tested + "\n" +
            "ESC = Quit",
            5 * s, 5 * s, 5 * s, 1, 1, 1, 1
            );
        renderer.endTriangles();
        renderer.end();

        scene.updateAnimators();

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }


    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
