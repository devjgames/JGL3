package org.jgl3;

import java.util.Hashtable;
import java.util.Vector;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCharCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;

public class UIManager extends Resource {
    
    private final Vector4f windowColor = new Vector4f(0.2f, 0.2f, 0.2f, 1);
    private final Vector4f textColor = new Vector4f(0.5f, 0.5f, 0.5f, 1);
    private final Vector4f textBackgroundColor = new Vector4f(0.3f, 0.3f, 0.3f, 1);
    private final Vector4f selectionColor = new Vector4f(1, 1, 1, 1);
    private final Vector4f windowActiveColor = new Vector4f(0.6f, 0.4f, 0.2f, 1);
    private final Hashtable<String, UIWindow> windows = new Hashtable<>();
    private final Vector<UIWindow> visibleWindows = new Vector<>();
    private final Vector<UIWindow> windowStack = new Vector<>();
    private int dragI = -1;
    private int dragX = 0;
    private int dragY = 0;

    public UIManager() {
        Game game = Game.getInstance();

        GLFW.glfwSetMouseButtonCallback(game.getWindow(), new GLFWMouseButtonCallbackI() {
            @Override
            public void invoke(long window, int button, int action, int mods) {
                if(action == GLFW.GLFW_PRESS) {
                    onButtonDown(button);
                } else if(action == GLFW.GLFW_RELEASE) {
                    onButtonUp(button);
                }
            }
        });

        GLFW.glfwSetCharCallback(game.getWindow(), new GLFWCharCallbackI() {

            @Override
            public void invoke(long window, int codepoint) {
                if(!windowStack.isEmpty()) {
                    UIWindow w = windowStack.lastElement();
                    UIControl active = w.getActiveControl(); 
                    if(active != null) {
                        active.onCharDown((char)codepoint);
                    }
                }
            }    
        });

        GLFW.glfwSetKeyCallback(game.getWindow(), new GLFWKeyCallbackI() {

            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if(!windowStack.isEmpty()) {
                    UIWindow w = windowStack.lastElement();
                    UIControl active = w.getActiveControl(); 
                    if(active != null) {
                        if(action == GLFW.GLFW_PRESS) {
                            active.onKeyDown(key);
                        }
                    }
                }
            } 
        });
    }

    public Vector4f getWindowColor() {
        return windowColor;
    }

    public Vector4f getTextColor() {
        return textColor;
    }

    public Vector4f getTextBackgroundColor() {
        return textBackgroundColor;
    }

    public Vector4f getSelectionColor() {
        return selectionColor;
    }

    public Vector4f getWindowActiveColor() {
        return windowActiveColor;
    }

    public void begin() {
        visibleWindows.removeAllElements();
    }

    public void beginWindow(String key, String text, int x, int y) {
        UIWindow window = windows.get(key);

        if(window == null) {
            windows.put(key, window = new UIWindow(this));
            windowStack.add(window);
            window.setXY(x, y);
        };
        window.setText(text);
        visibleWindows.add(window);
        
        window.begin();
    }

    public void moveRightOf(String key, int gap) {
        visibleWindows.lastElement().moveRightOf(key, gap);
    }

    public void addRow(int gap) {
        visibleWindows.lastElement().addRow(gap);
    }

    public boolean button(String key, int gap, String text, boolean selected) {
        UIWindow window =  visibleWindows.lastElement();
        UIButton button = (UIButton)window.getControl(key);

        if(button == null) {
            window.addControl(key, button = new UIButton(window));
        }
        button.setText(text);
        button.setSelected(selected);
        window.locateControl(key, gap);

        return button.getClicked();
    }

    public void label(String key, int gap, String text) {
        UIWindow window =  visibleWindows.lastElement();
        UILabel label = (UILabel)window.getControl(key);

        if(label == null) {
            window.addControl(key, label = new UILabel(window));
        }
        label.setText(text);
        window.locateControl(key, gap);
    }

    public Integer list(String key, int gap, Vector<Object> items, int rows, int columns, int select) {
        UIWindow window = visibleWindows.lastElement();
        UIList list = (UIList)window.getControl(key);

        if(list == null) {
            window.addControl(key, list = new UIList(window, rows, columns));
        }
        list.setItems(items);
        list.setSeletedIndex(select);
        window.locateControl(key, gap);

        return list.getChanged();
    }

    public Integer textField(String key, int gap, int value, boolean reset, int cols) {
        String result = textField(key, gap, "" + value, reset, cols);
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

    public Float textField(String key, int gap, float value, boolean reset, int cols) {
        String result = textField(key, gap, "" + value, reset, cols);
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

    public boolean textField(String key, int gap, Vector2f value, boolean reset, int cols) {
        String result = textField(key, gap, value.x + " " + value.y, reset, cols);

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

    public boolean textField(String key, int gap, Vector3f value, boolean reset, int cols) {
        String result = textField(key, gap, value.x + " " + value.y + " " + value.z, reset, cols);

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

    public boolean textField(String key, int gap, Vector4f value, boolean reset, int cols) {
        String result = textField(key, gap, value.x + " " + value.y + " " + value.z + " " + value.w, reset, cols);

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

    public String textField(String key, int gap, String text, boolean reset, int cols) {
        UIWindow window = visibleWindows.lastElement();
        UITextField textField = (UITextField)window.getControl(key);

        if(textField == null) {
            window.addControl(key, textField = new UITextField(window, cols));
        }
        if(reset) {
            textField.setText(text);
        }
        window.locateControl(key, gap);

        return textField.getChanged();
    }

    public void view(String key, int gap, int rows, int cols) {
        UIWindow window = visibleWindows.lastElement();
        UIView view = (UIView)window.getControl(key);

        if(view == null) {
            window.addControl(key, view = new UIView(window));
        }

        String s = "";

        for(int i = 0; i != cols; i++) {
            s += "-";
        }

        view.setWidth(window.getButtonWidth(s));
        view.setHeight(rows * window.getButtonHeight());

        window.locateControl(key, gap);
    }

    public int getViewMouseX(String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);
            UIView view = (UIView)window.getControl(viewKey);
            
            return view.getMouseX();
        }
        return 0;
    }

    public int getViewMouseY(String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);
            UIView view = (UIView)window.getControl(viewKey);
            
            return view.getMouseY();
        }
        return 0;
    }

    public boolean isViewButtonDown(int button, String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);
            UIView view = (UIView)window.getControl(viewKey);
            
            return view.isButtonDown(button);
        }
        return false;
    }

    public int getViewDX(String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);
            UIView view = (UIView)window.getControl(viewKey);
            
            return view.getDX();
        }
        return 0;
    }

    public int getViewDY(String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);
            UIView view = (UIView)window.getControl(viewKey);
            
            return view.getDY();
        }
        return 0;
    }

    public RenderTarget getViewRenderTarget(String windowKey, String viewKey) {
        if(windows.containsKey(windowKey)) {
            UIWindow window = windows.get(windowKey);

            return ((UIView)window.getControl(viewKey)).getRenderTarget();
        }
        return null;
    }

    public void endWindow() {
        
        visibleWindows.lastElement().end();
    }

    public String end(Size size, String activateKey) throws Exception {
        Game game = Game.getInstance();
        Font font = game.getFont();

        if(activateKey != null) {
            UIWindow window = windows.get(activateKey);

            if(visibleWindows.contains(window)) {
                windowStack.remove(window);
                windowStack.add(window);
                dragI = -1;
            }
        }

        game.getSpritePipeline().begin(size);
        for(UIWindow window : windowStack) {
            if(visibleWindows.contains(window)) {
                game.getSpritePipeline().beginSprite(font);
                window.render(window == windowStack.lastElement());
                window.renderText(window == windowStack.lastElement());
                game.getSpritePipeline().endSprite();
                window.renderImages();
            }
        }
        game.getSpritePipeline().end();

        if(!windowStack.isEmpty()) {
            UIWindow window = windowStack.lastElement();
            int x = game.getMouseX();
            int y = game.getMouseY();

            if(dragI != -1) {

                window.setUnscaledXY(x - dragX, y - dragY);
            } else {
                UIControl control = window.getActiveControl();

                if(control != null) {
                    control.onMouseMove(x, y);
                    control.onUpdate();
                }
            }
        }
        return null;
    }

    @Override
    public void destroy() throws Exception {
        for(UIWindow window : windows.values()) {
            window.destroy();
        }
        super.destroy();
    }

    private void onButtonDown(int button) {
        Game game = Game.getInstance();
        int x = game.getMouseX();
        int y = game.getMouseY();
        UIWindow window = null;
        
        dragI = -1;

        for(int i = windowStack.size() - 1; i != -1; i--) {

            window = windowStack.get(i);

            if(visibleWindows.contains(window)) {
                if(window.hitTest(x, y)) {
                    if(window == windowStack.lastElement()) {
                        if(window.hitTestControl(x, y)) {
                            window.getActiveControl().onButtonDown(button, x, y);
                            
                            return;
                        }
                    } 
                    dragI = i;
                    dragX = x - window.getX();
                    dragY = y - window.getY();

                    break;
                }
            }
        }

        if(dragI != -1) {
            
            window = windowStack.remove(dragI);
            windowStack.add(window);
            dragI = windowStack.size() - 1;
        }
    }

    private void onButtonUp(int button) {
        if(!windowStack.isEmpty()) {
            UIControl control = windowStack.lastElement().getActiveControl();
            Game game = Game.getInstance();
            int x = game.getMouseX();
            int y = game.getMouseY();

            if(control != null) {
                control.onButtonUp(button, x, y);
                if(control.deactivateOnMouseUp()) {
                    control.deactivate();
                    windowStack.lastElement().releaseActiveControl();
                }
            }
            dragI = -1;
        }
    }
}
