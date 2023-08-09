package org.jgl3;

import java.util.UUID;
import java.util.Vector;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Node {

    public static interface Visitor {
        boolean visit(Scene scene, Node node) throws Exception;
    }
    
    private final String id = UUID.randomUUID().toString();
    private String name = "Node";
    private String info = "Node information";
    private boolean visible = true;
    private final Vector3f position = new Vector3f();
    private final Vector3f absolutePosition = new Vector3f();
    private final Matrix4f rotation = new Matrix4f();
    private final Vector3f scale = new Vector3f(1, 1, 1);
    private final Matrix4f model = new Matrix4f();
    private final Matrix4f localModel = new Matrix4f();
    private int zOrder = 0;
    private final Vector<Node> children = new Vector<>();
    private Node parent = null;

    public final String getID() {
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

    public final boolean isVisible() {
        return visible;
    }

    public final void setVisible(boolean visible) {
        this.visible = visible;
    }

    public final Vector3f getPosition() {
        return position;
    }

    public final Vector3f getAbsolutePosition() {
        return absolutePosition;
    }

    public final Matrix4f getRotation() {
        return rotation;
    }

    public final Vector3f getScale() {
        return scale;
    }

    public final Matrix4f getModel() {
        return model;
    }

    public final Matrix4f getLocalModel() {
        return localModel;
    }

    public final int getZOrder() {
        return zOrder;
    }

    public final void setZOrder(int zOrder) {
        this.zOrder = zOrder;
    }

    public final int getChildCount() {
        return children.size();
    }

    public final Node getChild(int i) {
        return children.get(i);
    }

    public final Node lastChild() {
        return children.lastElement();
    }
    
    public final Node getParent() {
        return parent;
    }

    public final Node getRoot() {
        Node root = this;

        while(root.getParent() != null) {
            root = root.getParent();
        }
        return root;
    }

    public final void detachFromParent() {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
    }

    public final void detachAllChildren() {
        while(getChildCount() != 0) {
            getChild(0).detachFromParent();
        }
    }

    public final void addChild(Node child) {
        child.detachFromParent();
        child.parent = this;
        children.add(child);
    }

    public final void traverse(Scene scene, Visitor visitor) throws Exception {
        if(visitor.visit(scene, this)) {
            for(int i = 0; i != getChildCount(); i++) {
                getChild(i).traverse(scene, visitor);
            }
        }
    }

    public void calcModel() {
        calcLocalModel();
        getModel().set(getLocalModel());
        if(getParent() != null) {
            getParent().getModel().mul(getModel(), getModel());
        }
        getAbsolutePosition().zero();
        getAbsolutePosition().mulPosition(getModel());
        for(int i = 0; i != getChildCount(); i++) {
            getChild(i).calcModel();
        }
    }

    public void update(Scene scene) throws Exception {
    }

    public void calcLocalModel() {
        localModel.identity().translate(position).mul(rotation).scale(scale);
    }

    public boolean isRenderable() {
        return false;
    }

    public State render(Scene scene, Vector<Light> lights, Camera camera, State lastState) throws Exception {
        return null;
    }
}
