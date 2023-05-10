package org.jgl3.ui;

import org.jgl3.Game;
import org.jgl3.Renderer;
import org.jgl3.Texture;

class UIView extends UIControl {
    
    private boolean down = false;
    private Texture texture = null;

    public UIView(UIManager manager, int width, int height) {
        super(
            manager, 
            width + 2 * Game.getInstance().getScale(), 
            height + 2 * Game.getInstance().getScale()
            );
    }

    public void setTexture(Texture texture) {
        this.texture = texture;
    }

    public boolean getDown() {
        return down;
    }

    public int getMouseX() {
        return Game.getInstance().getMouseX() - getX() - getManager().getGame().getScale();
    }

    public int getMouseY() {
        return Game.getInstance().getMouseY() - getY() - getManager().getGame().getScale();
    }

    @Override
    public void onPushRects() {
        getManager().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
    }

    @Override
    public void onPushImages() throws Exception {
        Renderer renderer = getManager().getGame().getRenderer();
        int s = getManager().getGame().getScale();

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
