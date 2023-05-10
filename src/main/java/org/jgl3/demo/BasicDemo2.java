package org.jgl3.demo;

import java.util.Vector;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.KeyFrameMesh;
import org.jgl3.scene.KeyFrameMeshLoader;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.lwjgl.glfw.GLFW;

public class BasicDemo2 extends Demo {

    private static final Object[][] SEQUENCES = KeyFrameMeshLoader.cloneSequences();

    private Scene scene;
    private final Vector<String> names = new Vector<>();
    private int selSeq = -1;

    public BasicDemo2() {
        for(Object[] seq : SEQUENCES) {
            names.add((String)seq[0]);
        }
    }

    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();
        Node node = new Node();
        KeyFrameMesh mesh = game.getAssets().load(IO.file("assets/md2/hero.md2"));

        scene = new Scene();
        scene.getCamera().getEye().set(40, 40, 40);

        mesh = (KeyFrameMesh)mesh.newInstance();
        mesh.setSequence(0, 39, 10, true);
        node.setRenderable(mesh);
        node.setTexture(game.getAssets().load(IO.file("assets/md2/hero.png")));
        node.setLightingEnabled(true);
        node.getAmbientColor().set(0.2f, 0.2f, 0.6f, 1);
        node.getRotation().rotate((float)Math.toRadians(-90), 1, 0, 0);
        scene.getRoot().addChild(node);

        node = new Node();
        node.setLight(true);
        node.getLightColor().set(2, 1, 0, 1);
        node.getPosition().set(100, 100, 100);
        scene.getRoot().addChild(node);

        selSeq = 0;
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        UIManager ui = game.getUI();
        Font font = ui.getFont();
        int h = game.getHeight();
        int s = game.getScale();
        int fs = font.getScale();
        int ch = font.getCharHeight();
        Object result;

        if(game.isButtonDown(1)) {
            scene.getCamera().rotateAroundTarget(-game.getDX() * 0.025f, game.getDY() * 0.025f);
        }
        
        scene.render(game.getAspectRatio());
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS=" + game.getFrameRate() + 
            ", RES=" + Resource.getInstances() + 
            ", TRI=" + scene.getTrianglesRendered() + 
            ", ESC=Quit",
            5 * s, h - ch * fs - 5 * s, 5 * s, 
            1, 1, 1, 1
        );
        renderer.endTriangles();
        ui.begin();
        if((result = ui.list("BasicDemo2.seq.list", 0, names, 20, 10, selSeq)) != null) {
            KeyFrameMesh mesh = (KeyFrameMesh)scene.getRoot().getChild(0).getRenderable();
            Object[] seq = SEQUENCES[(Integer)result];
            
            mesh.setSequence((Integer)seq[1], (Integer)seq[2], (Integer)seq[3], (Boolean)seq[4]);
        }
        selSeq = -2;
        ui.end();
        renderer.end();

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    
}
