package org.jgl3;

import java.util.UUID;
import java.util.Vector;

import org.joml.Vector4f;

public class Scene {
    
    private final String id = UUID.randomUUID().toString();
    private String name = "Scene";
    private String info = "Scene information";
    private final Node root = new Node();
    private Camera camera = null;
    private final Vector<Node> renderables = new Vector<>();
    private final Vector<Light> lights = new Vector<>();
    private final Vector4f backgroundColor = new Vector4f(0.75f, 0.75f, 0.75f, 1);
    private boolean lightMapLinear = false;

    public String getID() {
        return id;
    }

    public final String getName() {
        return name;
    }

    public final void setName(String name) {
        this.name = name;
    }

    public final String getInfo() {
        return info;
    }

    public final void setInfo(String info) {
        this.info = info;
    }

    public Node getRoot() {
        return root;
    }

    public Vector4f getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isLightMapLinear() {
        return lightMapLinear;
    }

    public void setLightMapLinear(boolean linear) {
        lightMapLinear = linear;
    }

    public Camera getCamera() {
        return camera;
    }

    public void findActiveCamera() throws Exception {
        camera = null;

        getRoot().traverse(this, (scene, node) -> {
            if(node instanceof Camera) {
                Camera c = (Camera)node;

                if(c.isActive()) {
                    camera = c;
                }
            }
            return true;
        });
    }

    public void render(Size size) throws Exception {

        getRoot().traverse(this, (scene, node) -> {
            node.preUpdate(scene);
            
            return true;
        });

        getRoot().traverse(this, (scene, node) -> {
            node.update(scene);
            
            return true;
        });

        getRoot().traverse(this, (scene, node) -> {
            node.postUpdate(scene);
            
            return true;
        });

        getRoot().calcModel();

        findActiveCamera();

        if(camera != null) {
            renderables.clear();
            lights.clear();

            camera.calcProjection(size);

            getRoot().traverse(this, (scene, node) -> {
                if(node.isVisible()) {
                    if(node.isRenderable()) {
                        renderables.add(node);
                    }
                    if(node instanceof Light) {
                        lights.add((Light)node);
                    }
                    return true;
                }
                return false;
            });

            renderables.sort((a, b) -> {
                if(a == b) {
                    return 0;
                } else if(a.getZOrder() == b.getZOrder()) {
                    float da = camera.getAbsolutePosition().distance(a.getAbsolutePosition());
                    float db = camera.getAbsolutePosition().distance(b.getAbsolutePosition());

                    return Float.compare(db, da);
                } else {
                    return Integer.compare(a.getZOrder(), b.getZOrder());
                }
            });
            lights.sort((a, b) -> {
                if(a == b) {
                    return 0;
                } else {
                    float da = camera.getAbsolutePosition().distance(a.getAbsolutePosition());
                    float db = camera.getAbsolutePosition().distance(b.getAbsolutePosition());

                    return Float.compare(da, db);
                }
            });

            State lastState = null;

            GFX.clear(backgroundColor.x, backgroundColor.y, backgroundColor.z, backgroundColor.w);

            for(Node node : renderables) {
                lastState = node.render(this, lights, camera, lastState);
            }

            renderables.clear();
            lights.clear();
        }
    }
}
