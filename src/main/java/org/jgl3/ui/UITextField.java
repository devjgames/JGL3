package org.jgl3.ui;

import org.lwjgl.glfw.GLFW;

class UITextField extends UIControl {
    
    private String text = "";
    private String title = "";
    private final int cols;
    private int start = 0;
    private int cursor = 0;
    private String changed = null;
    private float seconds = 0;
    private boolean drawCursor = false;

    public UITextField(UIManager ui, String title, int cols) {
        super(
            ui,
            cols * ui.getFont().getCharWidth() * ui.getFont().getScale() +
            title.length() * ui.getFont().getCharWidth() * ui.getFont().getScale() +
            ui.getPadding() * 3,
            ui.getFont().getCharHeight() * ui.getFont().getScale() + ui.getPadding() * 2
        );

        this.cols = cols;
        this.title = title;
    }

    public void setText(String text) {
        this.text = text;
        start = 0;
        cursor = 0;
    }

    public String getChanged() {
        String c = changed;

        changed = null;

        return c;
    }

    @Override
    public void onPushRects() {
        int x = getX();
        int y = getY();
        int p = getUI().getGame().getScale();
        int cw = getUI().getFont().getCharWidth() * getUI().getFont().getScale();
        int ch = getUI().getFont().getCharHeight() * getUI().getFont().getScale();

        getUI().pushRect(x, y, getWidth(), getHeight(), UIManager.FOREGROUND);
        getUI().pushRect(x + p, y + p, getWidth() - p * 2, getHeight() - p * 2, UIManager.BACKGROUND);

        if(drawCursor) {
            x += getUI().getPadding() + cursor * cw;
            y += getUI().getPadding();
            getUI().pushRect(x, y, cw, ch, UIManager.FOREGROUND);
        }
    }

    @Override
    public void onPushText() {
        int p = getUI().getPadding();
        int x = getX() + p;
        int y = getY() + p;
        int cw = getUI().getFont().getCharWidth() * getUI().getFont().getScale();
        String t = text.substring(start, Math.min(text.length(), start + cols));

        getUI().pushText(title, x + cw * cols + p, y, UIManager.SELECTED);
        getUI().pushText(t, x, y, UIManager.FOREGROUND);
        if(drawCursor) {
            int i = start + cursor;
            if(i < text.length()) {
                x += cursor * cw;
                getUI().pushText("" + text.charAt(i), x, y, UIManager.BACKGROUND);
            }
        }
    }

    @Override
    public void onMouseButtonDown(int x, int y) {
        int cw = getUI().getFont().getCharWidth() * getUI().getFont().getScale();
        int ch = getUI().getFont().getCharHeight() * getUI().getFont().getScale();
        int x1 = getX() + getUI().getPadding();
        int y1 = getY() + getUI().getPadding();
        int x2 = x1 + cw * cols;
        int y2 = y1 + ch;

        if(x >= x1 && x <= x2 && y >= y1 && y <= y2) {
            int i = start + (x - getX() - getUI().getPadding()) / cw;
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
    public void deactive() {
         drawCursor = false;
    }

    @Override
    public boolean deactivateOnMouseUp() {
        return false;
    }

    @Override
    public void onUpdate() {
        seconds += getUI().getGame().getElapsedTime();
        if(seconds >= 1) {
            drawCursor = !drawCursor;
            seconds = 0;
        }
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
