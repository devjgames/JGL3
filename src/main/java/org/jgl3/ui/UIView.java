package org.jgl3.ui;

import org.jgl3.Game;
import org.jgl3.Renderer;
import org.jgl3.Texture;

class UIView extends UIControl {
    
    private boolean down = false;
    private Texture texture = null;

    public UIView(UIManager ui) {
        super(ui, 0, 0);
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public boolean getDown() {
        return down;
    }

    public int getMouseX() {
        return Game.getInstance().getMouseX() - getX() - getUI().getGame().getScale();
    }

    public int getMouseY() {
        return Game.getInstance().getMouseY() - getY() - getUI().getGame().getScale();
    }

    @Override
    public void onPushRects() {
        getUI().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
    }

    @Override
    public void onPushImages() throws Exception {
        Renderer renderer = getUI().getGame().getRenderer();
        int s = getUI().getGame().getScale();

        renderer.setTexture(texture);
        renderer.beginTriangles();
        renderer.push(
            0, 0, texture.getWidth(), texture.getHeight(),
            getX() + s, getY() + s, texture.getWidth(), texture.getHeight(),
            1, 1, 1, 1, true
        );
        renderer.endTriangles();
    }

    @Override
    public void onMouseButtonDown(int x, int y) {
        if(hitTest(x, y)) {
            down = true;
        }
    }

    @Override
    public void onMouseButtonUp(int x, int y) {
        down = false;
    }

    @Override
    public boolean deactivateOnMouseUp() {
        return false;
    }
}
