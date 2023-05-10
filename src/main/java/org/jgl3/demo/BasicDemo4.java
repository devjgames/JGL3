package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.scene.Collider;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.lwjgl.glfw.GLFW;

public class BasicDemo4 extends Demo {
    
    private final Collider collider = new Collider();
    private Scene scene = null;

    @Override
    public void init() throws Exception {
        Node node, child;
        Game game = Game.getInstance();
        
        scene = new Scene();
        scene.setLightMapWidth(64);
        scene.setLightMapHeight(64);

        node = new Node();
        node.addBox(300, 300, 300);
        node.swapWinding();
        node.getPosition().y = 150;
        node.setTexture(game.getAssets().load(IO.file("assets/maps/dirt2.png")));
        node.calcTextureCoordinates(0, 0, 0, 100);
        node.setCollidable(true);
        node.setLightMapEnabled(true);
        node.getAmbientColor().set(0.2f, 0.2f, 0.2f, 1);
        scene.getRoot().addChild(node);

        child = new Node();
        child.addBox(150, 4, 50);
        child.getPosition().set(75, 2, 0);
        child.setTexture(game.getAssets().load(IO.file("assets/maps/rock1.png")));
        child.calcTextureCoordinates(0, 0, 0, 50);
        child.setCollidable(true);
        child.setLightMapEnabled(true);
        child.getAmbientColor().set(0.2f, 0.2f, 0.2f, 1);
        node = new Node();
        node.addChild(child);
        node.getPosition().set(25, 0, 125);
        node.getRotation().rotate((float)Math.toRadians(30), 0, 0, 1);
        scene.getRoot().addChild(node);

        child = new Node();
        child.addBox(150, 4, 50);
        child.getPosition().set(-75, 2, 0);
        child.setTexture(game.getAssets().load(IO.file("assets/maps/rock1.png")));
        child.calcTextureCoordinates(0, 0, 0, 50);
        child.setCollidable(true);
        child.setLightMapEnabled(true);
        child.getAmbientColor().set(0.2f, 0.2f, 0.2f, 1);
        node = new Node();
        node.addChild(child);
        node.getPosition().set(-25, 0, -125);
        node.getRotation().rotate((float)Math.toRadians(-30), 0, 0, 1);
        scene.getRoot().addChild(node);

        node = new Node();
        node.setLight(true);
        node.getPosition().set(0, 100, 0);
        scene.getRoot().addChild(node);

        scene.getCamera().getEye().set(0, 50, 0);
        scene.getCamera().getEye().add(1, 0, 1, scene.getCamera().getTarget());

        scene.setAOStrength(1.5f);

        new LightMapper().light(IO.file("assets/basic.demo.4.png"), scene, false);

        collider.setLoopCount(4);

        game.enableFPSMouse();
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        Renderer renderer = game.getRenderer();
        Font font = game.getUI().getFont();
        int s = game.getScale();

        scene.render(game.getAspectRatio());
        renderer.initSprites();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS = " + game.getFrameRate() + "\n" +
            "RES = " + Resource.getInstances() + "\n" +
            "TRI = " + scene.getTrianglesRendered() + "\n" +
            "COL = " + scene.getCollidableTriangles() + "\n" +
            "TST = "  + collider.getTested() + "\n" +
            "ESC = Quit",
            5 * s, 5  * s, 5 * s,
            1, 1, 1, 1
        );
        renderer.endTriangles();
        renderer.end();

        collider.move(scene, 100);

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }

}
