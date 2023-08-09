package org.jgl3;

class UILabel extends UIControl {

    private String text = "";
    
    public UILabel(UIWindow window) {
        super(window);
    }

    public void setText(String text) {
        text = getWindow().fixText(text, text.length());

        setWidth(getWindow().getButtonWidth(text));
        setHeight(getWindow().getButtonHeight());

        this.text = text;
    }

    @Override
    public void pushText() {
        getWindow().buttonText(
            text, 
            getX(), getY(), 
            getManager().getTextColor(), 
            text.length()
            );
    }
}
