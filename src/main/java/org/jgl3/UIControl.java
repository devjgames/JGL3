package org.jgl3;

import org.joml.Intersectionf;

class UIControl {
    
    private final UIWindow window;
    private int x = 0;
    private int y = 0;
    private int w = 0;
    private int h = 0;

    public UIControl(UIWindow window) {
        this.window = window;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return w;
    }

    public void setWidth(int w) {
        this.w = w;
    }
    
    public int getHeight() {
        return h;
    }

    public void setHeight(int h) {
        this.h = h;
    }

    public UIWindow getWindow() {
        return window;
    }

    public UIManager getManager() {
        return window.getManager();
    }

    public void onButtonDown(int button, int x, int y) {
    }

    public void onMouseMove(int x, int y) {
    }

    public void onButtonUp(int button, int x, int y) {
    }

    public void onCharDown(char c) {

    }

    public void onKeyDown(int key) {
    }

    public void onUpdate() {
    }

    public void pushRects() {
    }

    public void pushText() {
    }

    public void pushImages() throws Exception {
    }

    public void deactivate() {
    }

    public boolean deactivateOnMouseUp() {
        return true;
    }

    public void activate() {
    }

    public boolean hitTest(int x, int y) {
        int x1 = getX() + getWindow().getClientX();
        int y1 = getY() + getWindow().getClientY();
        int x2 = x1 + getWidth();
        int y2 = y1 + getHeight();

        if(Intersectionf.testPointAar(x, y, x1, y1, x2, y2)) {
            return true;
        }
        return false;
    }

    public void destroy() throws Exception {
    }
}
