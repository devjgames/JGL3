package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Log;
import org.jgl3.PixelFormat;
import org.jgl3.RenderTarget;
import org.jgl3.Resource;
import org.jgl3.Size;
import org.joml.Intersectionf;
import org.lwjgl.glfw.GLFW;

public class App {

    public static abstract class Demo {

        public void renderTargets() throws Exception {
        }

        public abstract void init() throws Exception;
        public abstract void nextFrame(Size size) throws Exception;

        @Override
        public String toString() {
            return getClass().getSimpleName();
        }
    }

    public App(int width, int height, boolean resizable, Demo ... demos) throws Exception {
        Game game = null;

        try {
            game = new Game(width, height, resizable);

            Demo demo = demos[0];
            Font font = game.getFont();
            int s = game.getScale();
            int w = game.getWidth();
            int h = game.getHeight();
            boolean down = false;
            boolean sDown = false;
            boolean fDown = false;
            RenderTarget renderTarget = game.getResources().manage(new RenderTarget(w - 250 * s, h, PixelFormat.COLOR));
            int[] size = new int[2];
            int count = 0 ;

            demo.init();

            while(game.run()) {
                int x = game.getMouseX();
                int y = game.getMouseY();
                int i = 10 * s;

                w = game.getWidth();
                h = game.getHeight();

                if(w > 50 && h > 50 && (w - 250 * s != renderTarget.getWidth() || h !=  renderTarget.getHeight())) {
                    Log.put(1, "resizing render target ...");

                    game.getResources().unManage(renderTarget);
                    renderTarget = game.getResources().manage(new RenderTarget(w - 250 * s, h, PixelFormat.COLOR));
                }
                if(count > 0) {
                    count--;
                } else {
                    renderTarget.begin();
                    demo.nextFrame(renderTarget);
                    renderTarget.end();
                }

                demo.renderTargets();

                int sx = renderTarget.getWidth() + 10 * s;

                GFX.clear(0.3f, 0.3f, 0.3f, 1);

                game.getSpritePipeline().begin(game);

                game.getSpritePipeline().beginSprite(font);
                for(Demo iDemo : demos) {
                    String name = iDemo.toString();

                    if(iDemo == demo) {
                        game.getSpritePipeline().push(name, sx, i, 0, 1, 0.5f, 0, 1);
                    } else {
                        game.getSpritePipeline().push(name, sx, i, 0, 0.75f, 0.75f, 0.75f, 1);
                    }

                    font.measure(name, 5, size);

                    if(game.isButtonDown(0) && !down) {
                        
                        if(Intersectionf.testPointAar(x, y, sx, i, sx + size[0], i + size[1])) {

                            for(int j = 0; j != 10000; j++) {
                                System.out.println("\n");
                            }

                            Log.put(1, "loading '" + name + "' ...");

                            game.getAssets().clear();
                            demo = iDemo;
                            demo.init();
                            down = true;
                            game.resetTimer();

                            count = 30;
                        }
                    }
                    i += size[1] + 5 * s;
                }

                String info =
                    "FPS = " + game.getFrameRate() + 
                    "\nRES = " + Resource.getInstances() +
                    "\nS   = Sync" +
                    "\nF   = Fullscreen";

                font.measure(info, 5, size);

                game.getSpritePipeline().push(info, sx, h - 10 * s - size[1], 5, 0.75f, 0.75f, 0.75f, 1);
                game.getSpritePipeline().endSprite();

                game.getSpritePipeline().beginSprite(renderTarget.getTexture(0));
                game.getSpritePipeline().push(
                    0, 0, renderTarget.getWidth(), renderTarget.getHeight(), 
                    0, 0, renderTarget.getWidth(), renderTarget.getHeight(), 
                    1, 1, 1, 1, true
                    );
                game.getSpritePipeline().endSprite();

                game.getSpritePipeline().end();

                game.nextFrame();

                if(game.isButtonDown(0)) {
                    down = true;
                } else {
                    down = false;
                }
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

    public static void main(String[] args) throws Exception {
        new App(
            1000, 700, true,
            new HelloWorld(),
            new Plot(),
            new UI(),
            new Map(IO.file("assets/maps/map1.obj"), 0, 64, 0, 1, -1, 0.4f, 0.5f, 0.6f)
        );
    }
}
