package org.jgl3.scene;

import java.io.File;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Texture;
import org.joml.GeometryUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class NodeBuilder {
    
    private static class Vertex {
        public final int index;
        public int edge = -1;
        public final Vector3f position = new Vector3f();
        public final Vector3f normal = new Vector3f();

        public Vertex(int index) {
            this.index = index;
        }

        public Vertex(Vertex vertex) {
            index = vertex.index;
            edge = vertex.edge;
            position.set(vertex.position);
            normal.set(vertex.normal);
        }
    }

    private static class Edge {
        public final int index;
        public int prev = -1;
        public int next = -1;
        public int pair = -1;
        public int vertex = -1;
        public int face = -1;
        public final Vector2f textureCoordinate = new Vector2f();
        public final Vector3f normal = new Vector3f();

        public Edge(int index) {
            this.index = index;
        }

        public Edge(Edge edge) {
            index = edge.index;
            prev = edge.prev;
            next = edge.next;
            pair = edge.pair;
            vertex = edge.vertex;
            face = edge.face;
            textureCoordinate.set(edge.textureCoordinate);
            normal.set(edge.normal);
        }
    }

    private static class Face {
        public final int index;
        public int edge = -1;
        public final Vector3f normal = new Vector3f();
        public Texture texture = null;

        public Face(int index) {
            this.index = index;
        }

        public Face(Face face) {
            index = face.index;
            edge = face.edge;
            normal.set(face.normal);
            texture = face.texture;
        }
    }

    private final Vector<Vertex> vertices = new Vector<>();
    private final Vector<Edge> edges = new Vector<>();
    private final Vector<Face> faces = new Vector<>();
    private final Hashtable<String, Edge> vertexEdges = new Hashtable<>();

    public NodeBuilder() {
    }

    public NodeBuilder(NodeBuilder builder) {
        set(builder);
    }

    public final int getVertexCount() {
        return vertices.size();
    }

    public final int getVertexEdge(int i) {
        return vertices.get(i).edge;
    }

    public final Vector3f getVertexPosition(int i) {
        return vertices.get(i).position;
    }

    public final Vector3f getVertexNormal(int i) {
        return vertices.get(i).normal;
    }

    public final int addVertex(float x, float y, float z) {
        Vertex v = new Vertex(vertices.size());

        v.position.set(x, y, z);
        vertices.add(v);

        return v.index;
    }

    public final int addVertex(Vector3f position) {
        return addVertex(position.x, position.y, position.z);
    }

    public final int getEdgeCount() {
        return edges.size();
    }

    public final int getEdgePrev(int i) {
        return edges.get(i).prev;
    }

    public final int getEdgeNext(int i) {
        return edges.get(i).next;
    }

    public final int getEdgePair(int i) {
        return edges.get(i).pair;
    }

    public final int getEdgeVertex(int i) {
        return edges.get(i).vertex;
    }

    public final int getEdgeFace(int i) {
        return edges.get(i).face;
    }

    public final Vector2f getEdgeTextureCoordinate(int i) {
        return edges.get(i).textureCoordinate;
    }

    public final Vector3f getEdgeNormal(int i) {
        return edges.get(i).normal;
    }

    public final String keyFor(int v1, int v2) {
        if(v1 > v2) {
            int temp = v1;

            v1 = v2;
            v2 = temp;
        }
        return v1 + ":" + v2;
    }

    public final int getFaceCount() {
        return faces.size();
    }

    public final int getFaceEdge(int i) {
        return faces.get(i).edge;
    }

    public final int getFaceEdgeCount(int i) {
        int e1 = getFaceEdge(i);
        int e2 = e1;
        int count = 0;

        do {
            count++;
            e1 = getEdgeNext(e1);
        } while(e1 != e2);

        return count;
    }

    public final Vector3f getFaceNormal(int i) {
        return faces.get(i).normal;
    }

    public final Texture getFaceTexture(int i) {
        return faces.get(i).texture;
    }

    public final void setFaceTexture(int i, Texture texture) {
        faces.get(i).texture = texture;
    }

    public final int addFace(int ... indices) throws Exception {
        return addFace(null, indices);
    }

    public final int addFace(Texture texture, int ... indices) throws Exception {
        return addFace(false, texture, indices);
    }

    public final int addFace(boolean flip, Texture texture, int ... indices) throws Exception {
        if(indices.length < 3) {
            throw new Exception("NodeBuilder.addFace() index count < 3!");
        }

        if(flip) {
            int[] temp = new int[indices.length];

            for(int i = 0; i != indices.length; i++) {
                temp[i] = indices[indices.length - i - 1];
            }
            indices = temp;
        }

        Face face = new Face(faces.size());

        face.texture = texture;
        face.edge = edges.size();
        faces.add(face);

        for(int i = 0; i != indices.length; i++) {
            int index = indices[i];

            if(index < 0 || index >= vertices.size()) {
                throw new Exception("NodeBuilder.addFace() index out of bounds!");
            }

            Vertex vertex = vertices.get(index);
            
            edges.add(new Edge(edges.size()));
            edges.lastElement().vertex = vertex.index;
            edges.lastElement().face = face.index;

            if(vertex.edge == -1) {
                vertex.edge = edges.lastElement().index;
            }

            if(i != 0) {
                Edge prev = edges.get(face.edge + i - 1);

                prev.next = edges.lastElement().index;
                edges.lastElement().prev = prev.index;
            }
            if(i == indices.length - 1) {
                Edge next = edges.get(face.edge);

                next.prev = edges.lastElement().index;
                edges.lastElement().next = next.index;
            }
        }

        for(int i = 0; i != indices.length; i++) {
            int v1 = indices[i];
            int v2 = indices[(i + 1) % indices.length];
            String key = keyFor(v1, v2);
            Edge edge1 = edges.get(face.edge + i);

            if(vertexEdges.containsKey(key)) {
                Edge edge2 = vertexEdges.get(key);

                if(edge2.pair != -1) {
                    throw new Exception("NodeBuilder.addFace() edge already has a pair!");
                }
                edge2.pair = edge1.index;
                edge1.pair = edge2.index;
            } else {
                vertexEdges.put(key, edge1);
            }
        }
        return face.index;
    }

    public final void clear() {
        vertices.removeAllElements();
        edges.removeAllElements();
        faces.removeAllElements();
        vertexEdges.clear();;
    }

    public final void set(NodeBuilder builder) {
        Enumeration<String> keys = builder.vertexEdges.keys();

        clear();

        for(Vertex v : builder.vertices) {
            vertices.add(new Vertex(v));
        }
        for(Edge e : builder.edges) {
            edges.add(new Edge(e));
        }
        for(Face f : builder.faces) {
            faces.add(new Face(f));
        }
        while(keys.hasMoreElements()) {
            String key = keys.nextElement();
            Edge e = builder.vertexEdges.get(key);

            vertexEdges.put(key, edges.get(e.index));
        }
    }

    public final void calcNormals(boolean smooth) {
        for(int i = 0; i != getFaceCount(); i++) {
            int e1 = getFaceEdge(i);
            int e2 = getEdgeNext(e1);
            int e3 = getEdgeNext(e2);
            Vector3f p1 = getVertexPosition(getEdgeVertex(e1));
            Vector3f p2 = getVertexPosition(getEdgeVertex(e2));
            Vector3f p3 = getVertexPosition(getEdgeVertex(e3));

            GeometryUtils.normal(p1, p2, p3, getFaceNormal(i));
        }

        for(int i = 0; i != getVertexCount(); i++) {
            int e1 = getVertexEdge(i);

            if(e1 != -1) {
                int e2 = e1;
                Vector3f n = getVertexNormal(i);

                n.zero();
                do {
                    n.add(getFaceNormal(getEdgeFace(e1)));
                    if(getEdgePair(e1) == -1) {
                        break;
                    }
                    e1 = getEdgePair(e1);
                    e1 = getEdgeNext(e1);
                } while(e1 != e2);
                n.normalize();
            }
        }

        for(int i = 0; i != getEdgeCount(); i++) {
            if(smooth) {
                getEdgeNormal(i).set(getVertexNormal(getEdgeVertex(i)));
            } else {
                getEdgeNormal(i).set(getFaceNormal(getEdgeFace(i)));
            }
        }
    }

    public final void subdivide(int times) throws Exception {
        for(int t = 0; t < times; t++) {
            NodeBuilder builder = new NodeBuilder();
            Hashtable<String, Integer> midPoints = new Hashtable<>();

            for(int i = 0; i != getVertexCount(); i++) {
                builder.addVertex(getVertexPosition(i));
            }

            for(int i = 0; i != getFaceCount(); i++) {
                int e1 = getFaceEdge(i);
                int e2 = e1;
                int n = getFaceEdgeCount(i);
                int[] cnr = new int[n];
                int[] mps = new int[n];
                Vector3f center = new Vector3f();
                Vector2f[] tc = new Vector2f[n];
                Vector2f[] tm = new Vector2f[n];
                Vector2f ctc = new Vector2f();
                int j = 0;

                do {
                    int v1 = getEdgeVertex(e1);
                    int v2 = getEdgeVertex(getEdgeNext(e1));
                    Vector3f p1 = getVertexPosition(v1);
                    Vector2f t1 = getEdgeTextureCoordinate(e1);
                    Vector2f t2 = getEdgeTextureCoordinate(getEdgeNext(e1));
                    String key = keyFor(v1, v2);

                    if(!midPoints.containsKey(key)) {
                        Vector3f p2 = getVertexPosition(v2);
                        Vector3f mp = p2.add(p1, new Vector3f()).div(2);

                        midPoints.put(key, builder.addVertex(mp));
                    }
                    cnr[j] = v1;
                    mps[j] = midPoints.get(key);
                    tc[j] = t1;
                    tm[j] = t2.add(t1, new Vector2f()).div(2);
                    j++;

                    center.add(p1);
                    ctc.add(t1);

                    e1 = getEdgeNext(e1);
                } while(e1 != e2);

                center.div(n);
                ctc.div(n);

                int cp = builder.addVertex(center);

                for(j = 0; j != n; j++) {
                    if(j == 0) {
                        int f = builder.addFace(getFaceTexture(i), cnr[0], mps[0], cp, mps[n - 1]);
                        int e = builder.getFaceEdge(f);

                        builder.getEdgeTextureCoordinate(e + 0).set(tc[0]);
                        builder.getEdgeTextureCoordinate(e + 1).set(tm[0]);
                        builder.getEdgeTextureCoordinate(e + 2).set(ctc);
                        builder.getEdgeTextureCoordinate(e + 3).set(tm[n - 1]);
                    } else if(j == n - 1) {
                        int f = builder.addFace(getFaceTexture(i), mps[j - 1], cnr[j], mps[j], cp);
                        int e = builder.getFaceEdge(f);

                        builder.getEdgeTextureCoordinate(e + 0).set(tm[j - 1]);
                        builder.getEdgeTextureCoordinate(e + 1).set(tc[j]);
                        builder.getEdgeTextureCoordinate(e + 2).set(tm[j]);
                        builder.getEdgeTextureCoordinate(e + 3).set(ctc);
                    } else {
                        int f = builder.addFace(getFaceTexture(i), cp, mps[j - 1], cnr[j], mps[j]);
                        int e = builder.getFaceEdge(f);
                        
                        builder.getEdgeTextureCoordinate(e + 0).set(ctc);
                        builder.getEdgeTextureCoordinate(e + 1).set(tm[j - 1]);
                        builder.getEdgeTextureCoordinate(e + 2).set(tc[j]);
                        builder.getEdgeTextureCoordinate(e + 3).set(tm[j]);
                    }
                }
            }
            set(builder);
        }
    }

    public final void smooth(int times) {
        for(int t = 0; t < times; t++) {
            Vector<Vector3f> positions = new Vector<>(getVertexCount());
            Vector<Integer> degrees = new Vector<>(getVertexCount());

            for(int i = 0; i != getVertexCount(); i++) {
                int e1 = getVertexEdge(i);
                int e2 = e1;
                Vector3f p = new Vector3f();
                int degree = 0;

                if(e1 != -1) {
                    do {
                        int e = e1;
                        Vector3f center = new Vector3f();
                        int count = 0;

                        do { 
                            center.add(getVertexPosition(getEdgeVertex(e)));
                            count++;
                            e = getEdgeNext(e);
                        } while(e != e1);

                        p.add(center.div(count));

                        degree++;

                        if(getEdgePair(e1) == -1) {
                            break;
                        }
                        e1 = getEdgePair(e1);
                        e1 = getEdgeNext(e1);
                    } while(e1 != e2);
                } else {
                    degree = 1;
                }

                p.div(degree);
                positions.add(p);
                degrees.add(degree);
            }

            for(int i = 0; i != getVertexCount(); i++) {
                Vector3f p = getVertexPosition(i);
                Vector3f p2 = positions.get(i);
                float d = degrees.get(i);

                p.lerp(p2, 4.0f / d, p);
            }
        }
    }

    public final void calcTextureCoordinates(float ox, float oy, float oz, float units, int startEdge) {
        for(int i = startEdge; i != getEdgeCount(); i++) {
            Vector3f n = getFaceNormal(getEdgeFace(i));
            float x = Math.abs(n.x);
            float y = Math.abs(n.y);
            float z = Math.abs(n.z);
            Vector3f p = getVertexPosition(getEdgeVertex(i));
            Vector2f t = getEdgeTextureCoordinate(i);
            
            if(x >= y && x >= z) {
                t.set(p.z + oz, p.y + oy).div(units);
            } else if(y >= x && y >= z) {
                t.set(p.x + ox, p.z + oz).div(units);
            } else {
                t.set(p.x + ox, p.y + oy).div(units);
            }
        }
    }

    public final void rotateTextureCoordinates(int face, int times) {
        for(int i = 0; i < times; i++) {
            int e1 = getFaceEdge(face);
            int e2 = e1;
            Vector2f c = getEdgeTextureCoordinate(e1);
            float s = c.x;
            float t = c.y;

            do {
                getEdgeTextureCoordinate(e1).set(getEdgeTextureCoordinate(getEdgeNext(e1)));
                e1 = getEdgeNext(e1);
            } while(e1 != e2);
            getEdgeTextureCoordinate(getEdgePrev(e1)).set(s, t);
        }
    }

    public final void setQuadTextureCoordinates(int i, float x, float y, float w, float h, float texWidth, float texHeight) {
        getEdgeTextureCoordinate(getFaceEdge(i) + 0).set((x + 0) / (float)texWidth, (y + 0) / (float)texHeight);
        getEdgeTextureCoordinate(getFaceEdge(i) + 1).set((x + w) / (float)texWidth, (y + 0) / (float)texHeight);
        getEdgeTextureCoordinate(getFaceEdge(i) + 2).set((x + w) / (float)texWidth, (y + h) / (float)texHeight);
        getEdgeTextureCoordinate(getFaceEdge(i) + 3).set((x + 0) / (float)texWidth, (y + h) / (float)texHeight);
    }

    public final void nudge(float amount, float cutOff) {
        for(int f = 0; f != getFaceCount(); f++) {
            int e1 = getFaceEdge(f);
            int e2 = e1;

            do {
                Vector3f n1 = getFaceNormal(getEdgeFace(e1));
                int pair = getEdgePair(e1);

                if(pair != -1) {
                    Vector3f n2 = getFaceNormal(getEdgeFace(getEdgePair(e1)));

                    if(n1.dot(n2) > cutOff) {
                        Vector2f t0 = getEdgeTextureCoordinate(getEdgePrev(e1));
                        Vector2f t1 = getEdgeTextureCoordinate(e1);
                        Vector2f t2 = getEdgeTextureCoordinate(getEdgeNext(e1));
                        Vector2f t3 = getEdgeTextureCoordinate(getEdgeNext(getEdgeNext(e1)));

                        if(Math.abs(t0.x - t1.x) < 0.1) {
                            if(t1.y < 0.1) {
                                t1.y += amount;
                            } else {
                                t1.y -= amount;
                            }
                        } else if(t1.x < 0.1) {
                            t1.x += amount;
                        } else {
                            t1.x -= amount;
                        }
                        if(Math.abs(t2.x - t3.x) < 0.1) {
                            if(t2.y < 0.1) {
                                t2.y += amount;
                            } else {
                                t2.y -= amount;
                            }
                        } else if(t2.x < 0.1) {
                            t2.x += amount;
                        } else {
                            t2.x -= amount;
                        }
                    }
                }
                e1 = getEdgeNext(e1);
            } while(e1 != e2);
        }
    }

    public final void addBox(float x, float y, float z, float sx, float sy, float sz, int dx, int dy, int dz, Texture texture, boolean invert) throws Exception {
        Hashtable<String, Integer> v = new Hashtable<>();

        sx /= 2;
        sy /= 2;
        sz /= 2;

        for(int ix = 0; ix != dx + 1; ix++) {
            for(int iy = 0; iy != dy + 1; iy++) {
                for(int iz = 0; iz != dz + 1; iz++) {
                    if(ix == 0 || ix == dx || iy == 0 || iy == dy || iz == 0 || iz == dz) {
                        v.put(ix + ":" + iy + ":" + iz, addVertex(x - sx + ix / (float)dx * sx * 2, y - sy + iy / (float)dy * sy * 2, z - sz + iz / (float)dz * sz * 2));
                    }
                }
            }
        }

        for(int ix = 0; ix != dx; ix++) {
            for(int iy = 0; iy != dy; iy++) {
                for(int iz = 0; iz != dz; iz++) {
                    if(ix == 0) {
                        addFace(
                            invert,
                            texture,
                            v.get(ix + ":" + iy + ":" + iz),
                            v.get(ix + ":" + iy + ":" + (iz + 1)),
                            v.get(ix + ":" + (iy + 1) + ":" + (iz + 1)),
                            v.get(ix + ":" + (iy + 1) + ":" + iz)
                        );
                    }
                    if(ix == dx - 1) {
                        addFace(
                            invert,
                            texture,
                            v.get((ix + 1) + ":" + iy + ":" + iz),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + iz),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + (iz + 1)),
                            v.get((ix + 1) + ":" + iy + ":" + (iz + 1))
                        );
                    }
                    if(iy == 0) {
                        addFace(
                            invert,
                            texture,
                            v.get(ix + ":" + iy + ":" + iz),
                            v.get((ix + 1) + ":" + iy + ":" + iz),
                            v.get((ix + 1) + ":" + iy + ":" + (iz + 1)),
                            v.get(ix + ":" + iy + ":" + (iz + 1))
                        );
                    }
                    if(iy == dy - 1) {
                        addFace(
                            invert,
                            texture,
                            v.get(ix + ":" + (iy + 1) + ":" + iz),
                            v.get(ix + ":" + (iy + 1) + ":" + (iz + 1)),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + (iz + 1)),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + iz)
                        );
                    }
                    if(iz == 0) {
                        addFace(
                            invert,
                            texture,
                            v.get(ix + ":" + iy + ":" + iz),
                            v.get(ix + ":" + (iy + 1) + ":" + iz),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + iz),
                            v.get((ix + 1) + ":" + iy + ":" + iz)
                        );
                    }
                    if(iz == dz - 1) {
                        addFace(
                            invert,
                            texture,
                            v.get(ix + ":" + iy + ":" + (iz + 1)),
                            v.get((ix + 1) + ":" + iy + ":" + (iz + 1)),
                            v.get((ix + 1) + ":" + (iy + 1) + ":" + (iz + 1)),
                            v.get(ix + ":" + (iy + 1) + ":" + (iz + 1))
                        );
                    }
                }
            }
        }
    }
   
    public final Node build() throws Exception {
        Node root = new Node();
        Hashtable<String, Node> keyedNodes = new Hashtable<>();

        for(int i = 0; i != getFaceCount(); i++) {
            Texture texture = getFaceTexture(i);
            String key = "";
            Node node;

            if(texture != null) {
                key = texture.getFile().getPath();
            }
            if(!keyedNodes.containsKey(key)) {
                keyedNodes.put(key, node = new Node());
                if(texture != null) {
                    node.setName(IO.fileNameWithOutExtension(texture.getFile()));
                }
                node.setTexture(texture);
                root.addChild(node);
            }

            node = keyedNodes.get(key);

            int e1 = getFaceEdge(i);
            int e2 = e1;
            int[] indices = new int[getFaceEdgeCount(i)];
            int bv = node.getVertexCount();
            int j = 0;

            do {
                Vector3f p = getVertexPosition(getEdgeVertex(e1));
                Vector2f t = getEdgeTextureCoordinate(e1);
                Vector3f n = getEdgeNormal(e1);

                node.push(p.x, p.y, p.z, t.x, t.y, 0, 0, n.x, n.y, n.z, 1, 1, 1, 1);
                indices[j] = bv + j;
                j++;
                e1 = getEdgeNext(e1);
            } while(e1 != e2);

            node.push(indices);
        }
        for(int i = 0; i != root.getChildCount(); i++) {
            Node node = root.getChild(i);

            node.calcMeshBounds();
            node.compileMesh();
        }
        if(root.getChildCount() == 1) {
            root = root.getChild(0);
        }

        return root;
    }

    public static NodeBuilder load(File file) throws Exception {
        NodeBuilder builder = new NodeBuilder();
        String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
        Vector<Vector2f> tList = new Vector<>();
        Vector<Vector3f> nList = new Vector<>();
        Hashtable<String, Texture> textures = new Hashtable<>();
        Texture texture = null;

        for (String line : lines) {
            String tLine = line.trim();
            String[] tokens = tLine.split("\\s+");

            if(tLine.startsWith("mtllib ")) {
                File mFile = IO.file(file.getParentFile(), tLine.substring(6).trim());
                String[] mLines = new String(IO.readAllBytes(mFile)).split("\\n+");
                String mName = null;

                for(String mLine : mLines) {
                    String tmLine = mLine.trim();
                    
                    if(tmLine.startsWith("newmtl ")) {
                        mName = tmLine.substring(6).trim();
                    } else if(tmLine.startsWith("map_Kd ")) {
                        File tFile = IO.file(file.getParentFile(), tmLine.substring(6).trim());

                        textures.put(mName, Game.getInstance().getAssets().load(tFile));
                    }
                }
            } else if(tLine.startsWith("usemtl ")) {
                String name = tLine.substring(6).trim();

                if(textures.containsKey(name)) {
                    texture = textures.get(name);
                } else {
                    texture = null;
                }
            } else if (tLine.startsWith("v ")) {
                builder.addVertex(
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                );
            } else if (tLine.startsWith("vt ")) {
                Vector2f v = new Vector2f(
                    Float.parseFloat(tokens[1]),
                    1 - Float.parseFloat(tokens[2])
                );
                tList.add(v);
            } else if (tLine.startsWith("vn ")) {
                Vector3f v = new Vector3f(
                    Float.parseFloat(tokens[1]),
                    Float.parseFloat(tokens[2]),
                    Float.parseFloat(tokens[3])
                );
                nList.add(v);
            } else if (tLine.startsWith("f ")) {
                int[] indices = new int[tokens.length - 1];
                Vector2f[] textureCoordinates = new Vector2f[tokens.length - 1];
                Vector3f[] normals = new Vector3f[tokens.length - 1];

                for (int i = 1; i != tokens.length; i++) {
                    String[] iTokens = tokens[i].split("[/]+");
                    int vI = Integer.parseInt(iTokens[0]) - 1;
                    int tI = Integer.parseInt(iTokens[1]) - 1;
                    int nI = Integer.parseInt(iTokens[2]) - 1;
                    Vector2f t = tList.get(tI);
                    Vector3f n = nList.get(nI);

                    textureCoordinates[i - 1] = t;
                    normals[i - 1] = n;
                    indices[i - 1] = vI;
                }
                int f = builder.addFace(texture, indices);
                int e = builder.getFaceEdge(f);

                for(int i = 0; i != indices.length; i++) {
                    builder.getEdgeNormal(e + i).set(normals[i]);
                    builder.getEdgeTextureCoordinate(e + i).set(textureCoordinates[i]);
                }
                builder.setFaceTexture(f, texture);
            }
        }
        return builder;
    }
}
