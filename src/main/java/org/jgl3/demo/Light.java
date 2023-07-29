package org.jgl3.demo;

import org.jgl3.IO;
import org.jgl3.scene.ArgumentReader;
import org.jgl3.scene.ArgumentWriter;
import org.jgl3.scene.Node;
import org.jgl3.scene.NodeBuilder;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;

public class Light extends Node {

    public Light() {
        setLight(true);
    }

    @Override
    public Node init(Scene scene) throws Exception {
        if(scene.getInDesign()) {
            addChild(NodeBuilder.load(IO.file("assets/ui/ui.obj")).build());
            getChild(0).setTextureLinear(true);
        }
        return this;
    }

    @Override
    public boolean handleUI(Scene scene, boolean reset) throws Exception {
        UIManager ui = UIManager.getInstance();
        Float r;

        ui.textField("Light.color.field", 0, "Color", getLightColor(), reset, 25);
        ui.addRow(5);
        if((r = ui.textField("Light.radius.field", 0, "Radius", getLightRadius(), reset, 10)) != null) {
            setLightRadius(r);
        }
        return true;
    }
    
    @Override
    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {
        writer.write(getPosition());
        writer.write(getLightColor());
        writer.write(getLightRadius());

        return this;
    }

    @Override
    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {

        reader.readVector3f(getPosition());
        reader.readVector4f(getLightColor());
        setLightRadius(reader.readFloat());

        return this;
    }
}
