package org.jgl3;

import java.util.Vector;

import org.joml.Vector4f;

class UIList extends UIControl {

    private final Vector<String> items = new Vector<>();
    private final int rows;
    private final int columns;
    private int start = 0;
    private int thumb = 0;
    private int selectedIndex = -1;
    private boolean drag = false;
    private Integer changed = null;
    
    public UIList(UIWindow window, int rows, int columns) {
        super(window);

        this.rows = rows;
        this.columns = columns;
    }

    public void setSeletedIndex(int i) {
        if(i == -1) {
            selectedIndex = -1;
            changed = null;
        } else if(i >= 0 && i < items.size()) {
            selectedIndex = i;
            changed = null;
        }
    }

    public Integer getChanged() {
        Integer c = changed;

        changed = null;

        return c;
    }

    public void setItems(Vector<Object> items) {
        int w = 0;
        int h = rows * getWindow().getButtonHeight();
        boolean clear = items.size() != this.items.size();

        if(clear) {
            thumb = 0;
            start = 0;
            selectedIndex = -1;
        } else {
            for(int i = 0; i != items.size(); i++) {
                String text = items.get(i).toString();

                text = getWindow().fixText(text, columns);
                if(!text.equals(this.items.get(i))) {
                    clear = true;
                }
                w = Math.max(w, getWindow().getButtonWidth(text));
            }
        }

        if(clear) {
            w = 0;

            Log.put(1, "populating UIList ...");
            this.items.clear();
            for(Object item : items) {
                String text = getWindow().fixText(item.toString(), columns);

                this.items.add(text);
                w = Math.max(w, getWindow().getButtonWidth(text));
            }
        }

        int border = getWindow().getBorder();
        int padding = getWindow().getPadding();
        int cw = getWindow().getCharWidth();

        w = Math.max(w, columns * cw + border * 2 + padding * 2);

        setWidth(w + border * 2 + cw);
        setHeight(h);
    }

    @Override
    public void pushRects() {
        Vector4f c = getManager().getTextBackgroundColor();
        int border = getWindow().getBorder();
        int h = getWindow().getButtonHeight();
        int cw = getWindow().getCharWidth();
        
        getWindow().rect(getX(), getY(), getWidth(), getHeight(), c);

        if(selectedIndex != -1) {
            int i = selectedIndex - start;

            if(i >= 0 && i < rows) {

                c = getManager().getTextColor();

                getWindow().rect(getX(), getY() + h * i, getWidth() -  border * 2 - cw, h, c);  
            }
        }

        int n = Math.max(0, items.size() - rows);

        if(n > 0) {

            c = getManager().getTextColor();

            getWindow().rect(getX() + getWidth() - border - cw, getY() + thumb, cw + border, getWindow().getButtonHeight(), c);
        }
    }

    @Override
    public void pushText() {
        int x = getX();
        int y = getY();

        for(int i = 0; i != rows; i++) {
            int j = start + i;

            if(j < items.size()) {
                Vector4f c = (j == selectedIndex) ? getManager().getWindowColor() : getManager().getTextColor();

                getWindow().buttonText(items.get(j), x, y, c, columns);
                y += getWindow().getButtonHeight();
            } else {
                break;
            }
        }
    }

    @Override
    public void onButtonDown(int button, int x, int y) {
        if(button == 0) {
            if(hitTest(x, y)) {

                int border = getWindow().getBorder();
                int cw = getWindow().getCharWidth();
                int dx = getX() + getWindow().getClientX() + getWidth() - cw - border;
                int dy = getY() + getWindow().getClientY() + getHeight();

                if(x > dx && y < dy) {
                    drag = true;
                    calcThumb(y);
                } else if(x < dx) {
                    int i = start + (y - getWindow().getClientY() - getY()) / getWindow().getButtonHeight();
                    
                    if(i >= start && i < items.size() && i != selectedIndex) {
                        selectedIndex = changed = i;
                    }
                }
            }
        }
    }

    @Override
    public void onButtonUp(int button, int x, int y) {
        drag = false;
    }

    @Override
    public void onMouseMove(int x, int y) {
        if(drag) {
            calcThumb(y);
        }
    }

    private void calcThumb(int y) {

        float p = (y - getWindow().getClientY() - getY()) / (float)(getHeight());
        int n = Math.max(0, items.size() - rows);
        int h = getHeight() - getWindow().getButtonHeight();

        start = Math.max(0, Math.min(n, (int)(n * p)));

        thumb = Math.max(0, Math.min(h, (int)(p * h)));
    }
}
