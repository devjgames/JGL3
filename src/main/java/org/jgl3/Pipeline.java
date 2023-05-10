package org.jgl3;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public final class Pipeline extends Resource {
    
    private final VertexAttributes vertexAttributes;
    private final int program;
    private final FloatBuffer mBuf = BufferUtils.createFloatBuffer(16);

    public Pipeline(byte[] vertexBytes, byte[] fragmentBytes, Object ... attributes) throws Exception {
        int vs = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        int fs = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        int[] components = new int[attributes.length / 2];

        for(int i = 0, j = 0; i < attributes.length; i += 2, j++) {
            components[j] = (Integer)attributes[i + 1];
        }
        vertexAttributes = new VertexAttributes(components);

        GL20.glShaderSource(vs, new String(vertexBytes));
        GL20.glCompileShader(vs);

        GL20.glShaderSource(fs, new String(fragmentBytes));
        GL20.glCompileShader(fs);

        int status = GL20.glGetShaderi(vs, GL20.GL_COMPILE_STATUS);

        if(status == 0) {
            Log.put(0, GL20.glGetShaderInfoLog(vs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            program = 0;
            throw new Exception("Failed to compile vertex shader!");
        }

        status = GL20.glGetShaderi(fs, GL20.GL_COMPILE_STATUS);
        if(status == 0) {
            Log.put(0, GL20.glGetShaderInfoLog(fs));
            GL20.glDeleteShader(vs);
            GL20.glDeleteShader(fs);
            program = 0;
            throw new Exception("Failed to compile fragment shader!");
        }

        program = GL20.glCreateProgram();
        GL20.glAttachShader(program, vs);
        GL20.glAttachShader(program, fs);
        GL20.glDeleteShader(vs);
        GL20.glDeleteShader(fs);
        for(int i = 0, j = 0; i < attributes.length; i += 2, j++) {
            GL20.glBindAttribLocation(program, j, (String)attributes[i]);
        }
        GL20.glLinkProgram(program);
        status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);
        if(status == 0) {
            Log.put(0, GL20.glGetProgramInfoLog(program));
            GL20.glDeleteProgram(program);
            throw new Exception("Failed to link shader program!");
        }
    }

    public void setColorLocations(String ... locations) throws Exception {
        for(int i = 0; i < locations.length; i++) {
            GL30.glBindFragDataLocation(program, i, locations[i]);
        }
        GL20.glLinkProgram(program);

        int status = GL20.glGetProgrami(program, GL20.GL_LINK_STATUS);

        if(status == 0) {
            Log.put(0, GL20.glGetProgramInfoLog(program));
            throw new Exception("Failed to link shader program in pipeline.setColorLocations()!");
        }
    }

    public VertexAttributes getVertexAttributes() {
        return vertexAttributes;
    }

    public int getUniform(String name) {
        int uniform = GL20.glGetUniformLocation(program, name);

        if(uniform < 0) {
            Log.put(0, "Uniform '"+ name + "' not found");
        }
        return uniform;
    }

    public void begin() {
        GL20.glUseProgram(program);
    }

    public void set(int uniform, int value) {
        GL20.glUniform1i(uniform, value);
    }

    public void set(int uniform, boolean value) {
        set(uniform, (value) ? 1 : 0);
    }

    public void set(int uniform, float value) {
        GL20.glUniform1f(uniform, value);
    }

    public void set(int uniform, float x, float y) {
        GL20.glUniform2f(uniform, x, y);
    }

    public void set(int uniform, Vector2f value) {
        set(uniform, value.x, value.y);
    }

    public void set(int uniform, float x, float y, float z) {
        GL20.glUniform3f(uniform, x, y, z);
    }

    public void set(int uniform, Vector3f value) {
        set(uniform, value.x, value.y, value.z);
    }

    public void set(int uniform, float x, float y, float z, float w) {
        GL20.glUniform4f(uniform, x, y, z, w);
    }

    public void set(int uniform, Vector4f value) {
        set(uniform, value.x, value.y, value.z, value.w);
    }

    public void set(int uniform, Matrix4f value) {
        value.get(mBuf);
        GL20.glUniformMatrix4fv(uniform, false, mBuf);
        mBuf.limit(mBuf.capacity());
        mBuf.position(0);
    }

    public void set(int uniform, int unit, Texture value) {
        GL15.glActiveTexture(GL15.GL_TEXTURE0 + unit);
        set(uniform, unit);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, value.getTexture());
    }

    public void end() {
        GL20.glUseProgram(0);
    }

    @Override
    public void destroy() throws Exception {
        GL20.glDeleteProgram(program);
        super.destroy();
    }
}
