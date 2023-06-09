package org.jgl3.ui;

class UIButton extends UIControl {

    private final String title;
    private boolean selected = false;
    private boolean clicked = false;
    private boolean down = false;

    public UIButton(UIManager ui, String title) {
        super(
            ui,
            title.length() * ui.getFont().getCharWidth() * ui.getFont().getScale() + 
            ui.getPadding() * 2, 
            ui.getFont().getCharHeight() * ui.getFont().getScale() + 
            ui.getPadding() * 2
            );
        this.title = title;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getClicked() {
        boolean c = clicked;

        clicked = false;

        return c;
    }

    @Override
    public void onPushRects() {
        int p = getUI().getGame().getScale();

        getUI().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
        getUI().pushRect(
            getX() + p, getY() + p, getWidth() - p * 2, getHeight() - p * 2, UIManager.BACKGROUND
            );
    }

    public void onPushText() {
        getUI().pushText(
            title, 
            getX() + getUI().getPadding(),
            getY() + getUI().getPadding(),
            (selected) ? 
                ((down) ? UIManager.FOREGROUND : UIManager.SELECTED) : 
                ((down) ? UIManager.SELECTED : UIManager.FOREGROUND)
        );
    }

    @Override
    public void onMouseButtonDown(int x, int y) {
        if(hitTest(x, y)) {
            down = true;
        }
    }

    @Override
    public void onMouseButtonUp(int x, int y) {
        if(hitTest(x, y)) {
            clicked = true;
        }
        down = false;
    }
}
