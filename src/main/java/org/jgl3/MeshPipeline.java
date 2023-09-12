package org.jgl3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.joml.GeometryUtils;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

public abstract class MeshPipeline extends Resource {
    
    private final Pipeline pipeline;
    private final VertexArray vao;
    private FloatBuffer vBuf;
    private IntBuffer iBuf = BufferUtils.createIntBuffer(3);
    private Vector<int[]> faces = new Vector<>();
    private int indexCount = 0;
    private int vertexCount = 0;
    private final Vector3f normal = new Vector3f();

    public MeshPipeline(Pipeline pipeline) throws Exception {
        this.pipeline = pipeline;
        vao = new VertexArray(true, pipeline.getVertexAttributes().getComponents());
        vBuf = BufferUtils.createFloatBuffer(3 * getComponents());
    }

    public abstract int getComponents();

    protected final Pipeline getPipeline() {
        return pipeline;
    }

    public final void clearVertices() {
        vBuf.limit(vBuf.capacity());
        vBuf.position(0);

        vertexCount = 0;
    }

    public final int getVertexCount() {
        return vertexCount;
    }

    public final float getVertexComponent(int v, int c) {
        return vBuf.get(v * getComponents() + c);
    }

    public final void setVertexComponent(int v, int c, float x) {
        vBuf.put(v * getComponents() + c, x);
    }

    public final void pushVertex(float ... components) {
        vBuf = VertexBuffer.ensure(vBuf, 30000 * getComponents());
        for(int i = 0; i != getComponents(); i++) {
            vBuf.put(components[i]);
        }
        vertexCount++;
    }

    public final void bufferVertices(VertexUsage usage, boolean trim) {
        if(vBuf.position() != 0) {
            if(trim) {
                vBuf = VertexBuffer.trim(vBuf);
            }
            vBuf.flip();

            bufferVertices(usage);
        }
    }

    public final void bufferVertices(VertexUsage usage) {
        vao.getVBO().buffer(vBuf, usage);
    }

    public final void clearIndices() {
        iBuf.limit(iBuf.capacity());
        iBuf.position(0);
        
        indexCount = 0;
    }

    public final int getIndexCount() {
        return indexCount;
    }

    public final int getIndex(int i) {
        return iBuf.get(i);
    }

    public final int getFaceCount() {
        return faces.size();
    }

    public final int getFaceVertexCount(int i) {
        return faces.get(i).length;
    }

    public final int getFaceVertex(int i, int j) {
        return faces.get(i)[j];
    }

    public final void pushFace(int ... indices) {
        int tris = indices.length - 2;

        faces.add(indices.clone());

        for(int i = 0; i != tris; i++) {
            int i1 = indices[0];
            int i2 = indices[i + 1];
            int i3 = indices[i + 2];

            iBuf = VertexElementBuffer.ensure(iBuf, 30000);
            iBuf.put(i1);
            iBuf.put(i2);
            iBuf.put(i3);

            indexCount += 3;
        }
    }

    public final void bufferIndices(VertexUsage usage, boolean trim) {
        if(iBuf.position() != 0) {
            if(trim) {
                iBuf = VertexElementBuffer.trim(iBuf);
            }
            iBuf.flip();
            vao.getVEO().buffer(iBuf, usage);
        }
    }

    public final void begin() throws Exception {
        getPipeline().begin();
        vao.begin(pipeline.getVertexAttributes());
    }

    public final void render(int count) throws Exception {
        if(iBuf.limit() != 0 && iBuf.position() == 0 && count != 0 && count <= iBuf.limit()) {
            vao.draw(count);
        }
    }

    public final void end() {
        vao.end();
        getPipeline().end();
    }

    public final void calcNormals() {
        int components = getComponents();

        for(int i = 0; i != vBuf.position(); i += components) {
            vBuf.put(i + 5, 0);
            vBuf.put(i + 6, 0);
            vBuf.put(i + 7, 0);
        }
        for(int i = 0; i != iBuf.limit(); ) {
            int i1 = iBuf.get(i++);
            int i2 = iBuf.get(i++);
            int i3 = iBuf.get(i++);
            float x1 = vBuf.get(i1 * components + 0);
            float y1 = vBuf.get(i1 * components + 1);
            float z1 = vBuf.get(i1 * components + 2);
            float x2 = vBuf.get(i2 * components + 0);
            float y2 = vBuf.get(i2 * components + 1);
            float z2 = vBuf.get(i2 * components + 2);
            float x3 = vBuf.get(i3 * components + 0);
            float y3 = vBuf.get(i3 * components + 1);
            float z3 = vBuf.get(i3 * components + 2);

            GeometryUtils.normal(x1, y1, z1, x2, y2, z2, x3, y3, z3, normal);

            vBuf.put(i1 * components + 5, vBuf.get(i1 * components + 5) + normal.x);
            vBuf.put(i1 * components + 6, vBuf.get(i1 * components + 6) + normal.y);
            vBuf.put(i1 * components + 7, vBuf.get(i1 * components + 7) + normal.z);

            vBuf.put(i2 * components + 5, vBuf.get(i2 * components + 5) + normal.x);
            vBuf.put(i2 * components + 6, vBuf.get(i2 * components + 6) + normal.y);
            vBuf.put(i2 * components + 7, vBuf.get(i2 * components + 7) + normal.z);

            vBuf.put(i3 * components + 5, vBuf.get(i3 * components + 5) + normal.x);
            vBuf.put(i3 * components + 6, vBuf.get(i3 * components + 6) + normal.y);
            vBuf.put(i3 * components + 7, vBuf.get(i3 * components + 7) + normal.z);
        }
        for(int i = 0; i != vBuf.position(); i += components) {
            float x = vBuf.get(i + 5);
            float y = vBuf.get(i + 6);
            float z = vBuf.get(i + 7);
            float l = Vector3f.length(x, y, z);

            vBuf.put(i + 5, x / l);
            vBuf.put(i + 6, y / l);
            vBuf.put(i + 7, z / l);
        }
    }

    @Override
    public void destroy() throws Exception {
        vao.destroy();
        getPipeline().destroy();

        super.destroy();
    }
}
