package org.jgl3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public final class VertexArray extends Resource {
    
    private final VertexAttributes attributes;
    private final int vao;
    private final VertexBuffer vbo;
    private final VertexElementBuffer veo;

    public VertexArray(boolean createElementArrayBuffer, int ... components) {
        attributes = new VertexAttributes(components);
        vao = GL30.glGenVertexArrays();
        vbo = new VertexBuffer();
        if(createElementArrayBuffer) {
            veo = new VertexElementBuffer();
        } else {
            veo = null;
        }
    }

    public VertexAttributes getVertexAttributes() {
        return attributes;
    }

    public VertexBuffer getVBO() {
        return vbo;
    }

    public VertexElementBuffer getVEO() {
        return veo;
    }

    public void begin() throws Exception {
        begin(attributes);
    }

    public void begin(VertexAttributes pipelineAttributes) throws Exception {
        GL30.glBindVertexArray(vao);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo.getVBO());
        if(veo != null) {
            GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, veo.getVEO());
        }
        attributes.bind(pipelineAttributes);
    }

    public void draw(int start, int count) throws Exception {
        if(veo != null) {
            throw new Exception("VertexArray.draw(start, count) - call invalid when element buffer exists!");
        }
        GL11.glDrawArrays(GL11.GL_TRIANGLES, start, count);
    }

    public void draw(int count) throws Exception {
        if(veo == null) {
            throw new Exception("VertexArray.draw(count) - call invalid when element buffer does not exist!");
        }
        GL11.glDrawElements(GL11.GL_TRIANGLES, count, GL11.GL_UNSIGNED_INT, 0);
    }

    public void end() {
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    @Override
    public void destroy() throws Exception {
        GL30.glDeleteVertexArrays(vao);
        vbo.destroy();
        if(veo != null) {
            veo.destroy();
        }
        super.destroy();
    }
}
