package org.jgl3.demo;

import java.io.File;
import java.util.Vector;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.scene.ArgumentReader;
import org.jgl3.scene.ArgumentWriter;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeBuilder;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;

public class Platform extends Node {

    private static Vector<String> tileNames = new Vector<>();

    static {
        File[] files = IO.file("assets/tiles").listFiles();

        if(files != null) {
            for(File file : files) {
                if(IO.extension(file).equals(".png")) {
                    tileNames.add(IO.fileNameWithOutExtension(file));
                }
            }
        }
    }

    private int xdivs = 1;
    private int zdivs = 1;
    private int selTile = -1;
    private int tileIndex = -1;

    @Override
    public Node init(Scene scene) throws Exception {
        selTile = -2;
        tileIndex = -1;
        build();

        return this;
    }

    @Override
    public boolean handleUI(Scene scene, boolean reset) throws Exception {
        UIManager ui = UIManager.getInstance();
        Game game = Game.getInstance();
        Object r;

        if(reset) {
            selTile = tileIndex;
        }
        if((r = ui.list("Platform.texture.list", 0, tileNames, 20, 8, selTile)) != null) {
            tileIndex = (Integer)r;
            setTexture(game.getAssets().load(IO.file(IO.file("assets/tiles"), tileNames.get(tileIndex) + ".png")));
        }
        selTile = -2;
        ui.addRow(5);
        if((r = ui.textField("Platform.x.divs.field", 0, "X", xdivs, reset, 6)) != null) {
            xdivs = (Integer)r;
        }
        ui.addRow(5);
        if((r = ui.textField("Platform.z.divs.field", 0, "Z", zdivs, reset, 6)) != null) {
            zdivs = (Integer)r;
        }
        ui.addRow(5);
        ui.textField("Platform.ambient.color.field", 0, "Ambient", getAmbientColor(), reset, 20);
        ui.addRow(5);
        ui.textField("Platform.diffuse.color.field", 0, "Diffuse", getDiffuseColor(), reset, 20);
        ui.addRow(5);
        if(ui.button("Platform.lit.button", 0, "Lit", isLightingEnabled())) {
            setLightingEnabled(!isLightingEnabled());
        }
        if(ui.button("Platform.build.button", 5, "Build", false)) {
            build();
        }
        return true;
    }
    
    @Override
    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {

        writer.write(getTexture());
        writer.write(getRotation());
        writer.write(getPosition());
        writer.write(xdivs);
        writer.write(zdivs);
        writer.write(getAmbientColor());
        writer.write(getDiffuseColor());
        writer.write(isLightingEnabled());

        return this;
    }

    @Override
    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {
        setTexture(reader.readTexture());
        reader.readRotation(getRotation());
        reader.readVector3f(getPosition());
        xdivs = reader.readInteger();
        zdivs = reader.readInteger();
        reader.readVector4f(getAmbientColor());
        reader.readVector4f(getDiffuseColor());
        setLightingEnabled(reader.readBoolean());

        return this;
    }

    private void build() throws Exception {
        NodeBuilder builder = new NodeBuilder();

        builder.addBox(0, 0, 0, xdivs * 32, 32, zdivs * 32, xdivs, 1, zdivs, getTexture(), false);
        builder.calcNormals(false);
        builder.calcTextureCoordinates(xdivs * 16, 16, zdivs * 16, 32, 0);
        builder.subdivide(3);
        builder.smooth(8);
        builder.calcNormals(true);

        detachAllChildren();
        addChild(builder.build());
        getChild(0).setLightingEnabled(isLightingEnabled());
        getChild(0).getAmbientColor().set(getAmbientColor());
        getChild(0).getDiffuseColor().set(getDiffuseColor());

        if(getTexture() != null) {
            tileIndex = tileNames.indexOf(IO.fileNameWithOutExtension(getTexture().getFile()));
        }
    }
}
