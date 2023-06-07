package org.jgl3;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

public final class GFX {

    public static void setDepthState(DepthState state) {
        if(state == DepthState.READWRITE) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(true);
        } else if(state == DepthState.READONLY) {
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        } else {
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDepthMask(false);
        }
    }

    public static void setBlendState(BlendState state) {
        if(state == BlendState.OPAQUE) {
            GL11.glDisable(GL11.GL_BLEND);
        } else {
            GL11.glEnable(GL11.GL_BLEND);
            if(state == BlendState.ADDITIVE) {
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            } else {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
        }
    }

    public static void setCullState(CullState state) {
        if(state == CullState.NONE) {
            GL11.glDisable(GL11.GL_CULL_FACE);
        } else {
            GL11.glEnable(GL11.GL_CULL_FACE);
            if(state == CullState.BACK) {
                GL11.glCullFace(GL11.GL_BACK);
            } else {
                GL11.glCullFace(GL11.GL_FRONT);
            }
        }
    }

    public static void clear(float r, float g, float b, float a) {
        setDepthState(DepthState.READWRITE);
        setCullState(CullState.BACK);
        setBlendState(BlendState.OPAQUE);
        GL11.glClearColor(r, g, b, a);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
    }

    private static boolean error = false;

    public static void checkError(String tag) {
        if(!error) {
            int code = GL11.glGetError();

            if(code != GL11.GL_NO_ERROR) {
                error = true;
                Log.put(0, tag + ":" + (code == GL11.GL_INVALID_ENUM));
            }
        }
    }

    private static final Matrix4f matrix = new Matrix4f();
    private static final Vector4f v = new Vector4f();

    public static Vector3f unProject(float wx, float wy, float wz, int vx, int vy, int vw, int vh, Matrix4f projection, Matrix4f view, Vector3f point) {
        matrix.identity().mul(projection).mul(view).invert();

        v.x = (wx - vx) / (float) vw * 2 - 1;
        v.y = (wy - vy) / (float) vh * 2 - 1;
        v.z = 2 * wz - 1;
        v.w = 1;
        v.mul(matrix);
        v.div(v.w);
        point.set(v.x, v.y, v.z);
        
        return point;
    }
}
