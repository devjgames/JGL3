package org.jgl3;

public class State {
    
    private DepthState depthState = DepthState.READWRITE;
    private BlendState blendState = BlendState.OPAQUE;
    private CullState cullState = CullState.BACK;

    public DepthState getDepthState() {
        return depthState;
    }

    public void setDepthState(DepthState state) {
        depthState = state;
    }

    public BlendState getBlendState() {
        return blendState;
    }

    public void setBlendState(BlendState state) {
        blendState = state;
    }

    public CullState getCullState() {
        return cullState;
    }

    public void setCullState(CullState state) {
        cullState = state;
    }

    public void bind(State lastState) {
        if(lastState == null) {
            GFX.setDepthState(depthState);
            GFX.setBlendState(blendState);
            GFX.setCullState(cullState);
        } else {
            if(depthState != lastState.depthState) {
                GFX.setDepthState(depthState);
            }
            if(blendState != lastState.blendState) {
                GFX.setBlendState(blendState);
            }
            if(cullState != lastState.cullState) {
                GFX.setCullState(cullState);
            }
        }
    }
}
