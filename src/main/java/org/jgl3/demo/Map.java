package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.DepthState;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Texture;
import org.jgl3.scene.ArgumentReader;
import org.jgl3.scene.ArgumentWriter;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeBuilder;
import org.jgl3.scene.ParticleSystem;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;

public class Map extends Node {
    
    private static final Vector<String> mapNames = new Vector<>();

    static {
        File[] files = IO.file("assets/maps").listFiles();

        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".obj")) {
                    mapNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
    }

    private int selMap = -1;
    private int mapIndex = -1;

    @Override
    public Node init(Scene scene) throws Exception {
        if(scene.getInDesign()) {
            selMap = -1;
        }
        return this;
    }

    @Override
    public boolean handleUI(Scene scene, boolean reset) throws Exception {
        UIManager ui = UIManager.getInstance();
        Object r;

        if(reset) {
            selMap = mapIndex;
        }
        ui.textField("Map.bg.field", 0, "BG", scene.getBackgroundColor(), reset, 20);
        ui.addRow(5);
        if((r = ui.list("Map.list", 0, mapNames, 20, 8, selMap)) != null) {
            loadMap(IO.file(IO.file("assets/maps"), mapNames.get((Integer)r) + ".obj"));
        }
        ui.addRow(5);
        if((r = ui.textField("Map.aos.field", 0, "AO Strength", scene.getAOStrength(), reset, 6)) != null) {
            scene.setAOStrength((Float)r);
        }
        ui.addRow(5);
        if((r = ui.textField("Map.aol.field", 0, "AO Length", scene.getAOLength(), reset, 6)) != null) {
            scene.setAOLength((Float)r);
        }
        ui.addRow(5);
        if((r = ui.textField("Map.sr.field", 0, "Sample Radius", scene.getSampleRadius(), reset, 6)) != null) {
            scene.setSampleRadius((Float)r);
        }
        ui.addRow(5);
        if((r = ui.textField("Map.sc.field", 0, "Sample Count", scene.getSampleCount(), reset, 6)) != null) {
            scene.setSampleCount((Integer)r);
        }
        ui.addRow(5);
        if((r = ui.textField("Map.w.field", 0, "Width", scene.getLightMapWidth(), reset, 6)) != null) {
            scene.setLightMapWidth((Integer)r);
        }
        ui.addRow(5);
        if((r = ui.textField("Map.h.field", 0, "Height", scene.getLightMapHeight(), reset, 6)) != null) {
            scene.setLightMapHeight((Integer)r);
        }
        ui.addRow(5);
        if(ui.button("Map.tex.enabled.button", 0, "Tex Enabled", scene.isTextureEnabled())) {
            scene.setTextureEnabled(!scene.isTextureEnabled());
        }
        if(ui.button("Map.tex.linear.button", 5, "Tex Linear", scene.getRoot().isTexture2Linear())) {
            boolean linear = !scene.getRoot().isTexture2Linear();

            scene.getRoot().traverse((n) -> {
                n.setTexture2Linear(linear);
                return true;
            });
        }
        ui.addRow(5);
        if(ui.button("Map.map.button", 0, "Map", false)) {
            File file = ((Editor)App.getDemo()).getSceneFile();

            new LightMapper().map(scene, IO.file(file.getParentFile(), IO.fileNameWithOutExtension(file) + ".png"), true);
        }
        if(ui.button("Map.clear.button", 5, "Clear Map", false)) {
            scene.getRoot().traverse((n) -> {
                n.setTexture2(null);
                return true;
            });
        }
        selMap = -2;
        return true;
    }

    @Override
    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {

        writer.write((mapIndex == -1) ? null : IO.file(IO.file("assets/maps"), mapNames.get(mapIndex) + ".obj"));
        writer.write(scene.getLightMapWidth());
        writer.write(scene.getLightMapHeight());
        writer.write(scene.getAOStrength());
        writer.write(scene.getAOLength());
        writer.write(scene.getSampleRadius());
        writer.write(scene.getSampleCount());
        writer.write(scene.getBackgroundColor());

        return this;
    }

    @Override
    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {
        File mapFile = reader.readFile();

        scene.setLightMapWidth(reader.readInteger());
        scene.setLightMapHeight(reader.readInteger());
        scene.setAOStrength(reader.readFloat());
        scene.setAOLength(reader.readFloat());
        scene.setSampleRadius(reader.readFloat());
        scene.setSampleCount(reader.readInteger());
        reader.readVector4f(scene.getBackgroundColor());

        if(mapFile != null) {
            loadMap(mapFile);
        }

        return this;
    }

    private void loadMap(File file) throws Exception {
        Node node = NodeBuilder.load(file).build();
        final Vector<Node> particleNodes = new Vector<>();
        final Game game = Game.getInstance();

        detachAllChildren();

        particleNodes.clear();

        node.traverse((n) -> {
            if(n.hasMesh()) {
                n.getAmbientColor().set(0.2f, 0.2f, 0.2f, 1);
                n.setLightMapEnabled(true);
                n.setCastsShadow(true);
                n.setCollidable(true);

                Texture texture = n.getTexture();

                if(texture != null) {
                    if(IO.fileNameWithOutExtension(texture.getFile()).equals("torch")) {
                        n.setCollidable(false);
                        
                        for(int i = 0; i != n.getFaceCount(); i++) {
                            if(n.getVertexComponent(n.getFaceVertex(i, 0), 8) > 0.8f) {
                                BoundingBox b = new BoundingBox();

                                for(int j = 0; j != n.getFaceVertexCount(i); j++) {
                                    b.add(
                                        n.getVertexComponent(n.getFaceVertex(i, j), 0),
                                        n.getVertexComponent(n.getFaceVertex(i, j), 1),
                                        n.getVertexComponent(n.getFaceVertex(i, j), 2)
                                    );
                                }

                                ParticleSystem particles = game.getAssets().load(IO.file("assets/particles/fire.par"));
                                Node particlesNode = new Node();

                                particlesNode.setRenderable(particles.newInstance());
                                particlesNode.setVertexColorEnabled(true);
                                particlesNode.setDepthState(DepthState.READONLY);
                                particlesNode.setBlendState(BlendState.ADDITIVE);
                                particlesNode.setZOrder(10);
                                particlesNode.setTexture(game.getAssets().load(IO.file("assets/particles/fire.png")));
                                b.getCenter(particlesNode.getPosition());
                                
                                particleNodes.add(particlesNode);
                            }
                        }
                    }
                }
            }
            return true;
        });
        addChild(node);
        for(Node iNode : particleNodes) {
            addChild(iNode);
        }
        mapIndex = mapNames.indexOf(IO.fileNameWithOutExtension(file));
    }
}
