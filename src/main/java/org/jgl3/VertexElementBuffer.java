package org.jgl3;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL15;

public final class VertexElementBuffer extends Resource {
    
        
    private final int veo;

    public VertexElementBuffer() {
        veo = GL15.glGenBuffers();
    }

    int getVEO() {
        return veo;
    }

    public void buffer(IntBuffer buf, VertexUsage usage) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buf, usage.toGL());
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public void buffer(int[] buf, VertexUsage usage) {
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo);
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, buf, usage.toGL());
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy() throws Exception {
        GL15.glDeleteBuffers(veo);
        super.destroy();
    }

    public static IntBuffer ensure(IntBuffer iBuf, int growBy) {
        if(iBuf.position() == iBuf.capacity()) {
            Log.put(1, "increasing buffer capacity ...");

            IntBuffer nBuf = BufferUtils.createIntBuffer(iBuf.capacity() + growBy);

            iBuf.flip();
            nBuf.put(iBuf);
            iBuf = nBuf;
        }
        return iBuf;
    }

    public static IntBuffer trim(IntBuffer iBuf) {
        if(iBuf.position() < iBuf.capacity()) {
            Log.put(1, "trimming buffer capacity ...");

            IntBuffer nBuf = BufferUtils.createIntBuffer(iBuf.position());

            iBuf.flip();
            nBuf.put(iBuf);
            iBuf = nBuf;
        }
        return iBuf;
    }
}
