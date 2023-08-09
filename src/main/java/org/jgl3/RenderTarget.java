package org.jgl3;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public final class RenderTarget extends Resource implements Size {

    private final Texture[] textures;
    private final int framebuffer, renderbuffer;
    private final int[] viewport = new int[4];

    public RenderTarget(int width, int height, PixelFormat ... pixelFormats) throws Exception {
        int[] drawBuffers = new int[pixelFormats.length];

        textures = new Texture[pixelFormats.length];
        for(int i = 0; i != pixelFormats.length; i++) {
            textures[i] = new Texture(width, height, pixelFormats[i]);
            drawBuffers[i] = GL30.GL_COLOR_ATTACHMENT0 + i;
        }

        framebuffer = GL30.glGenFramebuffers();
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);

        renderbuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, renderbuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH32F_STENCIL8, width, height);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, renderbuffer);

        for(int i = 0; i != textures.length; i++) {
            GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0 + i, GL11.GL_TEXTURE_2D, textures[i].getTexture(), 0);
        }
        GL30.glDrawBuffers(drawBuffers);

        int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);

        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);

        if(status != GL30.GL_FRAMEBUFFER_COMPLETE) {
            throw new Exception("Failed to create RenderTarget!");
        }
    }

    @Override
    public float getAspectRatio() {
        return getWidth() / (float)getHeight();
    }

    public int getTextureCount() {
        return textures.length;
    }

    public Texture getTexture(int i) {
        return textures[i];
    }

    @Override
    public int getWidth() {
        return textures[0].getWidth();
    }

    @Override
    public int getHeight() {
        return textures[0].getHeight();
    }

    public void begin() {
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, framebuffer);
        GL11.glViewport(0, 0, getWidth(), getHeight());
    }

    public void end() {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL11.glViewport(viewport[0], viewport[1], viewport[2], viewport[3]);
    }
    
    @Override
    public void destroy() throws Exception {
        for(Texture texture : textures) {
            texture.destroy();
        }
        GL30.glDeleteFramebuffers(framebuffer);
        GL30.glDeleteRenderbuffers(renderbuffer);
        super.destroy();
    }
}
