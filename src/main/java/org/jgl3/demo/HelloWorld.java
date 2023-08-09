package org.jgl3.demo;

import org.jgl3.AmbientLight;
import org.jgl3.Camera;
import org.jgl3.CullState;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Light;
import org.jgl3.LightPipeline;
import org.jgl3.MeshPTN;
import org.jgl3.Scene;
import org.jgl3.Size;
import org.jgl3.VertexUsage;
import org.jgl3.demo.App.Demo;

public class HelloWorld extends Demo {

    private class RotatingQuad extends MeshPTN {

        public RotatingQuad() throws Exception {
            super(Game.getInstance().getAssets().manage(new LightPipeline()));

            getState().setCullState(CullState.NONE);

            getPipeline().push(-50, 0, -50, 0, 0, 0, 1, 0);
            getPipeline().push(-50, 0, +50, 0, 2, 0, 1, 0);
            getPipeline().push(+50, 0, +50, 2, 2, 0, 1, 0);
            getPipeline().push(+50, 0, -50, 2, 0, 0, 1, 0);

            getPipeline().push(0, 1, 2, 3);

            getPipeline().bufferVertices(VertexUsage.STATIC, true);
            getPipeline().bufferIndices(VertexUsage.STATIC, true);

            setTexture(Game.getInstance().getAssets().load(IO.file("assets/textures/checker.png")));
        }

        @Override
        public void update(Scene scene) {
            getRotation().rotate((float)Math.toRadians(45 * Game.getInstance().getElapsedTime()), 0, 1, 0);
        }
    }

    private Scene scene = null;
    
    @Override
    public void init() throws Exception {
        scene = new Scene();
        scene.getRoot().addChild(new RotatingQuad());
        scene.getRoot().addChild(new AmbientLight());
        ((Light)scene.getRoot().lastChild()).getColor().set(0.5f, 0.5f, 0.5f, 1);
        scene.getRoot().addChild(new Camera());
        ((Camera)scene.getRoot().lastChild()).activate();
        scene.getRoot().lastChild().getPosition().set(100, 100, 100);
        ((Camera)scene.getRoot().lastChild()).look(-1, -1, -1, 0, 1, 0);
    }

    @Override
    public void nextFrame(Size size) throws Exception {
        Game game = Game.getInstance();
        int s = game.getScale();

        scene.render(size);
        game.getSpritePipeline().begin(size);
        game.getSpritePipeline().beginSprite(game.getFont());
        game.getSpritePipeline().push("Hello World!", 10 * s, 10 * s, 0, 0, 0, 0, 1);
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
    }
}
