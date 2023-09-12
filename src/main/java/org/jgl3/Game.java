package org.jgl3;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

public final class Game implements Size {

    private static Game instance = null;

    public static Game getInstance() {
        return instance;
    }
    
    private final long window;
    private final ResourceManager resources = new ResourceManager();
    private final AssetManager assets;
    private final SoundEngine soundEngine;
    private final SpritePipeline spritePipeline;
    private final Font font;
    private final double[] x = new double[1];
    private final double[] y = new double[1];
    private final int[] w = new int[1];
    private final int[] h = new int[1];
    private float lastTime = 0;
    private float elapsedTime = 0;
    private float totalTime = 0;
    private float seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private int lx = 0;
    private int ly = 0;
    private int dx = 0;
    private int dy = 0;
    private boolean syncEnabled = true;
    private GLFWVidMode videoMode;
    private long primaryMonitor;
    private int wx, wy, ww, wh;
    private boolean fpsMouseEnabled = false;
    private boolean fullScreen = false;

    public Game(int width, int height, boolean resizable) throws Exception {
        if(instance == null) {
            instance = this;
        }

        if(!GLFW.glfwInit()) {
            throw new Exception("Failed to initialize GLFW!");
        }

        GLFW.glfwSetErrorCallback((e, d) -> { Log.put(0, "GLFW:ERROR:" + e); });

        GLFW.glfwWindowHint(GLFW.GLFW_RED_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_GREEN_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_BLUE_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_ALPHA_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_DEPTH_BITS, 24);
        GLFW.glfwWindowHint(GLFW.GLFW_STENCIL_BITS, 8);
        GLFW.glfwWindowHint(GLFW.GLFW_DOUBLEBUFFER, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, (resizable) ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 2);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        window = GLFW.glfwCreateWindow(width, height, "JGL3", 0, 0);
        if(window == 0) {
            GLFW.glfwTerminate();
            throw new Exception("Failed to create GLFW window!");
        }
        GLFW.glfwMakeContextCurrent(window);
        GL.createCapabilities();

        primaryMonitor = GLFW.glfwGetPrimaryMonitor();
        videoMode =  GLFW.glfwGetVideoMode(primaryMonitor);

        assets = resources.manage(new AssetManager());
        soundEngine = resources.manage(new SoundEngine());
        spritePipeline = resources.manage(new SpritePipeline());
        font = resources.manage(new Font(Game.class, "/font.png", 8, 12, 100, 10, 3, getScale()));

        GLFW.glfwSwapInterval(1);

        resetTimer();
    }

    public long getWindow() {
        return window;
    }

    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(window, title);
    }

    public ResourceManager getResources() {
        return resources;
    }

    public AssetManager getAssets() {
        return assets;
    }

    public SoundEngine getSoundEngine() {
        return soundEngine;
    }

    public SpritePipeline getSpritePipeline() {
        return spritePipeline;
    }

    public Font getFont() {
        return font;
    }

    @Override
    public int getWidth() {
        GLFW.glfwGetFramebufferSize(window, w, h);
        return w[0];
    }

    @Override
    public int getHeight() {
        GLFW.glfwGetFramebufferSize(window, w, h);
        return h[0];
    }

    @Override
    public float getAspectRatio() {
        return getWidth() / (float)getHeight();
    }

    public int getScale() {
        GLFW.glfwGetWindowSize(window, w, h);

        int x = w[0];
        
        return getWidth() / x;
    }

    public int getMouseX() {
        GLFW.glfwGetCursorPos(window, x, y);
        return (int)x[0] * getScale();
    }

    public int getMouseY() {
        GLFW.glfwGetCursorPos(window, x, y);
        return (int)y[0] * getScale();
    }

    public int getDX() {
        return dx;
    }

    public int getDY() {
        return dy;
    }

    public boolean isButtonDown(int button) {
        return GLFW.glfwGetMouseButton(window, button) == GLFW.GLFW_PRESS;
    }

    public boolean isKeyDown(int key) {
        return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
    }

    public void enableFPSMouse() {
        if(!fpsMouseEnabled) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            GLFW.glfwSetCursorPos(window, getWidth() / 2, getHeight() / 2);
            GLFW.glfwPollEvents();
            lx = getMouseX();
            ly = getMouseY();
            fpsMouseEnabled = true;
        }
    }

    public void disableFPSMouse() {
        if(fpsMouseEnabled) {
            GLFW.glfwSetInputMode(window, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            fpsMouseEnabled = false;
        }
    }

    public boolean isSyncEnabled() {
        return syncEnabled;
    }

    public void toggleSync() {
        syncEnabled = !syncEnabled;
        if(syncEnabled) {
            GLFW.glfwSwapInterval(1);
        } else {
            GLFW.glfwSwapInterval(0);
        }
    }

    public boolean isFullscreen() {
        return fullScreen;
    }

    public void toggleFullscreen() {
        fullScreen = !fullScreen;
        if(isFullscreen()) {
            GLFW.glfwGetWindowPos(window, w, h);
            wx = w[0];
            wy = h[0];
            GLFW.glfwGetWindowSize(window, w, h);
            ww = w[0];
            wh = h[0];
            GLFW.glfwSetWindowMonitor(window, primaryMonitor, 0, 0, videoMode.width(), videoMode.height(), videoMode.refreshRate());
        } else {
            GLFW.glfwSetWindowMonitor(window, 0, wx, wy, ww, wh, 0);
        }   
    }

    public float getElapsedTime() {
        return elapsedTime;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public int getFrameRate() {
        return fps;
    }

    public void resetTimer() {
        lastTime = System.nanoTime() / 1000000000.0f;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    public boolean run() {
        GLFW.glfwPollEvents();
        if(!GLFW.glfwWindowShouldClose(window)) {
            GL11.glViewport(0, 0, getWidth(), getHeight());
            return true;
        }
        return false;
    }

    public void nextFrame() throws Exception {
        GL11.glFlush();
        GFX.checkError("Game.nextFrame()");
        GLFW.glfwSwapBuffers(window);

        float now = System.nanoTime() / 1000000000.0f;

        elapsedTime = now - lastTime;
        lastTime = now;
        totalTime += elapsedTime;
        seconds += elapsedTime;
        frames++;
        if(seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }

        int x = getMouseX();
        int y = getMouseY();

        dx = x - lx;
        dy = y - ly;

        if(fpsMouseEnabled) {
            GLFW.glfwSetCursorPos(window, getWidth() / 2, getHeight() / 2);
            x = getMouseX();
            y = getMouseY();
        }

        lx = x;
        ly = y;
    }

    public void destroy() throws Exception {
        Log.put(1, Resource.getInstances() + " instance(s)");
        resources.destroy();
        Log.put(1, Resource.getInstances() + " instance(s)");

        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }
}
