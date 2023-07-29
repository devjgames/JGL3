package org.jgl3.scene;

import java.io.File;

import org.jgl3.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ArgumentWriter {
    
    private final StringBuilder builder = new StringBuilder(1000);

    public void write(String v) {
        builder.append(" " + v);
    }

    public void write(boolean v) {
        builder.append(" " + v);
    }

    public void write(int v) {
        builder.append(" " + v);
    }

    public void write(float v) {
        builder.append(" " + v);
    }

    public void write(Vector2f v) {
        builder.append(" " + v.x + " " + v.y);
    }

    public void write(Vector3f v) {
        builder.append(" " + v.x + " " + v.y + " " + v.z);
    }

    public void write(Vector4f v) {
        builder.append(" " + v.x + " " + v.y + " " + v.z + " " + v.w);
    }

    public void write(Matrix4f m) {
        builder.append(" " + m.m02() + " " + m.m22());
    }

    public void write(File file) {
        if(file == null) {
            builder.append( " @");
        } else {
            builder.append(" " + file.getPath());
        }
    }

    public void write(Texture texture) {
        if(texture != null) {
            write(texture.getFile());
        } else {
            write((File)null);
        }
    }

    public void write(Renderable renderable) {
        if(renderable != null) {
            write(renderable.getFile());
        } else {
            write((File)null);
        }
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
