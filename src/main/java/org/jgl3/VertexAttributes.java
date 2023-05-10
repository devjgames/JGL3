package org.jgl3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public final class VertexAttributes {
    
    private final int[] components;
    private final int stride;

    public VertexAttributes(int ... components) {
        this.components = components.clone();
        
        int stride = 0;

        for(int component : components) {
            stride += component;
        }
        this.stride = stride;
    }

    public boolean canBind(VertexAttributes pipelineAttributes) {
        boolean valid = pipelineAttributes.components.length <= components.length;

        if(valid) {
            for(int i = 0; i != pipelineAttributes.components.length; i++) {
                if(pipelineAttributes.components[i] != components[i]) {
                    valid = false;
                    break;
                }
            }
        }
        return valid;
    }

    void bind(VertexAttributes pipelineAttributes) throws Exception {
        if(!canBind(pipelineAttributes)) {
            throw new Exception("Invalid pipeline vertex attributes!");
        }
        for(int i = 0; i != pipelineAttributes.components.length; i++) {
            GL20.glEnableVertexAttribArray(i);
        }
        for(int i = 0, x = 0; i != pipelineAttributes.components.length; i++) {
            GL20.glVertexAttribPointer(i, components[i], GL11.GL_FLOAT, false, stride * 4, x);
            x += components[i] * 4;
        }
    }
}
