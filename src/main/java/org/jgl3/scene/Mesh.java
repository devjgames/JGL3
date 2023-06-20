package org.jgl3.scene;

import java.io.File;
import java.util.Vector;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Renderer;
import org.jgl3.Triangle;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jgl3.AssetLoader;
import org.jgl3.AssetManager;;

public class Mesh implements Renderable {

    public static void registerAssetLoader() {
        Game.getInstance().getAssets().registerAssetLoader(".obj", 1, new Loader());
    }

    private static class Loader implements AssetLoader {

        @Override
        public Object load(File file, AssetManager assets) throws Exception {
            String[] lines = new String(IO.readAllBytes(file)).split("\\n+");
            Vector<Vector3f> vList = new Vector<>();
            Vector<Vector2f> tList = new Vector<>();
            Vector<Vector3f> nList = new Vector<>();
            Mesh mesh = new Mesh(file);

            for (String line : lines) {
                String tLine = line.trim();
                String[] tokens = tLine.split("\\s+");
                if (tLine.startsWith("v ")) {
                    Vector3f v = new Vector3f(
                        Float.parseFloat(tokens[1]),
                        Float.parseFloat(tokens[2]),
                        Float.parseFloat(tokens[3])
                    );
                    vList.add(v);
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
                    int bV = mesh.getVertexCount();
                    int[] indices = new int[tokens.length - 1];

                    for (int i = 1; i != tokens.length; i++) {
                        String[] iTokens = tokens[i].split("[/]+");
                        int vI = Integer.parseInt(iTokens[0]) - 1;
                        int tI = Integer.parseInt(iTokens[1]) - 1;
                        int nI = Integer.parseInt(iTokens[2]) - 1;
                        Vector3f v = vList.get(vI);
                        Vector2f t = tList.get(tI);
                        Vector3f n = nList.get(nI);

                        mesh.push(
                            v.x, v.y, v.z,
                            t.x, t.y,
                            0, 0,
                            n.x, n.y, n.z,
                            1, 1, 1, 1
                        );

                        indices[i - 1] = bV + i - 1;
                    }
                    mesh.push(indices);
                }
            }
            mesh.calcBounds();
            mesh.compileMesh();

            return mesh;
        }
    }
    
    private final File file;
    private final Vector<Float> vertices = new Vector<>();
    private final Vector<Integer> indices = new Vector<>();
    private final Vector<int[]> faces = new Vector<>();
    private float[] vertexArray = null;
    private final BoundingBox bounds = new BoundingBox();
    private final Triangle triangle = new Triangle();
    private final Vector3f p1 = new Vector3f();
    private final Vector3f p2 = new Vector3f();
    private final Vector3f p3 = new Vector3f();
    private final Vector3f u = new Vector3f();
    private final Vector3f v = new Vector3f();
    private final Vector3f n = new Vector3f();

    public Mesh(File file) {
        this.file = file;
    }

    public int getVertexCount() {
        return vertices.size() / Renderer.COMPONENTS;
    }

    public float getVertexComponent(int i, int j) {
        return vertices.get(i * Renderer.COMPONENTS + j);
    }

    public Mesh setVertexComponent(int i, int j, float x) {
        vertices.set(i * Renderer.COMPONENTS + j, x);
        return this;
    }

    public void calcBounds() {
        bounds.clear();
        for(int i = 0; i != getVertexCount(); i++) {
            bounds.add(
                getVertexComponent(i, 0),
                getVertexComponent(i, 1),
                getVertexComponent(i, 2)
            );
        }
    }

