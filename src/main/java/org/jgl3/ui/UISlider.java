package org.jgl3.ui;

import org.jgl3.Game;

class UISlider extends UIControl {

    private final String title;
    private final int cols;
    private Float changed = null;
    private float value = 0;
    private boolean drag = false;
    
    public UISlider(UIManager ui, String title, int cols) {
        super(
            ui,
            ui.getPadding() * 3 + cols * ui.getFont().getCharWidth() * ui.getFont().getScale() + 
            title.length() * ui.getFont().getCharWidth() * ui.getFont().getScale(),
            2 * ui.getPadding() + ui.getFont().getCharHeight() * ui.getFont().getScale()
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
        return getX() + getUI().getPadding() + (int)(value * (getSliderLength() - getThumbSize()));
    }

    private int getThumbSize() {
        return getUI().getFont().getCharHeight() * getUI().getFont().getScale();
    }

    private int getThumbY() {
        return getY() + getUI().getPadding();
    }

    private int getSliderY() {
        int ch = getUI().getFont().getCharHeight() * getUI().getFont().getScale();

        return getThumbY() + ch / 2;
    }

    private int getSliderLength() {
        return cols * getUI().getFont().getCharWidth() * getUI().getFont().getScale() - Game.getInstance().getScale() * 2;
    }

    @Override
    public void onPushRects() {
        int x = getThumbX();
        int y = getThumbY();
        int l = getSliderLength();
        int s = getThumbSize();
        int p = getUI().getPadding();
        int ps = Game.getInstance().getScale();

        getUI().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
        getUI().pushRect(getX() + ps, getY() + ps, getWidth() - ps * 2, getHeight() - ps * 2, UIManager.BACKGROUND);
        getUI().pushRect(getX() + p, getSliderY(), l, ps, UIManager.FOREGROUND);
        getUI().pushRect(x, y, s, s, UIManager.FOREGROUND);
    }

    @Override
    public void onPushText() {
        int x = getX() + Game.getInstance().getScale();
        int y = getY() + Game.getInstance().getScale();
        int p = getUI().getPadding();
        int l = getSliderLength();

        getUI().pushText(title, x + p + l + p, y + p, UIManager.SELECTED);
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
