package org.jgl3.demo;

import java.util.Vector;

import org.jgl3.AmbientLight;
import org.jgl3.Camera;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Light;
import org.jgl3.LightPipeline;
import org.jgl3.MeshPTN;
import org.jgl3.RenderTarget;
import org.jgl3.Scene;
import org.jgl3.Size;
import org.jgl3.UIManager;
import org.jgl3.VertexUsage;
import org.jgl3.demo.App.Demo;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class UI extends Demo {

    private class MouseCamera extends Camera {

        private final Vector3f right = new Vector3f();
        private final Vector3f up = new Vector3f();
        private final Vector3f direction = new Vector3f();
        private final Matrix4f matrix = new Matrix4f();
        private final UIManager ui;
        private final String windowKey;
        private final String viewKey;

        public MouseCamera(UIManager ui, String windowKey, String viewKey) {
            
            this.ui = ui;
            this.windowKey = windowKey;
            this.viewKey = viewKey;
        }

        @Override
        public void update(Scene scene) {

            if(ui.isViewButtonDown(0, windowKey, viewKey)) {

                int dX = ui.getViewDX(windowKey, viewKey);
                int dY = ui.getViewDY(windowKey, viewKey);

                getRotation().getRow(1, up);
                matrix.identity().rotate(dX * -0.025f, 0,1 , 0);
                getPosition().cross(up, right).mulDirection(matrix).normalize();
                getPosition().mulDirection(matrix);
                matrix.identity().rotate(dY * 0.025f, right);
                right.cross(getPosition(), up).mulDirection(matrix).normalize();
                getPosition().mulDirection(matrix);
                getPosition().normalize(direction).negate();
                look(direction, up);
            }
        }
    }


    private UIManager ui = null;
    private String activateKey = null;
    private Vector<Object> items = new Vector<>();
    private String item = null;
    private Scene scene = null;
    private Light light;
    private boolean reset = false;
    
    @Override
    public void init() throws Exception {
        Game game = Game.getInstance();

        ui = game.getAssets().manage(new UIManager());

        activateKey = "UI.main";
        item = null;

        items.clear();
        populate();

        scene = new Scene();

        MeshPTN mesh = new MeshPTN(game.getAssets().manage(new LightPipeline()));

        mesh.getPipeline().push(-50, 0, -50, 0, 0, 0, 1, 0);
        mesh.getPipeline().push(-50, 0, +50, 0, 2, 0, 1, 0);
        mesh.getPipeline().push(+50, 0, +50, 2, 2, 0, 1, 0);
        mesh.getPipeline().push(+50, 0, -50, 2, 0, 0, 1, 0);
        mesh.getPipeline().push(0, 1, 2, 3);
        mesh.getPipeline().push(3, 2, 1, 0);
        mesh.getPipeline().bufferVertices(VertexUsage.STATIC, true);
        mesh.getPipeline().bufferIndices(VertexUsage.STATIC, true);
        mesh.setTexture(game.getAssets().load(IO.file("assets/textures/checker.png")));

        scene.getRoot().addChild(mesh);

        Camera camera = new MouseCamera(ui, "UI.main",  "view");
        
        camera.getPosition().set(100, 100, 100);
        camera.look(-1, -1, -1, 0, 1, 0);
        scene.getRoot().addChild(camera);
        camera.activate();

        light = new AmbientLight();
        light.getColor().set(0.5f, 0.5f, 0.5f, 1);
        scene.getRoot().addChild(light);

        reset = true;
    }

    @Override
    public void renderTargets() throws Exception {
        RenderTarget target = ui.getViewRenderTarget("UI.main", "view");

        if(target != null) {
            target.begin();
            scene.render(target);
            target.end();
        }
    }

    @Override
    public void nextFrame(Size size) throws Exception {

        Object r;

        GFX.clear(0.4f, 0.4f, 0.4f, 1);
        
        ui.begin();

        ui.beginWindow("UI.main", "Main", 10, 10);
        
        if((r = ui.list("list", 0, items, 8, 10, -2)) != null) {
            item = items.get((Integer)r).toString();
        }
        ui.addRow(5);

        if(items.size() != 0) {
            if(ui.button("clear", 0, "Clear", false)) {
                items.clear();
                item = null;
            }
        } else if(ui.button("populate", 0, "Populate", false)) {
            populate();
        }
        if(item != null) {
            ui.label("item", 5, "Selected Item = " + item);
        }

        ui.moveRightOf("list", 5);
        ui.view("view", 0, 8, 30);

        ui.endWindow();

        ui.beginWindow("UI.colors", "Colors", 10, 300);
        ui.textField("ambient.field", 0, light.getColor(), reset, 20);
        ui.label("ambient.label", 0, "Ambient Color");
        ui.addRow(5);
        ui.textField("background.field", 0, scene.getBackgroundColor(), reset, 20);
        ui.label("background.label", 0, "Background Color");
        ui.endWindow();

        reset = false;

        activateKey = ui.end(size, activateKey);
    }

    private void populate() {
        items.add("Apples");
        items.add("Oranges");
        items.add("Grapes");
        items.add("Banannas");
        items.add("Pears");
        items.add("Blue Berries");
        items.add("Water Melon");
        items.add("Straw Berries");
        items.add("Grape Fruit");

        for(int i = 0; i != 100; i++) {
            items.add(i);
        }
    }
}
