package org.jgl3;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL30;

public final class Texture extends Resource {

    public static final class TextureLoader implements AssetLoader {
        @Override
        public Object load(File file, AssetManager assets) throws Exception {
            BufferedImage image = ImageIO.read(file);
            int w = image.getWidth();
            int h = image.getHeight();
            int[] pixels = new int[w * h];

            image.getRGB(0, 0, w, h, pixels, 0, w);
            for(int i = 0; i != pixels.length; i++) {
                int p = pixels[i];

                pixels[i] = (p & 0xFF000000) | ((p << 16) & 0xFF0000) | (p & 0xFF00) | ((p >> 16) & 0xFF);
            }
            return new Texture(file, w, h, pixels);
        }
    }
    
    private final File file;
    private final int texture;
    private final int width;
    private final int height;
    private final PixelFormat pixelFormat;

    public Texture(int width, int height, PixelFormat pixelFormat) {
        file = null;
        texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        this.width = width;
        this.height = height;
        this.pixelFormat = pixelFormat;
        if(pixelFormat == PixelFormat.COLOR) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (ByteBuffer)null);
        } else if(pixelFormat == PixelFormat.FLOAT) {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_R32F, width, height, 0, GL11.GL_RED, GL11.GL_FLOAT, (FloatBuffer)null);
        } else {
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL30.GL_RGBA32F, width, height, 0, GL11.GL_RGBA, GL11.GL_FLOAT, (FloatBuffer)null);
        }
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        toNearest(true);
    }

    protected Texture(File file, int width, int height, int[] pixels) {
        IntBuffer buf = BufferUtils.createIntBuffer(width * height);
        
        buf.put(pixels);
        buf.flip();
        this.file = file;
        texture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        this.width = width;
        this.height = height;
        this.pixelFormat = PixelFormat.COLOR;
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
        toNearest(false);
    }

    public File getFile() {
        return file;
    }

    int getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public PixelFormat getPixelFormat() {
        return pixelFormat;
    }

    public void toLinear(boolean clampToEdge) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void toNearest(boolean clampToEdge) {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, (clampToEdge) ? GL15.GL_CLAMP_TO_EDGE : GL11.GL_REPEAT);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public void readPixels(int[] pixels) {
        if(pixelFormat == PixelFormat.COLOR) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture);
            GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
            for(int i = 0; i != pixels.length; i++) {
                int p = pixels[i];

                pixels[i] = (p & 0xFF000000) | ((p << 16) & 0xFF0000) | (p & 0xFF00) | ((p >> 16) & 0xFF);
            }
        }
    }

    @Override
    public String toString() {
        return file + ", " + width + " x " + height;
    }

    @Override
    public void destroy() throws Exception {
        GL11.glDeleteTextures(texture);
        super.destroy();
    }
}
