package org.jgl3.demo;

import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.DepthState;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.demo.Editor.Tools;
import org.jgl3.scene.Animator;
import org.jgl3.scene.KeyFrameMeshLoader;
import org.jgl3.scene.Mesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Renderable;
import org.jgl3.scene.Node.Visitor;
import org.jgl3.ui.UIManager;

public class App {

    private static Demo demo = null;

    public static Demo getDemo() {
        return demo;
    }

    public static Tools createTools() {
        return (ui, sceneFile, scene, selection, reset) -> {
            Game game = Game.getInstance();
            final Vector<Node> detach = new Vector<>();
            final BoundingBox bounds = new BoundingBox();
            Visitor clearTorches = (n) -> {
                Renderable renderable = n.getRenderable();

                if(renderable instanceof ParticleSystem) {
                    ParticleSystem particles = (ParticleSystem)renderable;

                    if(particles.getEmitter() instanceof Fire) {
                        detach.add(n);
                    }
                }
                return true;
            };

            detach.removeAllElements();

            if(selection != null) {
                if(ui.button("Tools.place.torches.button", 0, "Place Torches", false)) {

                    scene.getRoot().traverse(clearTorches); 

                    selection.traverse((n) -> {
                        if(n.hasMesh() && n.getTexture().getFile().equals(IO.file("assets/meshes/torch.png"))) {
                            for(int i = 0; i != n.getFaceCount(); i++) {
                                float y = n.getVertexComponent(n.getFaceVertex(i, 0), 8);

                                if(y > 0.9f) {
                                    bounds.clear();
                                    for(int j = 0; j != n.getFaceVertexCount(i); j++) {
                                        bounds.add(
                                            n.getVertexComponent(n.getFaceVertex(i, j), 0),
                                            n.getVertexComponent(n.getFaceVertex(i, j), 1),
                                            n.getVertexComponent(n.getFaceVertex(i, j), 2)
                                        );
                                    }

                                    Node node = new Node();

                                    node.setZOrder(10);
                                    node.setDepthState(DepthState.READONLY);
                                    node.setBlendState(BlendState.ADDITIVE);
                                    node.setVertexColorEnabled(true);
                                    node.setTexture(game.getAssets().load(IO.file("assets/particles/fire.png")));
                                    node.setRenderable(game.getAssets().load(IO.file("assets/particles/fire.par")));
                                    node.setRenderable(node.getRenderable().newInstance());
                                    bounds.getCenter(node.getPosition());

                                    scene.getRoot().addChild(scene, node);
                                }
                            }
                        }
                        return true;
                    });
                }
                ui.addRow(5);
                if(ui.button("Tools.clear.torches.button", 0, "Clear Torches", false)) {
                    scene.getRoot().traverse(clearTorches); 
                }
            }

            for(Node n : detach) {
                n.detachFromParent(scene);
            }
            detach.removeAllElements();

            return selection;
        };
    }

    public App(int width, int height, boolean resizable, Demo ... demos) throws Exception {
        Game game = null;

        try {
            game = new Game(width, height, resizable);
            
            registerAssetLoaders();

            Vector<String> demoNames = new Vector<>();
            int selDemo = -1;
            
            demo = null;
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

    protected void registerAssetLoaders() {
        Node.registerAssetLoader();
        KeyFrameMeshLoader.registerAssetLoader();
        ParticleSystem.registerAssetLoader();
        Animator.registerAssetLoader();
        Mesh.registerAssetLoader();
    }

    public static void main(String[] args) throws Exception {
        new App(1200, 700, true,
            new Editor(createTools()),
            new Player(IO.file("assets/scenes/scene1.scn"))
        );
    }
}
