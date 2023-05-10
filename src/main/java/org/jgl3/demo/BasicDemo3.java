package org.jgl3.demo;

import java.io.File;

import org.jgl3.Blur;
import org.jgl3.Dilate;
import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Log;
import org.jgl3.PixelFormat;
import org.jgl3.RenderTarget;
import org.jgl3.Renderer;
import org.jgl3.Resource;
import org.jgl3.Texture;
import org.jgl3.scene.Collider;
import org.jgl3.scene.LightMapper;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.jgl3.ui.UIManager;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

public class BasicDemo3 extends Demo {

    private final File lmFile = IO.file("assets/basic.demo.3.png");
    private final Collider collider = new Collider();
    private Scene scene = null;
    private float baseLength = 0;
    private RenderTarget target;
    private Dilate dilate;
    private Blur blur;
    private boolean blurEnabled;

    @Override
    public void init() throws Exception {
        Node node = new Node();
        Game game = Game.getInstance();
        int w = game.getWidth();
        int h = game.getHeight();
        int s = game.getScale();

        scene = new Scene();

        scene.getCamera().getTarget().y = 32;
        scene.setLightMapWidth(64);
        scene.setLightMapHeight(64);

        node.setLight(true);
        node.setLightRadius(300);
        node.setLightSampleCount(64);
        node.getPosition().y = 100;
        scene.getRoot().addChild(node);

        addCube(0, 150, 0, 300, 1, true)
            .calcTextureCoordinates(0, 0, 0, 100);
        addCube(0, 50, 0, 32, 2, false)
            .calcTextureCoordinates(0, 0, 0, 100)
            .getRotation()
            .rotate((float)Math.toRadians(45), 1, 0, 0)
            .rotate((float)Math.toRadians(45), 0, 0, 1);
        scene.getRoot().getLastChild().getLayerColor().set(0, 0, 0, 1);

        new LightMapper().light(lmFile, scene, false);

        baseLength = scene.getCamera().getOffset().length();
        collider.setIntersectionBits(1);

        dilate = game.getAssets().manage(new Dilate(w / s, h / s));
        blur = game.getAssets().manage(new  Blur(w / s, h / s));
        blur.scaleOffset(2);
        target = game.getAssets().manage(new RenderTarget(w / s, h / s, PixelFormat.COLOR, PixelFormat.COLOR, PixelFormat.COLOR));

        blurEnabled = false;
    }

    @Override
    public boolean run() throws Exception {
        Game game = Game.getInstance();
        UIManager ui = game.getUI();
        Font font = ui.getFont();
        int s = game.getScale();
        int w = game.getWidth();
        int h = game.getHeight();
        int fs = font.getScale();
        int ch = font.getCharHeight();
        Renderer renderer = game.getRenderer();

        if(w > 50 && h > 50 && (w / s != target.getWidth() || h / s != target.getHeight())) {
            Log.put(1, "Resizing render targets ...");
            game.getAssets().unManage(dilate);
            game.getAssets().unManage(blur);
            game.getAssets().unManage(target);
            dilate = game.getAssets().manage(new Dilate(w / s, h / s));
            blur = game.getAssets().manage(new Blur(w / s, h / s));
            blur.scaleOffset(2);
            target = game.getAssets().manage(new RenderTarget(w / s, h / s, PixelFormat.COLOR, PixelFormat.COLOR, PixelFormat.COLOR));
        }

        if(game.isButtonDown(1)) {
            scene.getCamera().rotateAroundTarget(-game.getDX() * 0.025f, game.getDY() * 0.025f);
        }

        target.begin();
        scene.render(game.getAspectRatio());
        renderer.end();
        target.end();
        
        Texture texture = dilate.process(target.getTexture(1), (blurEnabled) ? 8 : 2);

        if(blurEnabled) {
            texture = blur.process(texture);
        }

        GFX.clear(0, 0, 1, 1);
        renderer.begin();
        renderer.initSprites();
        renderer.setTexture(target.getTexture(0));
        renderer.beginTriangles();
        renderer.push(0, 0, w / s, h / s, 0, 0, w, h, 1, 1, 1, 1, true);
        renderer.endTriangles();
        renderer.setTexture(texture);
        renderer.beginTriangles();
        renderer.push(0, 0, w / s, h / s, 0, 0, w, h, 1, 1, 1, 1, true);
        renderer.endTriangles();
        renderer.setTexture(target.getTexture(2));
        renderer.beginTriangles();
        renderer.push(0, 0, w / s, h / s, 0, 0, w, h, 1, 1, 1, 1, true);
        renderer.endTriangles();
        renderer.setFont(font);
        renderer.beginTriangles();
        renderer.push(
            "FPS=" + game.getFrameRate() + 
            ", RES=" + Resource.getInstances() + 
            ", TRI=" + scene.getTrianglesRendered() + 
            ", ESC=Quit",
            5 * s, h - 5 * s - ch * fs, 5 * s,
            1, 1, 1, 1
        );
        renderer.endTriangles();
        ui.begin();
        if(ui.button("BasicDemo3.linear.button", 0, "Linear", scene.isLinear())) {
            scene.setLinear(!scene.isLinear());
        }
        if(ui.button("BasicDemo3.map.button", 5, "Map", false)) {
            new LightMapper().light(lmFile, scene, true);
        }
        if(ui.button("BasicDemo3.blur.button", 5, "Blur", blurEnabled)) {
            blurEnabled = !blurEnabled;
        }
        ui.end();
        renderer.end();

        Vector3f offset = scene.getCamera().getOffset().normalize(baseLength);

        collider.getOrigin().set(scene.getCamera().getTarget());
        collider.getDirection().set(offset).normalize();
        collider.setTime(baseLength + 14);
        if(collider.intersect(scene.getRoot()) != null) {
            offset.normalize(Math.min(baseLength, collider.getTime()) - 14);
        }
        scene.getCamera().getTarget().add(offset, scene.getCamera().getEye());

        return !game.isKeyDown(GLFW.GLFW_KEY_ESCAPE);
    }
    

    private Node addCube(int x, int y, int z, int size, int triangleTag, boolean invert) throws Exception {
        Node node = new Node();

        node.getPosition().set(x, y, z);
        node.addBox(size, size, size);

        if(invert) {
            node.swapWinding();
        }
        node.getAmbientColor().set(0.1f, 0.1f, 0.1f, 1);
        node.setLightMapEnabled(true);
        node.setCollidable(true);
        node.setTriangleTag(triangleTag);
        node.setTexture(Game.getInstance().getAssets().load(IO.file("assets/maps/dirt2.png")));

        scene.getRoot().addChild(node);

        return node;
    }
}
