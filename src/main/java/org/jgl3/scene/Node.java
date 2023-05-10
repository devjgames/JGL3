package org.jgl3.scene;

import java.io.IOException;
import java.io.Serializable;
import java.util.Vector;

import org.jgl3.BlendState;
import org.jgl3.BoundingBox;
import org.jgl3.CullState;
import org.jgl3.DepthState;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Log;
import org.jgl3.OctTree;
import org.jgl3.Renderer;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public final class Node implements Serializable {

    private static final long serialVersionUID = 1234567L;

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
    private boolean castsShadow = true;
    private boolean receivesShadow = true;
    private boolean isLight = false;
    private float lightRadius = 300;
    private float lightSampleRadius = 32;
    private int lightSampleCount = 32;
    private DepthState depthState = DepthState.READWRITE;
    private BlendState blendState = BlendState.OPAQUE;
    private CullState cullState = CullState.BACK;
    private boolean followEye = false;
    private transient Texture texture = null;
    private transient Texture texture2 = null;
    private transient Renderable renderable = null;
    private final Vector4f lightColor = new Vector4f(1, 1, 1, 1);
    private final Vector4f ambientColor = new Vector4f(0, 0, 0, 1);
    private final Vector4f diffuseColor = new Vector4f(1, 1, 1, 1);
    private final Vector4f color = new Vector4f(1, 1, 1, 1);
    private final Vector4f layerColor = new Vector4f(0, 0, 0, 0);
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
    private final Vector<Float> vertices = new Vector<>();
    private final Vector<Integer> indices = new Vector<>();
    private final Vector<int[]> faces = new Vector<>();
    private transient OctTree octTree = null;
    private int minTrisPerTree = 16;
    private final BoundingBox meshBounds = new BoundingBox();
    private transient float[] baseVertices = null;

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

    public float getLightSampleRadius() {
        return lightSampleRadius;
    }

    public Node setLightSampleRadius(float radius) {
        lightSampleRadius = radius;
        return this;
    }

    public int getLightSampleCount() {
        return lightSampleCount;
    }

    public Node setLightSampleCount(int count) {
        lightSampleCount = count;
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
        this.texture = texture;
        return this;
    }

    public Texture getTexture2() {
        return texture2;
    }

    public Node setTexture2(Texture texture2) {
        this.texture2 = texture2;
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

    public Vector4f getLayerColor() {
        return layerColor;
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

    public Node detachFromParent() {
        if(parent != null) {
            parent.children.remove(this);
            parent = null;
        }
        return this;
    }

    public Node addChild(Node child) {
        child.detachFromParent().parent = this;
        children.add(child);
        return this;
    }

    public Node detachAllChildren() {
        while(!children.isEmpty()) {
            children.get(0).detachFromParent();
        }
        return this;
    }

    public Node calcBoundsAndTransform(Camera camera) {
        if(followEye) {
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
            count = getIndexCount() / 3;
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
        if(hasMesh() && collidable && !dynamic && octTree == null) {
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

    public boolean hasMesh() {
        return getVertexCount() != 0 && getIndexCount() != 0;
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
        meshBounds.add(x, y, z);

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

    public void addBox(int sx, int sy, int sz) {
        sx /= 2;
        sy /= 2;
        sz /= 2;
        
        push(-sx, -sy, -sz, 0, 0, 0, 0, +0, +0, -1, 1, 1, 1, 1);
        push(-sx, +sy, -sz, 0, 0, 0, 0, +0, +0, -1, 1, 1, 1, 1);
        push(+sx, +sy, -sz, 0, 0, 0, 0, +0, +0, -1, 1, 1, 1, 1);
        push(+sx, -sy, -sz, 0, 0, 0, 0, +0, +0, -1, 1, 1, 1, 1);
        push(0, 1, 2, 3);

        push(-sx, -sy, +sz, 0, 0, 0, 0, +0, +0, +1, 1, 1, 1, 1);
        push(-sx, +sy, +sz, 0, 0, 0, 0, +0, +0, +1, 1, 1, 1, 1);
        push(+sx, +sy, +sz, 0, 0, 0, 0, +0, +0, +1, 1, 1, 1, 1);
        push(+sx, -sy, +sz, 0, 0, 0, 0, +0, +0, +1, 1, 1, 1, 1);
        push(7, 6, 5, 4);

        push(-sx, -sy, -sz, 0, 0, 0, 0, -1, +0, +0, 1, 1, 1, 1);
        push(-sx, -sy, +sz, 0, 0, 0, 0, -1, +0, +0, 1, 1, 1, 1);
        push(-sx, +sy, +sz, 0, 0, 0, 0, -1, +0, +0, 1, 1, 1, 1);
        push(-sx, +sy, -sz, 0, 0, 0, 0, -1, +0, +0, 1, 1, 1, 1);
        push(8, 9, 10, 11);

        push(+sx, -sy, -sz, 0, 0, 0, 0, +1, +0, +0, 1, 1, 1, 1);
        push(+sx, -sy, +sz, 0, 0, 0, 0, +1, +0, +0, 1, 1, 1, 1);
        push(+sx, +sy, +sz, 0, 0, 0, 0, +1, +0, +0, 1, 1, 1, 1);
        push(+sx, +sy, -sz, 0, 0, 0, 0, +1, +0, +0, 1, 1, 1, 1);
        push(15, 14, 13, 12);

        push(-sx, -sy, -sz, 0, 0, 0, 0, +0, -1, +0, 1, 1, 1, 1);
        push(+sx, -sy, -sz, 0, 0, 0, 0, +0, -1, +0, 1, 1, 1, 1);
        push(+sx, -sy, +sz, 0, 0, 0, 0, +0, -1, +0, 1, 1, 1, 1);
        push(-sx, -sy, +sz, 0, 0, 0, 0, +0, -1, +0, 1, 1, 1, 1);
        push(16, 17, 18, 19);

        push(-sx, +sy, -sz, 0, 0, 0, 0, +0, +1, +0, 1, 1, 1, 1);
        push(+sx, +sy, -sz, 0, 0, 0, 0, +0, +1, +0, 1, 1, 1, 1);
        push(+sx, +sy, +sz, 0, 0, 0, 0, +0, +1, +0, 1, 1, 1, 1);
        push(-sx, +sy, +sz, 0, 0, 0, 0, +0, +1, +0, 1, 1, 1, 1);
        push(23, 22, 21, 20);
    }

    public Node calcTextureCoordinates(float x, float y, float z, float units) {
        for(int i = 0; i != getVertexCount(); i++) {
            float nx = getVertexComponent(i, 7);
            float ny = getVertexComponent(i, 8);
            float nz = getVertexComponent(i, 9);
            float px = getVertexComponent(i, 0) + x;
            float py = getVertexComponent(i, 1) + y;
            float pz = getVertexComponent(i, 2) + z;

            nx = Math.abs(nx);
            ny = Math.abs(ny);
            nz = Math.abs(nz);

            if(nx >= ny && nx >= nz) {
                setVertexComponent(i, 3, pz / units);
                setVertexComponent(i, 4, py / units);
            } else if(ny >= nx && ny >= nz) {
                setVertexComponent(i, 3, px / units);
                setVertexComponent(i, 4, pz / units);
            } else {
                setVertexComponent(i, 3, px / units);
                setVertexComponent(i, 4, py / units);
            }
        }
        return this;
    }

    public Node swapWinding() {
        for(int i = 0; i != getIndexCount(); i += 3) {
            int temp = indices.get(i + 1);

            indices.set(i + 1, indices.get(i + 2));
            indices.set(i + 2, temp);
        }
        for(int[] face : faces) {
            int n = face.length / 2;

            for(int i = 0; i != n; i++) {
                int temp = face[i];

                face[i] = face[face.length - i - 1];
                face[face.length - i - 1] = temp;
            }
        }
        for(int i = 0; i != vertices.size(); i += Renderer.COMPONENTS) {
            vertices.set(i + 7, -vertices.get(i + 7));
            vertices.set(i + 8, -vertices.get(i + 8));
            vertices.set(i + 9, -vertices.get(i + 9));
        }
        return this;
    }

    public Node calcBaseVertices() {
        baseVertices = new float[getVertexCount() * 3];

        for(int i = 0, j = 0; i != getVertexCount(); i++) {
            baseVertices[j++] = getVertexComponent(i, 0);
            baseVertices[j++] = getVertexComponent(i, 1);
            baseVertices[j++] = getVertexComponent(i, 2);
        }
        return this;
    }

    public Node resetBaseVertices() {
        if(baseVertices != null) {
            for(int i = 0, j = 0; i != getVertexCount(); i++) {
                setVertexComponent(i, 0, baseVertices[j++]);
                setVertexComponent(i, 1, baseVertices[j++]);
                setVertexComponent(i, 2, baseVertices[j++]);
            }
        }
        return this;
    }

    public Node warp() {
        if(baseVertices != null) {
            Game game = Game.getInstance();
            float t = game.getTotalTime();

            for(int i = 0, j = 0; i != getVertexCount(); i++) {
                float x = baseVertices[j++];
                float y = baseVertices[j++];
                float z = baseVertices[j++];

                setVertexComponent(i, 0, x + (float)(8 * Math.sin(0.05 * z + t) * Math.cos(0.05 * y + t)));
                setVertexComponent(i, 1, y + (float)(8 * Math.cos(0.05 * x + t) * Math.sin(0.05 * z + t)));
                setVertexComponent(i, 2, z + (float)(8 * Math.sin(0.05 * x + t) * Math.cos(0.05 * y + t)));
            }
        }
        return this;
    }

    public Node renderMesh() throws Exception {
        if(hasMesh()) {
            Renderer renderer = Game.getInstance().getRenderer();

            renderer.beginTriangles();
            for(int i = 0; i != indices.size(); i++) {
                int j = indices.get(i) * Renderer.COMPONENTS;
                
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
        return this;
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

        String t1 = "@";
        String t2 = "@";
        String rd = "@";
        KeyFrameMesh mesh = null;

        if(texture != null) {
            if(texture.getFile() != null) {
                t1 = texture.getFile().getPath();
            } 
        } 
        if(texture2 != null) {
            if(texture2.getFile() != null) {
                t2 = texture2.getFile().getPath();
            }
        }
        if(renderable != null) {
            if(renderable.getFile() != null) {
                rd = renderable.getFile().getPath();
                if(renderable instanceof KeyFrameMesh) {
                    mesh = (KeyFrameMesh)renderable;
                }
            }
        }
        out.writeObject(t1);
        out.writeObject(t2);
        out.writeObject(rd);

        if(mesh != null) {
            out.writeObject(mesh.getStart());
            out.writeObject(mesh.getEnd());
            out.writeObject(mesh.getSpeed());
            out.writeObject(mesh.isLooping());
        } else {
            out.writeObject(0);
            out.writeObject(0);
            out.writeObject(0);
            out.writeObject(false);
        }
    }

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();

        String t1 = (String)in.readObject();
        String t2 = (String)in.readObject();
        String rd = (String)in.readObject();
        int start = (Integer)in.readObject();
        int end = (Integer)in.readObject();
        int speed = (Integer)in.readObject();
        boolean looping = (Boolean)in.readObject();

        if(!t1.equals("@")) {
            try {
                texture = Game.getInstance().getAssets().load(IO.file(t1));
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if(!t2.equals("@")) {
            try {
                texture2 = Game.getInstance().getAssets().load(IO.file(t2));
                texture2.toLinear(true);
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
        if(!rd.equals("@")) {
            try {
                renderable = Game.getInstance().getAssets().load(IO.file(rd));
                renderable = renderable.newInstance();
                if(renderable instanceof KeyFrameMesh) {
                    KeyFrameMesh mesh = (KeyFrameMesh)renderable;

                    mesh.setSequence(start, end, speed, looping);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return name;
    }
}
