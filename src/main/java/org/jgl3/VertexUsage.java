package org.jgl3;

import org.lwjgl.opengl.GL15;

public enum VertexUsage {
    STATIC,
    DYNAMIC,
    STREAM;

    int toGL() {
        if(this == STATIC) {
            return GL15.GL_STATIC_DRAW;
        } else if(this == DYNAMIC) {
            return GL15.GL_DYNAMIC_DRAW;
        } else {
            return GL15.GL_STREAM_DRAW;
        }
    }
}
