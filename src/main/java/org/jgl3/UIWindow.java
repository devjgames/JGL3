package org.jgl3;

import java.util.Hashtable;
import java.util.Vector;

import org.joml.Intersectionf;
import org.joml.Vector4f;

class UIWindow {
    
    private final UIManager manager;
    private int x = 0;
    private int y = 0;
    private int w = 0;
    private int h = 0;
    private String text = "";
    private final int scale;
    private final int border;
    private final int padding;
    private final Font font;
    private final int cw;
    private final int ch;
    private final int wx;
    private final int wy;
    private Hashtable<String, UIControl> controls = new Hashtable<>();
    private Vector<UIControl> visibleControls = new Vector<>();
    private int sx = 0;
    private int px = 0;
    private int py = 0;
    private int mh = 0;
    private UIControl active = null;

    public UIWindow(UIManager manager) {
        Game game = Game.getInstance();

        this.manager = manager;

        scale = game.getScale();
        border = scale * 2;
        padding = scale * 4;
        font = game.getFont();
        cw = font.getCharWidth() * scale;
        ch = font.getCharHeight() * scale;
        wx = font.getWhiteX();
        wy = font.getWhiteY();

        w *= scale;
        h *= scale;
    }

    public int getPadding() {
        return padding;
    }

    public int getBorder() {
        return border;
    }

    public UIManager getManager() {
        return manager;
    }

    public UIControl getActiveControl() {
        return active;
    }

    public void releaseActiveControl() {
        active = null;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x * scale;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y * scale;
    }

    public void setXY(int x, int y) {
        setX(x);
        setY(y);
    }

    public void setUnscaledXY(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    public int getCharWidth() {
        return cw;
    }

    public int getCharHeight() {
        return ch;
    }

    public int getWidth() {
        return w;
    }

    public int getHeight() {
        return h;
    }

    public int getClientX() {
        return x + border + padding;
    }

    public int getClientY() {
        return y + border + padding * 2 + ch + padding * 2;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String fixText(String text, int maxColumns) {

        text = text.replace("\n", "");

        return text.substring(0, Math.min(maxColumns, text.length()));
    }

    public int getButtonWidth(String text) {
        return border * 2 + padding * 2 + fixText(text, text.length()).length() * cw;
    }

    public int getButtonHeight() {
        return border * 2 + padding * 2 + ch;
    }

    public void rect(int x, int y, int w, int h, float r, float g, float b) {

        SpritePipeline spritePipeline = Game.getInstance().getSpritePipeline();

        x += getClientX();
        y += getClientY();

        spritePipeline.push(wx, wy, 1, 1, x, y, w, h, r, g, b, 1, false);
    }

    public void rect(int x, int y, int w, int h, Vector4f color) {
        rect(x, y, w, h, color.x, color.y, color.z);
    }

    public void rect3D(int x, int y, int w, int h, float r, float g, float b, boolean inverted) {

        float d = 0.5f;
        float l = 1.5f;

        if(inverted) {
            float t = d;

            d = l;
            l = t;
        }

        rect(x, y, border, h, r * d, g * d, b * d);
        rect(x, y + h - border, w, border, r * d, g * d, b * d);
        rect(x, y, w, border, r * l, g * l, b * l);
        rect(x + w - border, y, border, h, r * l, g * l, b * l);
        rect(x + border, y + border, w - border * 2, h - border * 2, r, g, b);
    }

    public void rect3D(int x, int y, int w, int h, Vector4f color, boolean inverted) {

        rect3D(x, y, w, h, color.x, color.y, color.z, inverted);
    }

    public void text(String text, int x, int y, float r, float g, float b, int maxColumns) {

        SpritePipeline spritePipeline = Game.getInstance().getSpritePipeline();

        text = fixText(text, maxColumns);

        x += getClientX();
        y += getClientY();

        spritePipeline.push(text, x, y, 0, r, g, b, 1);
    }

    public void text(String text, int x, int y, Vector4f color, int maxColumns) {

        text(text, x, y, color.x, color.y, color.z, maxColumns);
    }

    public void buttonText(String text, int x, int y, float r, float g, float b, int maxColumns) {
        text(text, x + border + padding, y + border + padding, r, g, b, maxColumns);
    }

    public void buttonText(String text, int x, int y, Vector4f color, int maxColumns) {
        text(text, x + border + padding, y + border + padding, color.x, color.y, color.z, maxColumns);
    }

    public void begin() {
        visibleControls.removeAllElements();

        sx = 0;
        px = 0;
        py = 0;
        mh = 0;
    }

    public void moveRightOf(String key, int gap) {
        UIControl control = controls.get(key);

        px = control.getX() + control.getWidth() + gap * scale;
        sx = px;
        py = control.getY();
        mh = 0;
    }

    public void addRow(int gap) {
        px = sx;
        py += mh + gap * scale;
        mh = 0;
    }

    public UIControl getControl(String key) {
        return controls.get(key);
    }
    
    public void addControl(String key, UIControl control) {
        controls.put(key, control);
    }

    public void locateControl(String key, int gap) {
        UIControl control = controls.get(key);

        px += gap * scale;
        control.setX(px);
        px += control.getWidth();
        control.setY(py);

        mh = Math.max(mh, control.getHeight());

        visibleControls.add(control);
    }

    public void end() {

        w = 0;
        h = 0;

        for(UIControl control : visibleControls) {
            int cx = control.getX();
            int cy = control.getY();
            int cw = control.getWidth();
            int ch = control.getHeight();

            w = Math.max(w, cx + cw);
            h = Math.max(h, cy + ch);
        }

        if(w == 0) {
            w = 100 * scale;
        }

        w += border * 2 + padding * 2;
        h += border * 2 + padding * 2 + ch + padding * 3;

        setText(text = fixText(text, (w - border * 2 - padding * 4) / cw));
    }

    public void render(boolean active) {

        Vector4f c = (active) ? manager.getWindowActiveColor() : manager.getTextBackgroundColor();

        rect3D(-border - padding, -border - padding * 2 - ch - padding * 2, w, h, manager.getWindowColor(), false);

        rect(
            -(getClientX() - getX()) + padding + border, 
            -(getClientY() - getY()) + padding + border, 
            w - border * 2 - padding * 2, 
            ch + padding * 2, c
            );

        for(UIControl control : visibleControls) {
            control.pushRects();
        }
    }

    public void renderText(boolean active) {

        Vector4f color = (active) ? manager.getSelectionColor() : manager.getTextColor();

        text(text, padding, -(getClientY() - getY()) + padding * 2 + border, color, text.length());

        for(UIControl control : visibleControls) {
            control.pushText();
        }
    }

    public void renderImages() throws Exception {
        for(UIControl control : visibleControls) {
            control.pushImages();
        }
    }

    public boolean hitTest(int x, int y) {

        return Intersectionf.testPointAar(x, y, this.x, this.y, this.x + w, this.y + h);
    }

    public boolean hitTestControl(int x, int y) {
        UIControl a = null;
        boolean hit = false;

        for(UIControl control : visibleControls) {
            
            if(control.hitTest(x, y)) {
                hit = true;
                a = control;
                break;
            }
        }
        if(a != active) {
            if(active != null) {
                active.deactivate();
            }
            if(a != null) {
                a.activate();
            }
        }
        active = a;

        return hit;
    }

    public void destroy() throws Exception {
        for(UIControl control : controls.values()) {
            control.destroy();
        }
    }
}
