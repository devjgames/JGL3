package org.jgl3;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public final class VertexBuffer extends Resource {

    private final int vbo;

    public VertexBuffer() {
        vbo = GL15.glGenBuffers();
    }

    int getVBO() {
        return vbo;
    }

    public void buffer(FloatBuffer buf, VertexUsage usage) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, usage.toGL());
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    public void buffer(float[] buf, VertexUsage usage) {
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, usage.toGL());
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy() throws Exception {
        GL15.glDeleteBuffers(vbo);
        super.destroy();
    }

    public static FloatBuffer ensure(FloatBuffer vBuf, int growBy) {
        if(vBuf.position() == vBuf.capacity()) {
            Log.put(1, "increasing buffer capacity ...");

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vBuf.capacity() + growBy);

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        return vBuf;
    }

    public static FloatBuffer trim(FloatBuffer vBuf) {
        if(vBuf.position() < vBuf.capacity()) {
            Log.put(1, "trimming buffer capacity ...");

            FloatBuffer nBuf = BufferUtils.createFloatBuffer(vBuf.position());

            vBuf.flip();
            nBuf.put(vBuf);
            vBuf = nBuf;
        }
        return vBuf;
    }
}
