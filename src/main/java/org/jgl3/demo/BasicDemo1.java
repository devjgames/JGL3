package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.lwjgl.glfw.GLFW;

public class BasicDemo1 extends Demo {

    @Override
    public void init() throws Exception {
        Game.getInstance().getRenderer().getModel().identity();
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        Font font = game.getUI().getFont();

        GFX.clear(0.2f, 0.2f, 0.2f, 1);
        renderer.begin();
        renderer.setProjectionFieldOfView(60, game.getAspectRatio(), 0.1f, 10000);
        renderer.setViewLookAt(100, 100, 100, 0, 0, 0, 0, 1, 0);
        renderer.getModel().rotate((float)Math.toRadians(45 * game.getElapsedTime()), 0, 1, 0);
        renderer.setModel();
        renderer.setVertexColorEnabled(true);
        renderer.beginTriangles();
        renderer.push(-50, 0, -50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.push(-50, 0, +50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.push(+50, 0, +50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.push(+50, 0, +50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.push(+50, 0, -50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.push(-50, 0, -50, 0, 0, 0, 0, 0, 0, 0, 1, 0.5f, 0, 1);
        renderer.endTriangles();
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS = " + game.getFrameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" + 
            "ESC = Quit",
            5 * game.getScale(), 5 * game.getScale(), 5 * game.getScale(),
            1, 1, 1, 1
        );
        renderer.endTriangles();
        renderer.end();

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
