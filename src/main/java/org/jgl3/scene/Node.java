package org.jgl3.scene;

import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.CullState;
import org.jgl3.DepthState;
import org.jgl3.Game;
import org.jgl3.Log;
import org.jgl3.OctTree;
import org.jgl3.Renderer;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class Node {

    public static interface Visitor {
        boolean visit(Node node) throws Exception;
    }

    private String name = "Node";
    private String tag = "";
    private boolean visible = true;
    private boolean collidable = false;
    private boolean dynamic = false;
    private boolean lightingEnabled = false;
    private boolean vertexColorEnabled = false;
    private boolean lightMapEnabled = false;
    private boolean castsShadow = false;
    private boolean receivesShadow = true;
    private boolean isLight = false;
    private float lightRadius = 300;
    private DepthState depthState = DepthState.READWRITE;
    private BlendState blendState = BlendState.OPAQUE;
    private CullState cullState = CullState.BACK;
    private boolean followEye = false;
    private Texture texture = null;
    private Texture texture2 = null;
    private Renderable renderable = null;
    private final Vector4f lightColor = new Vector4f(1, 1, 1, 1);
    private final Vector4f ambientColor = new Vector4f(0, 0, 0, 1);
    private final Vector4f diffuseColor = new Vector4f(1, 1, 1, 1);
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private final Vector3f absolutePosition = new Vector3f();
    private final Vector3f position = new Vector3f();
    private final Vector3f scale = new Vector3f(1, 1, 1);
    private final Matrix4f rotation = new Matrix4f();
    private final Matrix4f model = new Matrix4f();
    private final BoundingBox bounds = new BoundingBox();
    private int triangleTag = 1;
    private int zOrder = 0;
    private Object data = null;
    private Node parent = null;
    private final Vector<Node> children = new Vector<>();
    private OctTree octTree = null;
    private int minTrisPerTree = 16;
    private Vector3f warpAmplitude = new Vector3f(8, 8, 8);
    private float warpSpeed = 1;
    private float warpFrequency = 0.05f;
    private boolean warpEnabled = false;
    private boolean textureLinear = false;
    private boolean textureClampToEdge = false;
    private boolean texture2Linear = true;
    private boolean texture2ClampToEdge = true;
    private int textureUnit = 0;
    private final Vector<Float> vertices = new Vector<>();
    private final Vector<Integer> indices = new Vector<>();
    private final Vector<int[]> faces = new Vector<>();
    private float[] vertexArray = null;
    private final BoundingBox meshBounds = new BoundingBox();

    public String getName() {
        return name;
    }

    public Node setName(String name) {
        this.name = name;
        return this;
    }

    public String getTag() {
        return tag;
    }

    public Node setTag(String tag) {
        this.tag = tag;
        return this;
    }

    public boolean isVisible() {
        return visible;
    }

    public Node setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isCollidable() {
        return collidable;
    }

    public Node setCollidable(boolean collidable) {
        this.collidable = collidable;
        return this;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public Node setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
        return this;
    }

    public boolean isLightingEnabled() {
        return lightingEnabled;
    }

    public Node setLightingEnabled(boolean enabled) {
        lightingEnabled = enabled;
        return this;
    }

    public boolean isVertexColorEnabled() {
        return vertexColorEnabled;
    }

    public Node setVertexColorEnabled(boolean enabled) {
        vertexColorEnabled = enabled;
        return this;
    }

    public boolean isLightMapEnabled() {
        return lightMapEnabled;
    }

    public Node setLightMapEnabled(boolean enabled) {
        lightMapEnabled = enabled;
        return this;
    }

    public boolean getCastsShadow() {
        return castsShadow;
    }

    public Node setCastsShadow(boolean castsShadow) {
        this.castsShadow = castsShadow;
        return this;
    }

    public boolean getReceivesShadow() {
        return receivesShadow;
    }

    public Node setReceivesShadow(boolean receivesShadow) {
        this.receivesShadow = receivesShadow;
        return this;
    }

    public boolean isLight() {
        return isLight;
    }

    public Node setLight(boolean isLight) {
        this.isLight = isLight;
        return this;
    }

    public float getLightRadius() {
        return lightRadius;
    }

    public Node setLightRadius(float radius) {
        lightRadius = radius;
        return this;
    }

    public DepthState getDepthState() {
        return depthState;
    }

    public Node setDepthState(DepthState state) {
        depthState = state;
        return this;
    }

    public BlendState getBlendState() {
        return blendState;
    }

    public Node setBlendState(BlendState state) {
        blendState = state;
        return this;
    }

    public CullState getCullState() {
        return cullState;
    }

    public Node setCullState(CullState state) {
        cullState = state;
        return this;
    }

    public boolean getFollowEye() {
        return followEye;
    }

    public Node setFollowEye(boolean follow) {
        followEye = follow;
        return this;
    }

    public Texture getTexture() {
        return texture;
    }

    public Node setTexture(Texture texture) {
        setState(this.texture = texture, textureLinear, textureClampToEdge);
        return this;
    }

    public boolean isTextureLinear() {
        return textureLinear;
    }

    public Node setTextureLinear(boolean linear) {
        setState(texture, textureLinear = linear, textureClampToEdge);
        return this;
    }

    public boolean clampTextureToEdge() {
        return textureClampToEdge;
    }

    public Node setClampTextureToEdge(boolean clamp) {
        setState(texture, textureLinear, textureClampToEdge = clamp);
        return this;
    }

    public Texture getTexture2() {
        return texture2;
    }

    public Node setTexture2(Texture texture2) {
        setState(this.texture2 = texture2, texture2Linear, textureClampToEdge);
        return this;
    }

    public boolean isTexture2Linear() {
        return texture2Linear;
    }

    public Node setTexture2Linear(boolean linear) {
        setState(texture2, texture2Linear = linear, texture2ClampToEdge);
        return this;
    }

    public boolean clampTexture2ToEdge() {
        return texture2ClampToEdge;
    }

    public Node setClampTexture2ToEdge(boolean clamp) {
        setState(texture2, texture2Linear, texture2ClampToEdge = clamp);
        return this;
    }

    public int getTextureUnit() {
        return textureUnit;
    }

    public Node setTextureUnit(int unit) {
        textureUnit = unit;
        return this;
    }

    public Renderable getRenderable() {
        return renderable;
    }

    public Node setRenderable(Renderable renderable) {
        this.renderable = renderable;
        return this;
    }

    public Vector4f getLightColor() {
        return lightColor;
    }

    public Vector4f getAmbientColor() {
        return ambientColor;
    }

    public Vector4f getDiffuseColor() {
        return diffuseColor;
    }

    public Vector4f getColor() {
        return color;
    }
    
    public Vector3f getAbsolutePosition() {
        return absolutePosition;
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getScale() {
        return scale;
    }

    public Matrix4f getRotation() {
        return rotation;
    }

    public Matrix4f getModel() {
        return model;
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public int getTriangleTag() {
        return triangleTag;
    }

    public Node setTriangleTag(int tag) {
        triangleTag = tag;
        return this;
    }

    public int getZOrder() {
        return zOrder;
    }

    public Node setZOrder(int zOrder) {
        this.zOrder = zOrder;
        return this;
    }

    public Object getData() {
        return data;
    }

    public Node setData(Object data) {
        this.data = data;
        return this;
    }

    public Node getParent() {
        return parent;
    }

    public Node getRoot() {
        Node root = this;

        while(root.parent != null) {
            root = root.parent;
        }
        return root;
    }

    public int getChildCount() {
        return children.size();
    }

    public Node getChild(int i) {
        return children.get(i);
    }

    public Node getLastChild() {
        return children.lastElement();
    }

    public Node detachFromParent() throws Exception {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
        return this;
    }

    public Node addChild(Node child) throws Exception {
        child.detachFromParent().parent = this;
        children.add(child);
        return this;
    }

    public Node detachAllChildren() throws Exception {
        while(!children.isEmpty()) {
            children.get(0).detachFromParent();
        }
        return this;
    }

    public boolean hasMesh() {
        return getVertexCount() != 0 && getIndexCount() != 0;
    }

    public void clearMesh() {
        vertices.clear();
        indices.clear();
        faces.clear();
    }

    public int getVertexCount() {
        return vertices.size() / Renderer.COMPONENTS;
    }

    public float getVertexComponent(int i, int j) {
        return vertices.get(i * Renderer.COMPONENTS + j);
    }

    public Node setVertexComponent(int i, int j, float x) {
        vertices.set(i * Renderer.COMPONENTS + j, x);
        return this;
    }

    public Node calcMeshBounds() {
        meshBounds.clear();
        for(int i = 0; i != getVertexCount(); i++) {
            meshBounds.add(
                getVertexComponent(i, 0),
                getVertexComponent(i, 1),
                getVertexComponent(i, 2)
            );
        }
        return this;
    }

    public Node push(float x, float y, float z, float s, float t, float u, float v, float nx, float ny, float nz, float r, float g, float b, float a) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        vertices.add(s);
        vertices.add(t);
        vertices.add(u);
        vertices.add(v);
        vertices.add(nx);
        vertices.add(ny);
        vertices.add(nz);
        vertices.add(r);
        vertices.add(g);
        vertices.add(b);
        vertices.add(a);

        return this;
    }

    public int getIndexCount() {
        return indices.size();
    }

    public int getIndex(int i) {
        return indices.get(i);
    }

    public int getFaceCount() {
        return faces.size();
    }

    public int getFaceVertexCount(int i) {
        return faces.get(i).length;
    }

    public int getFaceVertex(int i, int j) {
        return faces.get(i)[j];
    }

    public Node push(int ... indices) {
        int tris = indices.length - 2;

        faces.add(indices.clone());

        for(int i = 0; i != tris; i++) {
            this.indices.add(indices[0]);
            this.indices.add(indices[i + 1]);
            this.indices.add(indices[i + 2]);
        }
        return this;
    }

    public Node compileMesh() {
        if(hasMesh()) {
            int v = 0;

            vertexArray = new float[indices.size() * Renderer.COMPONENTS];

            for(int i : indices) {
                int j = i * Renderer.COMPONENTS;

                for(int k = 0;  k != Renderer.COMPONENTS; k++, j++, v++) {
                    vertexArray[v] = vertices.get(j);
                }
            }
        }
        return this;
    }

    public Node clearCompiledMesh() {
        vertexArray = null;
        return this;
    }

    public void render(Scene scene) throws Exception {
        if(hasMesh()) {
            Renderer renderer = Game.getInstance().getRenderer();

            if(vertexArray != null) {
                renderer.render(vertexArray);
            } else {
                renderer.beginTriangles();
                for(int i : indices) {
                    int j = i * Renderer.COMPONENTS;

                    renderer.push(
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++),
                        vertices.get(j++)
                    );
                }
                renderer.endTriangles();
            }
        } else {
            Renderable renderable = getRenderable();

            renderable.render(scene, this);
        }
    }

    public Node calcBoundsAndTransform(Camera camera) {
        if(followEye && camera != null) {
            position.set(camera.getEye());
        }
        model 
            .identity()
            .translate(position)
            .mul(rotation)
            .scale(scale);
        if(parent != null) {
            parent.model.mul(model, model);
        }
        absolutePosition.zero().mulPosition(model);
        bounds.clear();
        if(hasMesh()) {
            bounds.set(meshBounds);
        } else if(renderable != null) {
            bounds.set(renderable.getBounds());
        }
        bounds.transform(model);

        for(int i = 0; i != getChildCount(); i++) {
            Node child = getChild(i);

            child.calcBoundsAndTransform(camera);
            bounds.add(child.getBounds());
        }
        return this;
    }

    public Node find(String name, boolean recursive) throws Exception {
        return find((n) -> {
            if(n.name.equals(name)) {
                return true;
            }
            return false;
        }, recursive);
    }

    public Node find(Visitor v, boolean recursive) throws Exception {
        for(int i = 0; i != getChildCount(); i++) {
            Node node = getChild(i);

            if(v.visit(node)) {
                return node;
            }
        }
        if(recursive) {
            for(int i = 0; i != getChildCount(); i++) {
                Node r = getChild(i).find(v, recursive);

                if(r != null) {
                    return r;
                }
            }
        }
        return null;
    }

    public Node traverse(Visitor v) throws Exception {
        if(v.visit(this)) {
            for(int i = 0; i != getChildCount(); i++) {
                getChild(i).traverse(v);
            }
        }
        return this;
    }

    public int getTriangleCount() {
        int count = 0;

        if(hasMesh()) {
            return getIndexCount() / 3;
        } else if(renderable != null) {
            count = renderable.getTriangleCount();
        }
        return count;
    }

    public Triangle getTriangle(int i, Triangle triangle) {
        if(hasMesh()) {
            i *= 3;
            triangle.getP1().set(
                getVertexComponent(getIndex(i + 0), 0),
                getVertexComponent(getIndex(i + 0), 1),
                getVertexComponent(getIndex(i + 0), 2)
            );
            triangle.getP2().set(
                getVertexComponent(getIndex(i + 1), 0),
                getVertexComponent(getIndex(i + 1), 1),
                getVertexComponent(getIndex(i + 1), 2)
            );
            triangle.getP3().set(
                getVertexComponent(getIndex(i + 2), 0),
                getVertexComponent(getIndex(i + 2), 1),
                getVertexComponent(getIndex(i + 2), 2)
            );
            triangle
                .calcPlane()
                .setTag(triangleTag)
                .transform(model)
                .setData(this);
        } else if(renderable != null) {
            renderable
                .getTriangle(i, triangle)
                .setTag(triangleTag)
                .transform(model)
                .setData(this);
        }
        return triangle;
    }

    public OctTree getOctTree() {
        if(collidable && !dynamic && octTree == null && (renderable != null || hasMesh())) {
            Vector<Triangle> triangles = new Vector<>();

            Log.put(1, "Creating OctTree ...");
            for(int i = 0; i != getTriangleCount(); i++) {
                triangles.add(getTriangle(i, new Triangle()));
            }
            octTree = OctTree.create(triangles, minTrisPerTree);
        }
        return octTree;
    }

    public Node clearOctTree() {
        octTree = null;
        return this;
    }

    public int getMinTrisPerTree() {
        return minTrisPerTree;
    }

    public Node setMinTrisPerTree(int minTrisPerTree) {
        this.minTrisPerTree = minTrisPerTree;
        return this;
    }

    public boolean isWarpEnabled() {
        return warpEnabled;
    }

    public Node setWarpEnabled(boolean enabled) {
        warpEnabled = enabled;
        return this;
    }

    public Vector3f getWarpAmplitude() {
        return warpAmplitude;
    }

    public float getWarpSpeed() {
        return warpSpeed;
    }

    public Node setWarpSpeed(float speed) {
        warpSpeed = speed;
        return this;
    }

    public float getWarpFrequency() {
        return warpFrequency;
    }

    public Node setWarpFrequency(float frequency) {
        warpFrequency = frequency;
        return this;
    }

    public Node init(Scene scene) throws Exception {
        return this;
    }

    public Node update(Scene scene) throws Exception {
        return this;
    }

    public Node handleMouse(Scene scene) throws Exception {
        return this;
    }

    public boolean handleUI(Scene scene, boolean reset) throws Exception {
        return false;
    }

    public Node serialize(Scene scene, ArgumentWriter writer) throws Exception {
        return this;
    }

    public Node deserialize(Scene scene, ArgumentReader reader) throws Exception {
        return this;
    }

    private void setState(Texture texture, boolean linear, boolean clampToEdge) {
        if(texture != null) {
            if(linear) {
                texture.toLinear(clampToEdge);
            } else {
                texture.toNearest(clampToEdge);
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
