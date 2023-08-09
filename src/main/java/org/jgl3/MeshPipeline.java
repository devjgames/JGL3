package org.jgl3;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Vector;

import org.lwjgl.BufferUtils;

public abstract class MeshPipeline extends Resource {
    
    private final VertexArray vao;
    private FloatBuffer vBuf;
    private IntBuffer iBuf = BufferUtils.createIntBuffer(3);
    private Vector<int[]> faces = new Vector<>();
    private int indexCount = 0;
    private int vertexCount = 0;

    public MeshPipeline(int ... components) {
        vao = new VertexArray(true, components);
        vBuf = BufferUtils.createFloatBuffer(3 * getComponents());
    }

    public abstract int getComponents();

    protected final VertexArray getVAO() {
        return vao;
    }

    protected final FloatBuffer getVertexBuffer() {
        return vBuf;
    }

    protected final FloatBuffer setVertexBuffer(FloatBuffer vBuf) {
        return this.vBuf = vBuf;
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

    protected final void push() {
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
        VertexArray vao = getVAO();

        vao.getVBO().buffer(vBuf, usage);
    }

    protected final IntBuffer getIndexBuffer() {
        return iBuf;
    }

    public final void clearIndices() {
        iBuf.limit(iBuf.capacity());
        iBuf.position(0);
        
        indexCount = 0;
    }

    public final boolean isIndexed() {
        return vao.getVEO() != null;
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

    public final void push(int ... indices) {
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

    public final void render() throws Exception {
        if(iBuf.limit() != 0 && iBuf.position() == 0) {
            vao.draw(iBuf.limit());
        }
    }

    @Override
    public void destroy() throws Exception {
        vao.destroy();

        super.destroy();
    }
}
