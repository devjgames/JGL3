package org.jgl3.demo;

import java.io.File;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.lwjgl.glfw.GLFW;

public class ScenePlayer extends Demo {

    private final File file;
    private Scene scene = null;
    private int tested = 0;
    private String info = "";

    public ScenePlayer(File file) {
        this.file = file;
    }

    public void setTested(int tested) {
        this.tested = tested;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public void init() throws Exception {
        tested = 0;
        info = "";

        scene = Scene.load(file, new LightMapper());
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        Font font = UIManager.getInstance().getFont();
        Renderer renderer = game.getRenderer();
        int s = game.getScale();
        int h = game.getHeight();

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
        renderer.push(info, 5 * s, h - 5 * s - font.getCharHeight() * font.getScale(), 0, 1, 1, 1, 1);
        renderer.endTriangles();
        renderer.end();

        scene.updateAnimators();

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }


    @Override
    public String toString() {
        String name = IO.fileNameWithOutExtension(file);

        name = Character.toUpperCase(name.charAt(0)) + name.substring(1);

        return name;
    }
}
