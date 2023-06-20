package org.jgl3.demo;

import org.jgl3.Font;
import org.jgl3.GFX;
import org.jgl3.Game;
import org.jgl3.PixelFormat;
import org.jgl3.RenderTarget;
import org.jgl3.Renderer;
import org.jgl3.scene.Animator;
import org.jgl3.scene.Mesh;
import org.jgl3.scene.Node;
import org.jgl3.scene.Scene;
import org.joml.Vector3f;

public class Text extends Animator {

    private RenderTarget target = null;
    private Node targetNode = null;
    private int face = -1;

    @Override
    public void init(Scene scene, Node node) throws Exception {
        Game game = Game.getInstance();

        final float[] time = new float[1];
        final Vector3f origin = new Vector3f();
        final Vector3f direction = new Vector3f();

        target = null;
        targetNode = null;
        face = -1;
        time[0] = Float.MAX_VALUE;
        origin.set(node.getAbsolutePosition());
        direction.set(0, -1, 0);
        scene.getRoot().traverse((n) -> {
            if(n.getRenderable() instanceof Mesh) {
                Mesh mesh = (Mesh)n.getRenderable();

                if(mesh != null) {
                    int f = mesh.intersectFace(origin, direction, time);

                    if(f != -1) {
                        face = f;
                        targetNode = n;
                    }
                }
            }
            return true;
        });

        if(face != -1) {
            target = game.getAssets().manage(new RenderTarget(256, 256, PixelFormat.COLOR));
            targetNode.setTexture2(target.getTexture(0));
            targetNode.setTexture2Linear(false);
            targetNode.setClampTexture2ToEdge(true);

            float degrees = Float.parseFloat(node.getTag().trim());
            Mesh mesh = (Mesh)targetNode.getRenderable();

            mesh.setDecalTexture2Coordinates(face, degrees);
            mesh.compileMesh();
        }
        if(!(App.getDemo() instanceof Editor)) {
            node.setVisible(false);
        }
    }
    
    @Override
    public void animate(Scene scene, Node node) throws Exception {
        if(target != null) {
            Game game = Game.getInstance();
            Font font = game.getUI().getFont();
            Renderer renderer = game.getRenderer();
            int s = game.getScale();
            int w = target.getWidth();
            int h = target.getHeight();
            String text = "";

            for(int i = 1; i != getTokenCount(); i++) {
                text += getToken(i) + " ";
            }
            text = text.trim();

            target.begin();
            GFX.clear(1, 1, 1, 1);
            renderer.begin();
            renderer.initSprites(w, h);
            renderer.setWarpTime(game.getTotalTime() * 2);
            renderer.setWarpAmplitude(0, 16, 0);
            renderer.setWarp(true);
            renderer.setFont(font);
            renderer.beginTriangles();
            renderer.push(text, w / 2 - 4 * s * text.length(), h / 2 - 6 * s, 0, 0, 0, 0, 1);
            renderer.endTriangles();
            renderer.end();
            target.end();
        }
    }

    @Override
    public void detach(Scene scene, Node node) throws Exception {
        if(targetNode != null) {
            Mesh mesh = (Mesh)targetNode.getRenderable();

            if(mesh != null) {
                for(int i = 0; i != mesh.getFaceVertexCount(face); i++) {
                    mesh.setVertexComponent(mesh.getFaceVertex(face, i), 5, 0);
                    mesh.setVertexComponent(mesh.getFaceVertex(face, i), 6, 0);
                }
                mesh.compileMesh();
            }
        }
    }
}
