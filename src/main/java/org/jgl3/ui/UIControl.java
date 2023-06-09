package org.jgl3.ui;

abstract class UIControl {
    
    private final UIManager ui;
    private int x = 0;
    private int y = 0;
    private final int w;
    private final int h;

    public UIControl(UIManager ui, int w, int h) {
        this.w = w;
        this.h = h;
        this.ui = ui;
    }

    public UIManager getUI() {
        return ui;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    protected void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public void onMouseButtonDown(int x, int y) {
    }

    public void onMouseButtonUp(int x, int y) {
    }

    public void onMouseMove(int x,  int y) {
    }

    public void onKeyDown(int key) {
    }

    public void onCharDown(char c) {
    }

    public void onPushRects() {
    }

    public void onPushText() {
    }

    public void onUpdate() {
    }

    public void onPushImages() throws Exception {
    }

    public void deactive() {
    }

    public boolean deactivateOnMouseUp() {
        return true;
    }

    public boolean hitTest(int x, int y) {
        return 
        x >= getX() && x < getX() + getWidth() && 
        y >= getY() && y < getY() + getHeight();
    }
}
