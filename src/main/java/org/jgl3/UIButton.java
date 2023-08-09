package org.jgl3;

class UIButton extends UIControl {

    private String text;
    private boolean selected = false;
    private boolean clicked = false;
    private boolean down = false;
    
    public UIButton(UIWindow window) {
        super(window);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void setText(String text) {
        text = getWindow().fixText(text, text.length());

        setWidth(getWindow().getButtonWidth(text));
        setHeight(getWindow().getButtonHeight());

        this.text = text;
    }

    public boolean getClicked() {
        boolean c = clicked;

        clicked = false;

        return c;
    }

    @Override
    public void pushRects() {
        getWindow().rect3D(
            getX(), getY(), 
            getWidth(), getHeight(), 
            getManager().getWindowColor(), 
            down
            );
    }

    @Override
    public void pushText() {
        getWindow().buttonText(
            text, 
            getX(), getY(), 
            (isSelected()) ? getManager().getSelectionColor() : getManager().getTextColor(), 
            text.length()
            );
    }

    @Override
    public void onButtonDown(int button, int x, int y) {
        if(button == 0) {
            if(hitTest(x, y)) {
                down = true;
            }
        }
    }

    @Override
    public void onButtonUp(int button, int x, int y) {
        if(down) {
            if(hitTest(x, y)) {
                clicked = true;
            }
        }
        down = false;
    }
}
