package org.jgl3.ui;

import org.jgl3.Game;

class UISlider extends UIControl {

    private final String title;
    private final int cols;
    private Float changed = null;
    private float value = 0;
    private boolean drag = false;
    
    public UISlider(UIManager manager, String title, int cols) {
        super(
            manager,
            manager.getPadding() * 3 + cols * manager.getFont().getCharWidth() * manager.getFont().getScale() + 
            title.length() * manager.getFont().getCharWidth() * manager.getFont().getScale(),
            2 * manager.getPadding() + manager.getFont().getCharHeight() * manager.getFont().getScale()
        );

        this.title = title;
        this.cols = cols;
    }

    public void setValue(float value) {
        this.value = Math.max(0, Math.min(1, value));
    }

    public Float getChanged() {
        Float c = changed;

        changed = null;

        return c;
    }

    private int getThumbX() {
        return getX() + getManager().getPadding() + (int)(value * (getSliderLength() - getThumbSize()));
    }

    private int getThumbSize() {
        return getManager().getFont().getCharHeight() * getManager().getFont().getScale();
    }

    private int getThumbY() {
        return getY() + getManager().getPadding();
    }

    private int getSliderY() {
        int ch = getManager().getFont().getCharHeight() * getManager().getFont().getScale();

        return getThumbY() + ch / 2;
    }

    private int getSliderLength() {
        return cols * getManager().getFont().getCharWidth() * getManager().getFont().getScale() - Game.getInstance().getScale() * 2;
    }

    @Override
    public void onPushRects() {
        int x = getThumbX();
        int y = getThumbY();
        int l = getSliderLength();
        int s = getThumbSize();
        int p = getManager().getPadding();
        int ps = Game.getInstance().getScale();

        getManager().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
        getManager().pushRect(getX() + ps, getY() + ps, getWidth() - ps * 2, getHeight() - ps * 2, UIManager.BACKGROUND);
        getManager().pushRect(getX() + p, getSliderY(), l, ps, UIManager.FOREGROUND);
        getManager().pushRect(x, y, s, s, UIManager.FOREGROUND);
    }

    @Override
    public void onPushText() {
        int x = getX() + Game.getInstance().getScale();
        int y = getY() + Game.getInstance().getScale();
        int p = getManager().getPadding();
        int l = getSliderLength();

        getManager().pushText(title, x + p + l + p, y + p, UIManager.SELECTED);
    }

    @Override
    public void onMouseButtonDown(int x, int y) {
        int tx = getThumbX();
        int ty = getThumbY();
        int ts = getThumbSize();

        if(x >= tx && x <= tx + ts && y >= ty && y <= ty + ts) {
            drag = true;
            setValue(x);
        }
    }

    @Override
    public void onMouseMove(int x, int y) {
        if(drag) {
            setValue(x);
        }
    }

    @Override
    public void onMouseButtonUp(int x, int y) {
        drag = false;
    }

    private void setValue(int x) {
        int s = getX() + Game.getInstance().getScale();

        value = Math.max(0, Math.min(1, (x - s) / (float)(getSliderLength() - getThumbSize())));
        changed = value;
    }

    @Override
    public void deactive() {
        changed = null;
    }
}
