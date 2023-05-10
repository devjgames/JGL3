package org.jgl3.ui;

class UIButton extends UIControl {

    private final String title;
    private boolean selected = false;
    private boolean clicked = false;
    private boolean down = false;

    public UIButton(UIManager manager, String title) {
        super(
            manager,
            title.length() * manager.getFont().getCharWidth() * manager.getFont().getScale() + 
            manager.getPadding() * 2, 
            manager.getFont().getCharHeight() * manager.getFont().getScale() + 
            manager.getPadding() * 2
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
        int p = getManager().getGame().getScale();

        getManager().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
        getManager().pushRect(
            getX() + p, getY() + p, getWidth() - p * 2, getHeight() - p * 2, UIManager.BACKGROUND
            );
    }

    public void onPushText() {
        getManager().pushText(
            title, 
            getX() + getManager().getPadding(),
            getY() + getManager().getPadding(),
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
