package org.jgl3;

import org.joml.Intersectionf;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

class UITextField extends UIControl {

    private String text = "";
    private final int cols;
    private int start = 0;
    private int cursor = 0;
    private String changed = null;
    private float seconds = 0;
    private boolean drawCursor =  false;
    
    public UITextField(UIWindow window, int cols) {
        super(window);

        this.cols = cols;

        String s = "";

        for(int i = 0; i != cols; i++) {
            s += "-";
        }
        setHeight(window.getButtonHeight());
        setWidth(window.getButtonWidth(s) + window.getBorder() * 2 + window.getPadding() * 2);
    }

    public void setText(String text) {
        this.text = getWindow().fixText(text, cols);
        start = 0;
        cursor = 0;
    }

    public String getChanged() {
        String c = changed;

        changed = null;

        return c;
    }

    @Override
    public void pushRects() {
        int x = getX();
        int y = getY();
        int b = getWindow().getBorder();
        int p = getWindow().getPadding();
        Vector4f c = getManager().getTextBackgroundColor();
        Vector4f t = getManager().getTextColor();
        int cw = getWindow().getCharWidth();
        int ch = getWindow().getCharHeight();
        
        getWindow().rect(x, y, getWidth(), getHeight(), c);
        if(drawCursor) {
            x += b + p + cursor * cw;
            y += b + p;
            getWindow().rect(x, y, cw, ch, t);
        }
    }

    @Override
    public void pushText() {
        int b = getWindow().getBorder();
        int p = getWindow().getPadding();
        int x = getX() + b + p;
        int y = getY() + b + p;
        int cw = getWindow().getCharWidth();
        String s = text.substring(start, Math.min(text.length(), start + cols));
        Vector4f c = getManager().getTextBackgroundColor();
        Vector4f t = getManager().getTextColor();

        getWindow().text(s, x, y, t, cols);
        if(drawCursor) {
            int i = start + cursor;
            if(i < text.length()) {
                x += cursor * cw;
                getWindow().text("" + text.charAt(i), x, y, c, cols);
            }
        }
    }

    @Override
    public void onButtonDown(int button, int x, int y) {
        if(button == 0) {
            int cw = getWindow().getCharWidth();
            int ch = getWindow().getCharHeight();
            int b = getWindow().getBorder();
            int p = getWindow().getPadding();
            int x1 = getX() + getWindow().getClientX() + b + p;
            int y1 = getY() + getWindow().getClientY() + b + p;
            int x2 = x1 + cw * cols;
            int y2 = y1 + ch;

            if(Intersectionf.testPointAar(x, y, x1, y1, x2, y2)) {
                int i = start + (x - getX() - getWindow().getClientX() - b - p) / cw;
                if(i < 0) {
                    i = 0;
                } else if(i > text.length()) {
                    i = text.length();
                }
                cursor = Math.min(cols - 1, i - start);
                seconds = 0;
                drawCursor = true;
            }
        }
    }

    @Override
    public void onKeyDown(int key) {
        if(key == GLFW.GLFW_KEY_BACKSPACE) {
            if(!text.isEmpty()) {
                if(text.length() == 1) {
                    text = "";
                } else {
                    int i = start + cursor;
                    if(i == 0) {
                        text = text.substring(1);
                    } else if(i >= text.length()) {
                        text = text.substring(0, text.length() - 1);
                    } else {
                        text = text.substring(0, i - 1) + text.substring(i);
                    }
                }
                decCursor();
                changed = text;
            }
        } else if(key == GLFW.GLFW_KEY_LEFT) {
            decCursor();
        } else if(key == GLFW.GLFW_KEY_RIGHT) {
            incCursor();
        } else {
            return;
        }
        seconds = 0;
        drawCursor = true;
    }

    @Override
    public void onCharDown(char c) {
        if(c >= 32 && c < 128) {
            char ch = (char)c;
            if(text.isEmpty()) {
                text += ch;
            } else {
                int i = start + cursor;
                if(i == 0) {
                    text = ch + text;
                } else if(i >= text.length()) {
                    text += ch;
                } else {
                    text = text.substring(0, i) + ch + text.substring(i);
                }
            }
            changed = text;
            incCursor();
            seconds = 0;
            drawCursor = true;
        }
    }

    @Override
    public void onUpdate() {
        seconds += Game.getInstance().getElapsedTime();
        if(seconds >= 1) {
            drawCursor = !drawCursor;
            seconds = 0;
        }
    }

    @Override
    public void deactivate() {
         drawCursor = false;
    }

    @Override 
    public boolean deactivateOnMouseUp() {
        return false;
    }

    private void decCursor() {
        cursor--;
        if(cursor < 0) {
            start--;
            if(start < 0) {
                start = 0;
            }
            cursor = 0;
        }
    }

    private void incCursor() {
        cursor++;
        if(start + cursor > text.length()) {
            cursor--;
        } else if(cursor >= cols) {
            start++;
            cursor--;
        }
    }
}
