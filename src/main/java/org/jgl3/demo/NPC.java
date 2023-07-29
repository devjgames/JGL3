package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.scene.ArgumentReader;
import org.jgl3.scene.ArgumentWriter;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;

public class NPC extends Node {
    
    private static final Vector<String> meshNames = new Vector<>();

    static {
        File[] files = IO.file("assets/npc").listFiles();

        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".md2")) {
                    meshNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
    }

    private int selMesh = -1;
    private int meshIndex = -1;

    @Override
    public boolean handleUI(Scene scene, boolean reset) throws Exception {
        Object r;
        UIManager ui = UIManager.getInstance();

        if(reset) {
            selMesh = meshIndex;
        }
        if((r = ui.list("NPC.mesh.list", 0, meshNames, 20, 8, selMesh)) != null) {
            loadMesh(IO.file(IO.file("assets/npc"), meshNames.get((Integer)r) + ".md2"));
        }
        selMesh = -2;
        return true;
    }

    @Override
    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {

        writer.write((meshIndex == -1) ? null : IO.file(IO.file("assets/npc"), meshNames.get(meshIndex) + ".md2"));
        writer.write(getPosition());
        writer.write(getRotation());

        return this;
    }

    @Override
    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {

        File file = reader.readFile();

        reader.readVector3f(getPosition());
        reader.readRotation(getRotation());

        loadMesh(file);

        return this;
    }

    private void loadMesh(File file) throws Exception {
        Game game = Game.getInstance();

        detachAllChildren();

        if(file != null) {
            KeyFrameMesh mesh = game.getAssets().load(file);
            Node node = new Node();

            node.setRenderable(mesh = (KeyFrameMesh)mesh.newInstance());
            mesh.setSequence(0, 39, 10, true);
            node.setTexture(game.getAssets().load(IO.file(IO.file("assets/npc"), IO.fileNameWithOutExtension(file) + ".png")));

            node.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
            node.getPosition().y = -mesh.getFrame(0).getBounds().getMin().z;

            node.setLightingEnabled(true);
            node.getAmbientColor().set(0.2f, 0.2f, 0.6f, 1);

            addChild(node);

            meshIndex = meshNames.indexOf(IO.fileNameWithOutExtension(file));
        } else {
            meshIndex = -1;
        }
    }
}
