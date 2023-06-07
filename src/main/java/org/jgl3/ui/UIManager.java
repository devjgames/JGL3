package org.jgl3.ui;

import java.util.Hashtable;
import java.util.Vector;

import org.jgl3.Font;
import org.jgl3.Game;
import org.jgl3.IO;
import org.jgl3.Resource;
import org.jgl3.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;

public final class UIManager extends Resource {

    public static final Vector4f BACKGROUND = new Vector4f(0.35f, 0.35f, 0.35f, 1);
    public static final Vector4f FOREGROUND = new Vector4f(0, 0, 0, 1);
    public static final Vector4f SELECTED = new Vector4f(1, 1, 1, 1);
    
    private final Hashtable<String, UIControl> keyedControls = new Hashtable<>();
    private final Vector<UIControl> controls = new Vector<>();
    private final Font font;
    private UIControl active = null;
    private boolean down = false;
    private boolean handled = false;
    private int lx = 0;
    private int ly = 0;
    private int sx = 0;
    private int maxH = 0;
    private final int padding = 5;
    private final Game game;
    private UIView currentView = null;

    public UIManager(Font font) {
        game = Game.getInstance();

        this.font = font;

        GLFW.glfwSetKeyCallback(game.getWindow(), new GLFWKeyCallbackI() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(active != null) {
                    if(action == GLFW.GLFW_PRESS) {
                        active.onKeyDown(key);
                    }
                }
            } 
        });

        GLFW.glfwSetCharCallback(game.getWindow(), new GLFWCharCallbackI() {

            @Override
            public void invoke(long window, int codepoint) {
                if(active != null) {
                    active.onCharDown((char)codepoint);
                }
            }    
        });
    }

    public UIManager() throws Exception {
        this(
            new Font(
                IO.file("assets/font.png"), 
                8, 12, 100, 10, 3, Game.getInstance().getScale() 
                )
            );
    }

    int getPadding() {
        return game.getScale() * padding;
    }

    Game getGame() {
        return game;
    }

    public Font getFont() {
        return font;
    }

    void pushRect(int x, int y, int w, int h, Vector4f color) {
        game.getRenderer().push(
            font.getWhiteX(), font.getWhiteY(), 1, 1, x, y, w, h, 
            color.x, color.y, color.z, color.w, false
            );
    }

    void pushText(String text, int x, int y, Vector4f color) {
        game.getRenderer().push(text, x, y, 0, color.x, color.y, color.z, color.w);
    }

    public void begin() {
        controls.clear();
        moveTo(5 * game.getScale(), 5 * game.getScale());
        currentView = null;
    }

    public void moveTo(int x, int y) {
        lx = sx = x;
        ly = y;
        maxH = 0;
    }

    public void moveRightOf(String key, int gap) {
        if(keyedControls.containsKey(key)) {
            UIControl control = keyedControls.get(key);

            lx = sx = control.getX() + control.getWidth() + gap * game.getScale();
            ly = control.getY();
            maxH = 0;
        }
    }

    public void addRow(int gap) {
        lx = sx;
        ly += gap * game.getScale() + maxH;
        maxH = 0;
    }

    public boolean button(String key, int gap, String title, boolean selected) {
        UIButton button = (UIButton)keyedControls.get(key);

        if(button == null) {
            keyedControls.put(key, button = new UIButton(this, title));
        }
        lx += gap * game.getScale();
        button.setLocation(lx, ly);
        lx += button.getWidth();

        button.setSelected(selected);

        maxH = Math.max(button.getHeight(), maxH);

        controls.add(button);
        
        return button.getClicked();
    }

    public Integer textField(String key, int gap, String title, int value, boolean reset, int cols) {
        String result = textField(key, gap, title, "" + value, reset, cols);
        Integer x = null;

        if(result != null) {
            result = result.trim();
            try {
                x = Integer.parseInt(result);
            } catch(NumberFormatException ex) {
            }
        }
        return x;
    }

    public Float textField(String key, int gap, String title, float value, boolean reset, int cols) {
        String result = textField(key, gap, title, "" + value, reset, cols);
        Float x = null;

        if(result != null) {
            result = result.trim();
            try {
                x = Float.parseFloat(result);
            } catch(NumberFormatException ex) {
            }
        }
        return x;
    }

    public boolean textField(String key, int gap, String title, Vector2f value, boolean reset, int cols) {
        String result = textField(key, gap, title, value.x + " " + value.y, reset, cols);

        if(result != null) {
            result = result.trim();

            String[] tokens = result.split("\\s+");

            if(tokens.length == 2) {
                try {
                    float x = Float.parseFloat(tokens[0]);
                    float y = Float.parseFloat(tokens[1]);

                    value.set(x, y);

                    return true;
                } catch(NumberFormatException ex) {
                }
            }
        }
        return false;
    }

    public boolean textField(String key, int gap, String title, Vector3f value, boolean reset, int cols) {
        String result = textField(key, gap, title, value.x + " " + value.y + " " + value.z, reset, cols);

        if(result != null) {
            result = result.trim();

            String[] tokens = result.split("\\s+");

            if(tokens.length == 3) {
                try {
                    float x = Float.parseFloat(tokens[0]);
                    float y = Float.parseFloat(tokens[1]);
                    float z = Float.parseFloat(tokens[2]);

                    value.set(x, y, z);

                    return true;
                } catch(NumberFormatException ex) {
                }
            }
        }
        return false;
    }

    public boolean textField(String key, int gap, String title, Vector4f value, boolean reset, int cols) {
        String result = textField(key, gap, title, value.x + " " + value.y + " " + value.z + " " + value.w, reset, cols);

        if(result != null) {
            result = result.trim();

            String[] tokens = result.split("\\s+");

            if(tokens.length == 4) {
                try {
                    float x = Float.parseFloat(tokens[0]);
                    float y = Float.parseFloat(tokens[1]);
                    float z = Float.parseFloat(tokens[2]);
                    float w = Float.parseFloat(tokens[3]);

                    value.set(x, y, z, w);

                    return true;
                } catch(NumberFormatException ex) {
                }
            }
        }
        return false;
    }

    public String textField(String key, int gap, String title, String text, boolean reset, int cols) {
        UITextField textField = (UITextField)keyedControls.get(key);

        if(textField == null) {
            keyedControls.put(key, textField = new UITextField(this, title, cols));
        }
        lx += gap * game.getScale();
        textField.setLocation(lx, ly);
        lx += textField.getWidth();
        if(reset) {
            textField.setText(text);
        }
        maxH = Math.max(textField.getHeight(), maxH);

        controls.add(textField);
        
        return textField.getChanged();
    }

    public Float slider(String key, int gap, String title, float value, boolean reset, int cols) {
        UISlider slider = (UISlider)keyedControls.get(key);

        if(slider == null) {
            keyedControls.put(key, slider = new UISlider(this, title, cols));
        }
        lx += gap * game.getScale();
        slider.setLocation(lx, ly);
        lx += slider.getWidth();
        if(reset) {
            slider.setValue(value);
        }
        maxH = Math.max(slider.getHeight(), maxH);

        controls.add(slider);

        return slider.getChanged();
    }

    public Integer list(String key, int gap, Vector<String> items, int cols, int rows, int selected) {
        UIList list = (UIList)keyedControls.get(key);

        if(list == null) {
            keyedControls.put(key, list = new UIList(this, cols, rows));
        }
        lx += gap * game.getScale();
        list.setLocation(lx, ly);
        lx += list.getWidth();

        list.setItems(items);
        if(selected >= -1 && selected < items.size()) {
            list.select(selected);
        }
        maxH = Math.max(list.getHeight(), maxH);

        controls.add(list);
        
        return list.getChanged();
    }

    public void beginView(String key, int gap, Texture texture) {
        currentView = (UIView)keyedControls.get(key);

        if(currentView == null) {
            keyedControls.put(key, currentView = new UIView(this, texture.getWidth(), texture.getHeight()));
        }
        lx += gap * game.getScale();
        currentView.setLocation(lx, ly);
        currentView.setTexture(texture);
        lx += currentView.getWidth();

        maxH = Math.max(currentView.getHeight(), maxH);

        controls.add(currentView);
    }

    public boolean isViewButtonDown() {
        return currentView.getDown();
    }

    public int getViewMouseX() {
        return currentView.getMouseX();
    }

    public int getViewMouseY() {
        return currentView.getMouseY();
    }

    public void endView() {
        currentView = null;
    }

    public boolean end() throws Exception {
        int x = game.getMouseX();
        int y = game.getMouseY();

        game.getRenderer().initSprites(game.getWidth(), game.getHeight());
        game.getRenderer().setFont(font);
        game.getRenderer().beginTriangles();
        for(UIControl control : controls) {
            control.onPushRects();
            control.onPushText();
        }
        game.getRenderer().endTriangles();
        for(UIControl control : controls) {
            control.onPushImages();
        }

        if(game.isButtonDown(0)) {
            if(!down) {
                down = true;
                if(active != null) {
                    active.deactive();
                }
                active = null;
                for(UIControl control : controls) {
                    if(control.hitTest(x, y)) {
                        active = control;
                    }
                }
                if(active != null) {
                    active.onMouseButtonDown(x, y);
                    handled = true;
                }   
            }
        } else {
            if(active != null && down) {
                active.onMouseButtonUp(x, y);
                if(active.deactivateOnMouseUp()) {
                    deactive();
                }
            }
            handled = false;
            down = false;
        }
        if(active != null) {
            active.onMouseMove(x, y);
            active.onUpdate();
        }
        return handled;
    }

    private void deactive() {
        if(active != null) {
            active.deactive();
            active = null;
        }
    }

    @Override
    public void destroy() throws Exception {
        font.destroy();
        super.destroy();
    }
}
