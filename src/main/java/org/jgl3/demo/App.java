package org.jgl3.demo;

import java.util.Vector;

import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.Renderer;
import org.jgl3.scene.KeyFrameMeshLoader;
import org.jgl3.scene.NodeLoader;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.ui.UIManager;

public class App {

    public App(int width, int height, boolean resizable, Demo ... demos) throws Exception {
        Game game = null;

        try {
            game = new Game(width, height, resizable);

            KeyFrameMeshLoader.registerAssetLoader();
            ParticleSystem.registerAssetLoader();

            Vector<String> demoNames = new Vector<>();
            int selDemo = -1;
            Demo demo = null;

            for(Demo iDemo : demos) {
                demoNames.add(iDemo.toString());
            }

            game.resetTimer();

            while(game.run()) {
                if(demo != null) {
                    if(!demo.run()) {
                        game.getAssets().clear();
                        demo = null;
                        selDemo = -1;
                        game.disableFPSMouse();
                    }
                } else {
                    Renderer renderer = game.getRenderer();
                    UIManager ui = game.getUI();
                    Object result;
                    
                    GFX.clear(0.2f, 0.2f, 0.2f, 1);
                    renderer.begin();
                    ui.begin();
                    if(ui.button("App.full.screen.button", 0, "Full Screen", game.isFullscreen())) {
                        game.toggleFullscreen();
                    }
                    if(ui.button("App.sync.button", 5, "Sync", game.isSyncEnabled())) {
                        game.toggleSync();
                    }
                    ui.addRow(5);
                    if((result = ui.list("App.demo.list", 0, demoNames, 25, 8, selDemo)) != null) {
                        demo = demos[(Integer)result];
                        demo.init();
                    }
                    selDemo = -2;
                    ui.end();
                    renderer.end();
                }
                game.nextFrame();
            }
        } finally {
            if(game != null) {
                game.destroy();
            }
        }
    }

    public static void main(String[] args) throws Exception {
        NodeLoader.registerLoader(".obj", new NodeLoader.OBJLoader());
        
        new App(1000, 600, true,
            new Editor(),
            new Island()
        );
    }
}
