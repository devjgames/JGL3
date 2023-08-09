package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.AmbientLight;
import org.jgl3.Camera;
import org.jgl3.DualTexturePipeline;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Light;
import org.jgl3.LightMapper;
import org.jgl3.LightPipeline;
import org.jgl3.Mesh;
import org.jgl3.Node;
import org.jgl3.NodeLoader;
import org.jgl3.OctTree;
import org.jgl3.PointLight;
import org.jgl3.Scene;
import org.jgl3.Size;
import org.jgl3.Triangle;
import org.jgl3.demo.App.Demo;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Map extends Demo {
    
    private final File file;
    private final Vector3f position = new Vector3f();
    private final Vector3f direction = new Vector3f();
    private final Vector4f ambientColor = new Vector4f();
    private Scene scene = null;
    private Camera camera = null;

    public Map(File file, float x, float y, float z, float dx, float dz, float r, float g, float b) {

        this.file = file;

        position.set(x, y, z);
        direction.set(dx, 0, dz);
        ambientColor.set(r, g, b, 1);
    }

    @Override
    public void init() throws Exception {
        scene = new Scene();

        scene.getBackgroundColor().set(0, 0, 0, 1);

        Light ambient = new AmbientLight();

        ambient.getColor().set(ambientColor);
        scene.getRoot().addChild(ambient);

        Node map = null;
        File mapFile = IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".txt");

        if(mapFile.exists()) {

            File lmFile = IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".png");
            LightMapper lightMapper = new LightMapper();
            String[] lines = new String(IO.readAllBytes(mapFile)).split("\\n+");
            boolean rebuild = false;

            for(String line : lines) {
                String tLine = line.trim();
                String[] tokens = tLine.split("\\s+");

                if(tLine.startsWith("w ")) {
                    lightMapper.setLightMapWidth(Integer.parseInt(tokens[1]));

                } else if(tLine.startsWith("h ")) {
                    lightMapper.setLightMapHeight(Integer.parseInt(tokens[1]));

                } else if(tLine.startsWith("aos ")) {
                    lightMapper.setAOStrength(Float.parseFloat(tokens[1]));

                } else if(tLine.startsWith("aol ")) {
                    lightMapper.setAOLength(Float.parseFloat(tokens[1]));

                } else if(tLine.startsWith("sr ")) {
                    lightMapper.setSampleRadius(Float.parseFloat(tokens[1]));

                } else if(tLine.startsWith("sc ")) {
                    lightMapper.setSampleCount(Integer.parseInt(tokens[1]));

                } else if(tLine.startsWith("rebuild ")) {
                    rebuild = Boolean.parseBoolean(tokens[1]);
                
                } else if(tLine.startsWith("light ")) {
                    PointLight light = new PointLight();

                    light.getPosition().set(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]));
                    light.getColor().set(Float.parseFloat(tokens[4]), Float.parseFloat(tokens[5]), Float.parseFloat(tokens[6]), 1);
                    light.setRange(Float.parseFloat(tokens[7]));

                    scene.getRoot().addChild(light);
                }
            }

            map = NodeLoader.load(file, 0.025f, () -> new DualTexturePipeline());
            scene.getRoot().addChild(map);

            lightMapper.map(scene, lmFile, rebuild);
        } else {
            map = NodeLoader.load(file, 0.025f, () -> new LightPipeline());

            scene.getRoot().addChild(map);
        }
        
        final Vector<Triangle> triangles = new Vector<>();

        triangles.clear();

        map.traverse(scene, (s, n) -> {
            if(n instanceof Mesh) {
                Mesh mesh = (Mesh)n;

                for(int i = 0; i != mesh.getMeshPipeline().getIndexCount(); ) {
                    int i1 = mesh.getMeshPipeline().getIndex(i++);
                    int i2 = mesh.getMeshPipeline().getIndex(i++);
                    int i3 = mesh.getMeshPipeline().getIndex(i++);
                    Triangle triangle = new Triangle();

                    triangle.getP1().set(
                        mesh.getMeshPipeline().getVertexComponent(i1, 0),
                        mesh.getMeshPipeline().getVertexComponent(i1, 1),
                        mesh.getMeshPipeline().getVertexComponent(i1, 2)
                    );
                    triangle.getP2().set(
                        mesh.getMeshPipeline().getVertexComponent(i2, 0),
                        mesh.getMeshPipeline().getVertexComponent(i2, 1),
                        mesh.getMeshPipeline().getVertexComponent(i2, 2)
                    );
                    triangle.getP3().set(
                        mesh.getMeshPipeline().getVertexComponent(i3, 0),
                        mesh.getMeshPipeline().getVertexComponent(i3, 1),
                        mesh.getMeshPipeline().getVertexComponent(i3, 2)
                    );
                    triangle.calcPlane();

                    triangles.add(triangle);
                }
            }
            return true;
        });

        camera = new FPSCamera(OctTree.create(triangles, 16));

        camera.getPosition().set(position);
        camera.look(direction.x, direction.y, direction.z, 0, 1, 0);

        scene.getRoot().addChild(camera);
        camera.activate();

        Game.getInstance().enableFPSMouse();
    }

    @Override
    public void nextFrame(Size size) throws Exception {
        Game game = Game.getInstance();
        Vector3f p = camera.getPosition();
        int h = game.getHeight();
        int s = game.getScale();
        int c = game.getFont().getCharHeight();

        scene.render(size);
        game.getSpritePipeline().begin(size);
        game.getSpritePipeline().beginSprite(game.getFont());
        game.getSpritePipeline().push("Press ESC key to release mouse", 10 * s, 10 * s, 0, 1, 1, 1, 1);
        game.getSpritePipeline().push("Location = " + (int)p.x + " " + (int)p.y + " " + (int)p.z, 10 * s, h -  c * s - 10 * s, 0, 1, 1, 1, 1);
        game.getSpritePipeline().endSprite();
        game.getSpritePipeline().end();
    }

    @Override
    public String toString() {
        return IO.fileNameWithOutExtension(file);
    }
}
