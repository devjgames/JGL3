package org.jgl3;

class UIView extends UIControl {

    private RenderTarget renderTarget = null;
    private int mouseX = 0;
    private int mouseY = 0;
    private int dX = 0;
    private int dY = 0;
    private int lX = 0;
    private int lY = 0;
    private boolean[] buttonDown = new boolean[] { false, false, false };
    
    public UIView(UIWindow window) {
        super(window);
    }

    public int getMouseX() {
        return mouseX;
    }

    public int getMouseY() {
        return mouseY;
    }

    public int getDX() {
        return dX;
    }

    public int getDY() {
        return dY;
    }

    public boolean isButtonDown(int button) {
        if(button >= 0 && button < buttonDown.length) {
            return buttonDown[button];
        }
        return false;
    }

    public RenderTarget getRenderTarget() {
        return renderTarget;
    }

    private void createRenderTarget() throws Exception {
        int w = getWidth();
        int h = getHeight();

        if(renderTarget == null) {
            Log.put(1, "Creating view render target ...");

            renderTarget = new RenderTarget(getWidth(), getHeight(), PixelFormat.COLOR);
        } else {
            int tw = renderTarget.getWidth();
            int th = renderTarget.getHeight();

            if(w != tw || h != th) {
                Log.put(1, "Creating view render target ...");

                renderTarget.destroy();

                renderTarget = new RenderTarget(w, h, PixelFormat.COLOR);
            }
        }
    }

    @Override
    public void onButtonDown(int button, int x, int y) {
        if(hitTest(x, y)) {
            if(button >= 0 && button < buttonDown.length) {
                buttonDown[button] = true;
            }
            mouseX = getWindow().getClientX() + getX() - x;
            mouseY = getWindow().getClientY() + getY() - y;
            lX = mouseX;
            lY = mouseY;
        }
    }

    @Override
    public void onButtonUp(int button, int x, int y) {
        if(button >= 0 && button < buttonDown.length) {
            buttonDown[button] = false;
        }
        dX = 0;
        dY = 0;
    }

    @Override
    public void onMouseMove(int x, int y) {
        mouseX = getWindow().getClientX() + getX() - x;
        mouseY = getWindow().getClientY() + getY() - y;
        dX = lX - mouseX;
        dY = lY - mouseY;
        lX = mouseX;
        lY = mouseY;
    }

    @Override
    public void pushImages() throws Exception {
        Game game = Game.getInstance();
        int x = getWindow().getClientX() + getX();
        int y = getWindow().getClientY() + getY();

        createRenderTarget();

        game.getSpritePipeline().beginSprite(renderTarget.getTexture(0));
        game.getSpritePipeline().push(
            0, 0, renderTarget.getWidth(), renderTarget.getHeight(),
            x, y, renderTarget.getWidth(), renderTarget.getHeight(),
            1, 1, 1, 1,
            true
        );
        game.getSpritePipeline().endSprite();
    }

    @Override
    public boolean deactivateOnMouseUp() {
        return false;
    }

    @Override
    public void destroy() throws Exception {
        if(renderTarget != null) {
            renderTarget.destroy();
        }
        renderTarget = null;
    }
}