    public Mesh push(float x, float y, float z, float s, float t, float u, float v, float nx, float ny, float nz, float r, float g, float b, float a) {
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

    public Mesh push(int ... indices) {
        int tris = indices.length - 2;

        faces.add(indices.clone());

        for(int i = 0; i != tris; i++) {
            this.indices.add(indices[0]);
            this.indices.add(indices[i + 1]);
            this.indices.add(indices[i + 2]);
        }
        return this;
    }

    public Mesh compileMesh() {
        int v = 0;

        vertexArray = new float[indices.size() * Renderer.COMPONENTS];

        for(int i : indices) {
            int j = i * Renderer.COMPONENTS;

            for(int k = 0;  k != Renderer.COMPONENTS; k++, j++, v++) {
                vertexArray[v] = vertices.get(j);
            }
        }
        return this;
    }

    public Mesh clearCompiledMesh() {
        vertexArray = null;
        return this;
    }

    public int intersectFace(Vector3f origin, Vector3f direction, float[] time) {
        int hit = -1;

        for(int i = 0; i != getFaceCount(); i++) {
            for(int j = 0; j != getFaceVertexCount(i) - 2; j++) {
                triangle.getP1().set(
                    getVertexComponent(getFaceVertex(i, 0), 0),
                    getVertexComponent(getFaceVertex(i, 0), 1),
                    getVertexComponent(getFaceVertex(i, 0), 2)
                );
                triangle.getP2().set(
                    getVertexComponent(getFaceVertex(i, j + 1), 0),
                    getVertexComponent(getFaceVertex(i, j + 1), 1),
                    getVertexComponent(getFaceVertex(i, j + 1), 2)
                );
                triangle.getP3().set(
                    getVertexComponent(getFaceVertex(i, j + 2), 0),
                    getVertexComponent(getFaceVertex(i, j + 2), 1),
                    getVertexComponent(getFaceVertex(i, j + 2), 2)
                );
                triangle.calcPlane();
                if(triangle.intersects(origin, direction, 0, time)) {
                    hit = i;
                }
            }
        }
        return hit;
    }

    public void setDecalTexture2Coordinates(int face, float degrees) {
        p1.set(
            getVertexComponent(getFaceVertex(face, 0), 0),
            getVertexComponent(getFaceVertex(face, 0), 1),
            getVertexComponent(getFaceVertex(face, 0), 2)
        );
        p2.set(
            getVertexComponent(getFaceVertex(face, 1), 0),
            getVertexComponent(getFaceVertex(face, 1), 1),
            getVertexComponent(getFaceVertex(face, 1), 2)
        );
        p3.set(
            getVertexComponent(getFaceVertex(face, 2), 0),
            getVertexComponent(getFaceVertex(face, 2), 1),
            getVertexComponent(getFaceVertex(face, 2), 2)
        );
        p2.sub(p1, u).normalize();
        p3.sub(p2, v).normalize();
        u.cross(v, n).normalize();

        float minX = Float.MAX_VALUE;
        float minY = Float.MAX_VALUE;
        float maxX = -minX;
        float maxY = -minY;

        u.cross(n, v).normalize();

        for(int i = 0; i != getFaceVertexCount(face); i++) {
            p1.set(
                getVertexComponent(getFaceVertex(face, i), 0),
                getVertexComponent(getFaceVertex(face, i), 1),
                getVertexComponent(getFaceVertex(face, i), 2)
            ); 
            float x = u.dot(p1);
            float y = v.dot(p1);

            minX = Math.min(x, minX);
            minY = Math.min(y, minY);
            maxX = Math.max(x, maxX);
            maxY = Math.max(y, maxY);
        }

        float w = maxX - minX;
        float h = maxY - minY;

        for(int i = 0; i != getFaceVertexCount(face); i++) {
            p1.set(
                getVertexComponent(getFaceVertex(face, i), 0),
                getVertexComponent(getFaceVertex(face, i), 1),
                getVertexComponent(getFaceVertex(face, i), 2)
            ); 
            float x = u.dot(p1);
            float y = v.dot(p1);

            x = (x - (maxX + minX) / 2) / w;
            y = (y - (maxY + minY) / 2) / h;

            float c = (float)Math.cos(Math.toRadians(degrees));
            float s = (float)Math.sin(Math.toRadians(degrees));
            float tx = c * x + s * y;
            float ty = -s * x + c * y;

            ty = -ty;

            tx += 0.5f;
            ty += 0.5f;

            setVertexComponent(getFaceVertex(face, i), 5, tx);
            setVertexComponent(getFaceVertex(face, i), 6, ty);
        }
    }

    @Override
    public File getFile() {
        return file;
    }

    @Override
    public BoundingBox getBounds() {
        return bounds;
    }

    @Override
    public int getTriangleCount() {
        return getIndexCount() / 3;
    }

    @Override
    public Triangle getTriangle(int i, Triangle triangle) {
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
        return triangle.calcPlane();
    }

    @Override
    public void update(Scene scene, Node node) throws Exception {
    }

    @Override
    public void render(Scene scene, Node node) throws Exception {
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
    }

    @Override
    public Renderable newInstance() throws Exception {
        Mesh mesh = new Mesh(file);

        mesh.vertices.addAll(vertices);
        mesh.indices.addAll(indices);
        mesh.faces.addAll(faces);
        if(vertexArray != null) {
            mesh.vertexArray = vertexArray.clone();
        }
        mesh.bounds.set(bounds);

        return mesh;
    }
}
