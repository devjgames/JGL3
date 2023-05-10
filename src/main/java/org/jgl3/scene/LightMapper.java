package org.jgl3.scene;

import java.util.*;

import javax.imageio.ImageIO;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.Log;
import org.jgl3.OctTree;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.*;
import java.awt.image.*;

public class LightMapper {

    private Vector<Node> renderables = new Vector<>();
    private Vector<Triangle> triangles = new Vector<>();
    private Vector<Node> lights = new Vector<>();
    private Triangle triangle = new Triangle();

    public void light(File file, Scene scene, boolean deleteLightMap) throws Exception {
        Game game = Game.getInstance();
        int[] xy = new int[] { 0, 0 };
        int[] maxH = new int[] { 0 };
        int width = scene.getLightMapWidth();
        int height = scene.getLightMapHeight();
        Vector2f pixelSize = new Vector2f(1, 1).div(width, height);
        int[] pixels = null;
        BufferedImage image = null;

        renderables.clear();
        triangles.clear();
        lights.clear();

        scene.getRoot().calcBoundsAndTransform(scene.getCamera());

        if(deleteLightMap) {
            if(file.exists()) {
                Log.put(1, "deleting - " + file);
                file.delete();
            }
        }
        if(!file.exists()) {
            pixels = new int[width * height];
            for(int i = 0; i != pixels.length; i++) {
                pixels[i] = 0xFF000000;
            }
        }

        scene.getRoot().traverse((n) -> {
            if(n.isVisible()) {
                if(n.hasMesh() && n.isLightMapEnabled()) {
                    renderables.add(n);
                    if(n.getCastsShadow()) {
                        for(int i = 0; i != n.getTriangleCount(); i++) {
                            triangles.add(n.getTriangle(i, new Triangle()));
                        }
                    }
                }
                if(n.isLight()) {
                    lights.add(n);
                }
                return true;
            }
            return false;
        });

        OctTree tree = null;

        if(!file.exists()) {
            tree = OctTree.create(triangles, 16);
        }
        triangles.clear();

        for(Node renderable : renderables) {
            for(int i = 0; i != renderable.getFaceCount(); i++) {
                float v1x = renderable.getVertexComponent(renderable.getFaceVertex(i, 0), 0);
                float v1y = renderable.getVertexComponent(renderable.getFaceVertex(i, 0), 1);
                float v1z = renderable.getVertexComponent(renderable.getFaceVertex(i, 0), 2);
                float v2x = renderable.getVertexComponent(renderable.getFaceVertex(i, 1), 0);
                float v2y = renderable.getVertexComponent(renderable.getFaceVertex(i, 1), 1);
                float v2z = renderable.getVertexComponent(renderable.getFaceVertex(i, 1), 2);
                float v3x = renderable.getVertexComponent(renderable.getFaceVertex(i, 2), 0);
                float v3y = renderable.getVertexComponent(renderable.getFaceVertex(i, 2), 1);
                float v3z = renderable.getVertexComponent(renderable.getFaceVertex(i, 2), 2);
                Vector3f e1 = new Vector3f();
                Vector3f e2 = new Vector3f();
                Vector3f p1 = new Vector3f(v1x, v1y, v1z);
                Vector3f p2 = new Vector3f(v2x, v2y, v2z);
                Vector3f p3 = new Vector3f(v3x, v3y, v3z);
                Vector3f n1 = new Vector3f(
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 0),  7),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 0),  8),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 0),  9)
                );
                Vector3f n2 = new Vector3f(
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 1),  7),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 1),  8),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 1),  9)
                );
                Vector3f n3 = new Vector3f(
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 2),  7),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 2),  8),
                    renderable.getVertexComponent(renderable.getFaceVertex(i, 2),  9)
                );
                Vector3f normal = new Vector3f();
                Matrix4f modelIT = new Matrix4f(renderable.getModel()).invert().transpose();

                n1.mulDirection(modelIT);
                n2.mulDirection(modelIT);
                n3.mulDirection(modelIT);

                p1.mulPosition(renderable.getModel());
                p2.mulPosition(renderable.getModel());
                p3.mulPosition(renderable.getModel());

                p2.sub(p1, e1).normalize();
                p3.sub(p2, e2).normalize();
                e1.cross(e2, normal).normalize();
                normal.cross(e1, e2).normalize();

                float x1 = Float.MAX_VALUE;
                float y1 = Float.MAX_VALUE;
                int lx = 0;
                int ly = 0;
                int w, h;

                if(renderable.getFaceVertexCount(i) == 4) {
                    w = (int)Math.ceil(p2.distance(p1) / 16);
                    h = (int)Math.ceil(p3.distance(p2) / 16);
                    w = Math.max(1, w);
                    h = Math.max(1, h);
                } else {
                    float x2 = -Float.MAX_VALUE;
                    float y2 = -Float.MAX_VALUE;

                    for(int j = 0; j != renderable.getFaceVertexCount(i); j++) {
                        float vx = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 0);
                        float vy = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 1);
                        float vz = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 2);
                        Vector3f p = new Vector3f(vx, vy, vz).mulPosition(renderable.getModel());
                        float x = p.dot(e1);
                        float y = p.dot(e2);

                        x1 = Math.min(x, x1);
                        y1 = Math.min(y, y1);
                        x2 = Math.max(x, x2);
                        y2 = Math.max(y, y2);
                    }

                    x1 = (float)Math.floor(x1 / 16);
                    y1 = (float)Math.floor(y1 / 16);
                    x2 = (float)Math.ceil(x2 / 16);
                    y2 = (float)Math.ceil(y2 / 16);
                    lx = (int)(x1 * 16);
                    ly = (int)(y1 * 16);
                    w = (int)(x2 - x1) * 16;
                    h = (int)(y2 - y1) * 16;
                    w = w / 16 + 1;
                    h = h / 16 + 1;
                }

                if(!allocate(xy, w, h, maxH, width, height)) {
                    throw new Exception("Failed to allocate light map tile");
                }
                if(renderable.getFaceVertexCount(i) == 4) {
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 0), 5, (xy[0] + 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 0), 6, (xy[1] + 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 1), 5, (xy[0] + w - 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 1), 6, (xy[1] + 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 2), 5, (xy[0] + w - 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 2), 6, (xy[1] + h - 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 3), 5, (xy[0] + 0.5f) * pixelSize.x);
                    renderable.setVertexComponent(renderable.getFaceVertex(i, 3), 6, (xy[1] + h - 0.5f) * pixelSize.x);
                } else {
                    for(int j = 0; j != renderable.getFaceVertexCount(i); j++) {
                        float vx = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 0);
                        float vy = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 1);
                        float vz = renderable.getVertexComponent(renderable.getFaceVertex(i, j), 2);
                        Vector3f p = new Vector3f(vx, vy, vz).mulPosition(renderable.getModel());
                        float x = p.dot(e1);
                        float y = p.dot(e2);

                        x -= lx;
                        x += xy[0] * 16;
                        x += 8;
                        x /= width * 16;
                        y -= ly;
                        y += xy[1] * 16;
                        y += 8;
                        y /= height * 16;

                        renderable.setVertexComponent(renderable.getFaceVertex(i, j), 5, x);
                        renderable.setVertexComponent(renderable.getFaceVertex(i, j), 6, y);
                    }
                }

                if(!file.exists()) {
                    Log.put(1, "rendering light " + w + " x " + h + " ...");

                    float v1u = renderable.getVertexComponent(renderable.getFaceVertex(i, 0), 5);
                    float v1v = renderable.getVertexComponent(renderable.getFaceVertex(i, 0), 6);
                    float v2u = renderable.getVertexComponent(renderable.getFaceVertex(i, 1), 5);
                    float v2v = renderable.getVertexComponent(renderable.getFaceVertex(i, 1), 6);
                    float v3u = renderable.getVertexComponent(renderable.getFaceVertex(i, 2), 5);
                    float v3v = renderable.getVertexComponent(renderable.getFaceVertex(i, 2), 6);
                    Vector2f t1 = new Vector2f(v1u, v1v);
                    Vector2f t2 = new Vector2f(v3u, v3v);
                    Vector2f t3 = new Vector2f(v2u, v2v);
    
                    float area = (t3.x - t1.x) * (t2.y - t1.y) - (t3.y - t1.y) * (t2.x - t1.x);
    
                    if(area < 0) {
                        Log.put(0, "area < 0");
                    }
    
                    Vector3f n = new Vector3f();
                    Vector3f p = new Vector3f();
                    Vector4f c = new Vector4f();

                    for(int x = xy[0]; x != xy[0] + w; x++) {
                        for(int y = xy[1]; y != xy[1] + h; y++) {
                            float tx = (x + 0.5f) / (float)width;
                            float ty = (y + 0.5f) / (float)height;
                            float w0 = (tx - t2.x) * (t3.y - t2.y) - (ty - t2.y) * (t3.x - t2.x);
                            float w1 = (tx - t3.x) * (t1.y - t3.y) - (ty - t3.y) * (t1.x - t3.x);
                            float w2 = (tx - t1.x) * (t2.y - t1.y) - (ty - t1.y) * (t2.x - t1.x);
                            w0 /= area;
                            w1 /= area;
                            w2 /= area;
                            p.x = w0 * p1.x + w1 * p3.x + w2 * p2.x;
                            p.y = w0 * p1.y + w1 * p3.y + w2 * p2.y;
                            p.z = w0 * p1.z + w1 * p3.z + w2 * p2.z;                            
                            n.x = w0 * n1.x + w1 * n3.x + w2 * n2.x;
                            n.y = w0 * n1.y + w1 * n3.y + w2 * n2.y;
                            n.z = w0 * n1.z + w1 * n3.z + w2 * n2.z;
                            n.normalize();
                            c.set(renderable.getAmbientColor());

                            Random random;
                            Vector3f origin = new Vector3f();
                            Vector3f direction = new Vector3f();
                            BoundingBox bounds = new BoundingBox();
                            Vector3f point = new Vector3f();
                            Vector4f accum = new Vector4f(0, 0, 0, 1);
                            float[] time = new float[1];

                            for(Node light : lights) {
                                Vector3f lOffset = new Vector3f();
                                Vector3f lNormal = new Vector3f();

                                light.getAbsolutePosition().sub(p, lOffset);
                                lOffset.normalize(lNormal);

                                float atten = 1.0f - Math.min(lOffset.length() / light.getLightRadius(), 1.0f);
                                float dI = n.dot(lNormal);

                                if(atten < 1.0f && dI > 0.0f) {
                                    float sV = 1.0f;

                                    if(renderable.getReceivesShadow()) {
                                        lNormal.mul(2);
                                        float sF = 1.0f / light.getLightSampleCount();
                                        Vector3f lP = light.getAbsolutePosition();
                                        random = new Random(1000);
                                        for(int s = 0; s != light.getLightSampleCount(); s++) {
                                            origin.set(p).add(lNormal);
                                            float oX = random.nextFloat() * 2 - 1;
                                            Float oY = random.nextFloat() * 2 - 1;
                                            Float oZ = random.nextFloat() * 2 - 1;
                                            direction.set(oX, oY, oZ).mul(light.getLightSampleRadius()).add(lP).sub(origin);
                                            time[0] = direction.length();
                                            direction.normalize();
                                            if(inShadow(tree, bounds, point, origin, direction, time)) {
                                                sV -= sF;
                                            }
                                        }
                                    }
                                    Vector4f diff = new Vector4f(renderable.getDiffuseColor());
                                    
                                    diff.mul(light.getLightColor());
                                    diff.mul(sV * dI * atten);
                                    accum.add(diff);
                                }
                            }

                            float ao = 1;

                            random = new Random(1000);
                            for(int s = 0; s != scene.getAOSampleCount(); s++) {
                                float dX = random.nextFloat() * 2 - 1;
                                Float dY = random.nextFloat() * 2 - 1;
                                Float dZ = random.nextFloat() * 2 - 1;
                                direction.set(dX, dY, dZ);
                                if(direction.length() < 0.0000001) {
                                    direction.set(0, 1, 0);
                                }
                                direction.normalize();
                                if(direction.dot(normal) > 0.1) {
                                    origin.set(p).add(normal);
                                    time[0] = scene.getAORadius();
                                    if(inShadow(tree, bounds, point, origin, direction, time)) {
                                        ao = Math.max(scene.getMinAO(), ao - scene.getAOStrength() / (float)scene.getAOSampleCount());
                                    }
                                }
                            }

                            accum.mul(ao, ao, ao, 1);
                            c.add(accum);

                            float max = Math.max(c.x, Math.max(c.y, c.z));
                            if(max > 1) {
                                c.div(max);
                            }
                            int r = (int)(c.x * 255);
                            int g = (int)(c.y * 255);
                            int b = (int)(c.z * 255);
                            pixels[y * width + x] =  0xFF000000 | ((r << 16) & 0xFF0000) | ((g << 8) & 0xFF00) | (b & 0xFF);
                        }
                    }
                }
                xy[0] += w;
            }
        }
        if(!file.exists()) {
            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            image.setRGB(0, 0, width, height, pixels, 0, width);
            ImageIO.write(image, "PNG", file);
        }
        game.getAssets().unLoad(file);

        Texture texture2 = game.getAssets().load(file);

        if(scene.isLinear()) {
            texture2.toLinear(true);
        } else {
            texture2.toNearest(true);
        }
        for(Node renderable : renderables) {
            renderable.setTexture2(texture2);
        }

        renderables.clear();
        lights.clear();
    }

    private boolean inShadow(OctTree tree, BoundingBox bounds, Vector3f point, Vector3f origin, Vector3f direction, float[] time) {
        bounds.clear();
        bounds.add(origin);
        bounds.add(point.set(direction).mul(time[0]).add(origin));
        if(tree.getBounds().touches(bounds)) {
            for(int i = 0; i != tree.getTriangleCount(); i++) {
                if(tree.getTriangle(i, triangle).intersects(origin, direction, 0, time)) {
                    return true;
                }
            }
            for(int i = 0; i != tree.getChildCount(); i++) {
                if(inShadow(tree.getChild(i), bounds, point, origin, direction, time)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean allocate(int[] xy, int w, int h, int[] maxH, int lmW, int lmH) {
        if(xy[0] + w < lmW) {
            maxH[0] = Math.max(maxH[0], h);
        } else {
            xy[0] = 0;
            xy[1] += maxH[0];
            maxH[0] = h;
        }
        return xy[1] + h < lmH;
    }
}