package org.jgl3.scene;

import java.io.File;

import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Texture;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class ArgumentReader {
    
    private final String[] tokens;
    private int position = 0;

    public ArgumentReader(String[] tokens) {
        this.tokens = tokens;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        if(position >= 0 && position < tokens.length - 2) {
            this.position = position;
        }
    }

    public String readString() {
        return tokens[2 + position++];
    }

    public boolean readBoolean() {
        return Boolean.parseBoolean(readString());
    }

    public int readInteger() {
        return Integer.parseInt(readString());
    }

    public float readFloat() {
        return Float.parseFloat(readString());
    }

    public void readVector2f(Vector2f v) {
        v.x = readFloat();
        v.y = readFloat();
    }

    public void readVector3f(Vector3f v) {
        v.x = readFloat();
        v.y = readFloat();
        v.z = readFloat();
    }

    public void readVector4f(Vector4f v) {
        v.x = readFloat();
        v.y = readFloat();
        v.z = readFloat();
        v.w = readFloat();
    }

    public void readRotation(Matrix4f m) {
        Vector3f r = new Vector3f();
        Vector3f u = new Vector3f(0, 1, 0);
        Vector3f f = new Vector3f();

        f.x = readFloat();
        f.z = readFloat();
        u.cross(f.normalize(), r).normalize();
        m.set(
            r.x, u.x, f.x, 0, 
            r.y, u.y, f.y, 0, 
            r.z, u.z, f.z, 0, 
            0, 0, 0, 1
            );
    }

    public Texture readTexture() throws Exception {
        File file = readFile();

        if(file != null) {
            return Game.getInstance().getAssets().load(file);
        }
        return null;
    }

    public Renderable readRenderable() throws Exception {
        File file = readFile();

        if(file != null) {
            Renderable renderable = Game.getInstance().getAssets().load(file);

            return renderable.newInstance();
        }
        return null;
    }

    public File readFile() {
        String path = readString();

        if(!path.equals("@")) {
            return IO.file(path);
        }
        return null;
    }
}
