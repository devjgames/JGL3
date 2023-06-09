package org.jgl3.ui;

import java.util.Vector;

class UIList extends UIControl {
    
    private final Vector<String> items = new Vector<>();
    private Integer changed = null;
    private final int cols;
    private final int rows;
    private int start = 0;
    private int thumbSize = 0;
    private int thumbPosition = 0;
    private int selectedIndex = -1;
    private boolean drag = false;

    public UIList(UIManager ui, int cols, int rows) {
        super(
            ui, 
            cols * ui.getFont().getCharWidth() * ui.getFont().getScale() + 
            ui.getGame().getScale() * 4 + 8 * ui.getGame().getScale(), 
            rows * ui.getFont().getCharHeight() * ui.getFont().getScale() + 
            ui.getGame().getScale() * 6
        );

        this.cols = cols;
        this.rows = rows;
    }

    public Integer getChanged() {
        Integer c = changed;

        changed = null;

        return c;
    }

    public void select(int index) {
        if(index >= -1 && index < items.size()) {
            selectedIndex = index;
            changed = null;
        }
    }

    public void setItems(Vector<String> items) {
        boolean update = items.size() != this.items.size();

        if(!update) {
            for(int i = 0; i != items.size(); i++) {
                if(!items.get(i).equals(this.items.get(i))) {
                    update = true;
                    break;
                }
            }
        }
        if(update) {
            this.items.removeAllElements();
            this.items.addAll(items);
            start = 0;
            selectedIndex = -1;
            changed = null;
            calcThumb();
        }
    }

    @Override
    public void onPushRects() {
        int p = getUI().getGame().getScale();

        getUI().pushRect(getX(), getY(), getWidth(), getHeight(), UIManager.FOREGROUND);
        getUI().pushRect(
            getX() + p, getY() + p, getWidth() - p * 2, getHeight() - p * 2, UIManager.BACKGROUND
            );
        if(thumbSize > 0) {
            getUI().pushRect(
                getX() + (getWidth() - 8 * p), getY() + thumbPosition + p, 8 * p, thumbSize, 
                UIManager.FOREGROUND
                );
        }
    }

    @Override
    public void onPushText() {
        int p = getUI().getGame().getScale();
        int ch = getUI().getFont().getCharHeight() * getUI().getFont().getScale();

        for(int i = 0; i != rows; i++) {
            int j = start + i;
            if(j >= items.size()) {
                break;
            }
            String item = items.get(j);

            if(item.length() > cols) {
                item = item.substring(0, cols);
            }
            if(j == selectedIndex) {
                getUI().pushText(
                    item, 
                    getX() + p * 3, 
                    getY() + p * 3 + i * ch + p, 
                    UIManager.SELECTED
                    );
            } else {
                getUI().pushText(
                    item, 
                    getX() + p * 3, 
                    getY() + p * 3 + i * ch + p, 
                    UIManager.FOREGROUND
                    );
            }
        }
    }

    @Override
    public void onMouseButtonDown(int x, int y) {
        int p = getUI().getGame().getScale();
        int ch = getUI().getFont().getCharHeight() * getUI().getFont().getScale();

        if(
            x >= getX() + getWidth() - 8 * p &&  
            x <= getX() + getWidth() &&
            y >= getY() + p &&  
            y <= getY() + getHeight() - p && 
            thumbSize > 0) {
            drag = true;
        } else {
            for(int i = 0; i != rows; i++) {
                int j = start + i;
                if(j >= items.size()) {
                    break;
                }
                if(
                    x >= getX() + p && 
                    x <= getX() + getWidth() - 8 * p && 
                    y >= getY() + p * 3 + i * ch && 
                    y <= getY() + p * 3 + i * ch + ch + p) {
                    if(selectedIndex != j) {
                        selectedIndex = j;
                        changed = j;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onMouseButtonUp(int x, int y) {
        drag = false;
    }

    @Override
    public void onMouseMove(int x, int y) {
        if(drag) {
            int p = getUI().getGame().getScale();

            thumbPosition = y - getY();
            thumbPosition = Math.max(0, thumbPosition);
            thumbPosition = Math.min(getHeight() - thumbSize - p, thumbPosition);
            start = (int)(thumbPosition / (float)(getHeight() - thumbSize - p) * (items.size() - rows));
            start = Math.max(0, start);
            start = Math.min(items.size() - rows, start);
        }
    }

    private void calcThumb() {
        if(items.size() > rows) {
            int p = getUI().getGame().getScale();

            thumbSize = (int)((rows / (float)items.size()) * getHeight());
            thumbSize = Math.max(8 * p, thumbSize);
            thumbPosition = (int)(start / (float)items.size() - rows) * (getHeight() + thumbSize + p);
            thumbPosition = Math.max(0, thumbPosition);
            thumbPosition = Math.min(getHeight() - thumbSize - p, thumbPosition);
        } else {
            thumbSize = 0;
            thumbPosition = 0;
        }
    }
}
