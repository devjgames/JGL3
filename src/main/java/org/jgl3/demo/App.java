package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.NodeLoader;
import org.jgl3.Resource;
import org.jgl3.Scene;
import org.lwjgl.glfw.GLFW;

public class App {

    public static void main(String[] args) throws Exception {
        Game game = null;

        try {
            game = new Game(1000, 700, true);

            Scene scene = null;
            Font font = game.getFont();
            int s = game.getScale();
            boolean sDown = false;
            boolean fDown = false;

            scene = NodeLoader.load(IO.file("scenes/scene1.xml"), new Generator());

            while(game.run()) {

                scene.render(game);

                game.getSpritePipeline().begin(game);
                game.getSpritePipeline().beginSprite(font);
                String info =
                    "FPS = " + game.getFrameRate() + 
                    "\nRES = " + Resource.getInstances() +
                    "\nS   = Sync" +
                    "\nF   = Fullscreen";
                game.getSpritePipeline().push(info, 10 * s, 10 * s, 5, 1, 1, 1, 1);
                game.getSpritePipeline().endSprite();
                game.getSpritePipeline().end();

                game.nextFrame();

                if(game.isKeyDown(GLFW.GLFW_KEY_S)) {
                    if(!sDown) {
                        game.toggleSync();
                        sDown = true;
                    }
                } else {
                    sDown = false;
                }
                if(game.isKeyDown(GLFW.GLFW_KEY_F)) {
                    if(!fDown) {
                        game.toggleFullscreen();
                        fDown = true;
                    }
                } else {
                    fDown = false;
                }
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }
}
