package org.jgl3.demo;

import org.jgl3.AmbientLight;
import org.jgl3.Camera;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Light;
import org.jgl3.LightPipeline;
import org.jgl3.Mesh;
import org.jgl3.PointLight;
import org.jgl3.Scene;
import org.jgl3.Size;
import org.jgl3.VertexUsage;
import org.jgl3.demo.App.Demo;

public class Plot extends Demo {

    private class RotatingPointLight extends PointLight {

        private final float angularVelocity;
        private final float radius;
        private float angle;

        public RotatingPointLight(float angularVelocity, float startAngle, float radius, float range, float r, float g, float b) {

            this.angularVelocity = (float)Math.toRadians(angularVelocity);
            this.radius = radius;
            setRange(range);
            getColor().set(r, g, b, 1);

            this.angle = startAngle = (float)Math.toRadians(startAngle);
            getPosition().set((float)Math.cos(startAngle) * radius, 20, (float)Math.sin(startAngle) * radius);
        }

        @Override
        public void update(Scene scene) throws Exception {

            angle += angularVelocity * Game.getInstance().getElapsedTime();

            getPosition().set((float)Math.cos(angle) * radius, getPosition().y, (float)Math.sin(angle) * radius);
        }
    }

    private class Graph extends Mesh {

        private static final int H = 50;
        private static final int D = 64;

        private float h = H;
        private float d = -1;

        public Graph() throws Exception {
            super(Game.getInstance().getAssets().manage(new LightPipeline()));

            for(int i = 0; i != D - 1; i++) {
                for(int j = 0; j != D - 1; j++) {
                    getPipeline().pushFace(
                        i * D + j, i * D + j + 1, (i + 1) * D + j + 1, (i + 1) * D + j
                    );
                }
            }
            getPipeline().bufferIndices(VertexUsage.STATIC, true);

            plot();
            
            setTexture(Game.getInstance().getAssets().load(IO.file("assets/textures/checker.png")));
        }

        @Override
        public void update(Scene scene) throws Exception {

            h += d * 100 * Game.getInstance().getElapsedTime();

            if(h < -(H - 1)) {
                d = 1;
            } else if(h > H - 1) {
                d = -1;
            }

            plot();
        }

        private void plot() {
            getPipeline().clearVertices();
            for(int i = 0; i != D; i++) {
                for(int j = 0; j != D; j++) {
                    float x = -50 + i / (D - 1.0f) * 100;
                    float z = -50 + j / (D - 1.0f) * 100;
                    float y = h / (1 + (x * 0.1f) * (x * 0.1f) + (z * 0.1f) * (z * 0.1f));

                    getPipeline().pushVertex(x, y, z, i / (D - 1.0f) * 4, j / (D - 1.0f) * 4, 0, 0, 0);
                }
            }
            getPipeline().calcNormals();
            getPipeline().bufferVertices(VertexUsage.DYNAMIC, true);
        }
    }

    private Scene scene = null;

    @Override
    public void init() throws Exception {
        scene = new Scene();
        scene.getRoot().addChild(new Graph());
        scene.getRoot().addChild(new AmbientLight());
        ((Light)scene.getRoot().lastChild()).getColor().set(0.5f, 0.5f, 0.5f, 1);
        scene.getRoot().addChild(new RotatingPointLight(+180, 000, 40, 200, 2, 1, 0));
        scene.getRoot().addChild(new RotatingPointLight(-180, 180, 40, 200, 0, 1, 2));
        scene.getRoot().addChild(new Camera());
        scene.getRoot().lastChild().getPosition().set(100, 100, 100);
        ((Camera)scene.getRoot().lastChild()).look(-1, -1, -1, 0, 1, 0);
        ((Camera)scene.getRoot().lastChild()).activate();
    }

    @Override 
    public void nextFrame(Size size) throws Exception {
        scene.render(size);
    }
}
