package org.jgl3.scene;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.Log;
import org.jgl3.OctTree;
import org.jgl3.Texture;
import org.jgl3.Triangle;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class LightMapper {

    private Vector<Triangle> triangles = null;
    private float[] time = null;
    private Vector3f origin = null;
    private Vector3f direction = null;
    private boolean hit = false;
    
    public void map(Scene scene, File file, boolean rebuild) throws Exception {
        OctTree octTree = null;
        Vector<Object[]> tiles = new Vector<>();
        Vector<Node> meshes = new Vector<>();
        Vector<Node> lights = new Vector<>();
        int x = 0;
        int y = 0;
        int maxH = 0;
        float psx = 1.0f / scene.getLightMapWidth();
        float psy = 1.0f / scene.getLightMapHeight();
        
        if(!rebuild) {
            rebuild = !file.exists();
        }
        if(rebuild) {
            triangles = new Vector<>();
            scene.getRoot().traverse((n) -> {
                if(n.getCastsShadow()) {
                    for(int i = 0; i != n.getTriangleCount(); i++) {
                        triangles.add(n.getTriangle(i, new Triangle()));
                    }
                }
                return true;
            });
            octTree = OctTree.create(triangles, 16);
        }
        scene.getRoot().traverse((n) -> {
            if(n.isLight()) {
                lights.add(n);
            }
            if(n.hasMesh() && n.isLightMapEnabled()) {
                meshes.add(n);
            }
            return true;
        });

        for(Node mesh : meshes) {
            for(int i = 0; i != mesh.getFaceCount(); i++) {
                int vc = mesh.getFaceVertexCount(i);

                if(vc != 4) {
                    int i1 = 0;
                    int i2 = 1;
                    int i3 = 2;
                    boolean found = false;

                    for(int j = 0; j != vc; j++) {
                        int a = j;
                        int b = (j + 1) % vc;
                        int c = (j + 2) % vc;

                        Vector3f p1 = new Vector3f(
                            mesh.getVertexComponent(mesh.getFaceVertex(i, a), 0),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, a), 1),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, a), 2)
                        );
                        Vector3f p2 = new Vector3f(
                            mesh.getVertexComponent(mesh.getFaceVertex(i, b), 0),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, b), 1),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, b), 2)
                        );
                        Vector3f p3 = new Vector3f(
                            mesh.getVertexComponent(mesh.getFaceVertex(i, c), 0),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, c), 1),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, c), 2)
                        );
                        Vector3f e1 = p1.sub(p2, new Vector3f());
                        Vector3f e2 = p3.sub(p2, new Vector3f());

                        float dot = e1.normalize(new Vector3f()).dot(e2.normalize(new Vector3f()));

                        dot = Math.max(-0.999f, Math.min(0.999f, dot));

                        float degrees = (float)(Math.acos(dot) * 180 / Math.PI);

                        if(Math.abs(degrees - 90) > 0.1f) {
                            continue;
                        }

                        boolean contained = true;

                        for(int k = 0; k != vc; k++) {
                            Vector3f p = new Vector3f(
                                mesh.getVertexComponent(mesh.getFaceVertex(i, k), 0),
                                mesh.getVertexComponent(mesh.getFaceVertex(i, k), 1),
                                mesh.getVertexComponent(mesh.getFaceVertex(i, k), 2)
                            );
                            float u = p.sub(p2, new Vector3f()).dot(e1.normalize(new Vector3f()));
                            float v = p.sub(p2, new Vector3f()).dot(e2.normalize(new Vector3f()));

                            if(u < 0 || v < 0 || u > e1.length() || v > e2.length()) {
                                contained = false;
                                break;
                            }
                        }

                        if(!contained) {
                            continue;
                        }

                        i1 = a;
                        i2 = b;
                        i3 = c;

                        found = true;

                        break;
                    }

                    if(!found) {
                        Log.put(0, "Could not map polygon, did not find a 90 degree corner that fits points to a quad ...");
                        for(int j = 0; j != vc; j++) {
                            mesh.setVertexComponent(mesh.getFaceVertex(i, j), 5, 0.5f * psx);
                            mesh.setVertexComponent(mesh.getFaceVertex(i, j), 6, 0.5f * psy);
                        }
                        continue;
                    }

                    Vector3f p1 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i1), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i1), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i1), 2)
                    );
                    Vector3f p2 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i2), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i2), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i2), 2)
                    );
                    Vector3f p3 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i3), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i3), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, i3), 2)
                    );

                    p1.mulPosition(mesh.getModel());
                    p2.mulPosition(mesh.getModel());
                    p3.mulPosition(mesh.getModel());

                    Vector3f e1 = p1.sub(p2, new Vector3f());
                    Vector3f e2 = p3.sub(p2, new Vector3f());
                    Vector3f n = e2.cross(e1, new Vector3f()).normalize();

                    int w = (int)Math.ceil(e1.length()) / 16 + 1;
                    int h = (int)Math.ceil(e2.length()) / 16 + 1;

                    if(x + w >= scene.getLightMapWidth()) {
                        x = 0;
                        y += maxH;
                        maxH = 0;
                    }
                    maxH = Math.max(maxH, h);
                    if(y + maxH >= scene.getLightMapHeight()) {
                        Log.put(0, "Failed to allocate light map tile");
                        return;
                    }

                    for(int j = 0; j != vc; j++) {
                        Vector3f p = new Vector3f(
                            mesh.getVertexComponent(mesh.getFaceVertex(i, j), 0),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, j), 1),
                            mesh.getVertexComponent(mesh.getFaceVertex(i, j), 2)
                        );

                        p.mulPosition(mesh.getModel());

                        float u = p.sub(p2, new Vector3f()).dot(e1.normalize(new Vector3f()));
                        float v = p.sub(p2, new Vector3f()).dot(e2.normalize(new Vector3f()));

                        u += 8;
                        u /= 16;
                        u += x;
                        u *= psx;
                        v += 8;
                        v /= 16;
                        v += y;
                        v *= psy;

                        mesh.setVertexComponent(mesh.getFaceVertex(i, j), 5, u);
                        mesh.setVertexComponent(mesh.getFaceVertex(i, j), 6, v);
                    }

                    Vector3f a = p2;
                    Vector3f b = p1;
                    Vector3f c = p3;
                    Vector3f d = p2.add(e1, new Vector3f()).add(e2, new Vector3f());

                    tiles.add(new Object[] { mesh, x, y, w, h, a, b, c, d, n });

                    x += w;
                } else {
                    Vector3f p1 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 0), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 0), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 0), 2)
                    );
                    Vector3f p2 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 1), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 1), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 1), 2)
                    );
                    Vector3f p3 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 2), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 2), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 2), 2)
                    );
                    Vector3f p4 = new Vector3f(
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 3), 0),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 3), 1),
                        mesh.getVertexComponent(mesh.getFaceVertex(i, 3), 2)
                    );

                    p1.mulPosition(mesh.getModel());
                    p2.mulPosition(mesh.getModel());
                    p3.mulPosition(mesh.getModel());
                    p4.mulPosition(mesh.getModel());

                    Vector3f e1 = p2.sub(p1, new Vector3f());
                    Vector3f e2 = p4.sub(p1, new Vector3f());
                    Vector3f n = e1.cross(e2, new Vector3f()).normalize();
                    int w = (int)Math.ceil(e1.length() / 16) + 1;
                    int h = (int)Math.ceil(e2.length() / 16) + 1;

                    if(x + w >= scene.getLightMapWidth()) {
                        x = 0;
                        y += maxH;
                        maxH = 0;
                    }
                    maxH = Math.max(maxH, h);
                    if(y + maxH >= scene.getLightMapHeight()) {
                        Log.put(0, "Failed to allocate light map tile");
                        return;
                    }

                    mesh.setVertexComponent(mesh.getFaceVertex(i, 0), 5, (x + 0 + 0.5f) * psx);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 0), 6, (y + 0 + 0.5f) * psy);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 1), 5, (x + w - 0.5f) * psx);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 1), 6, (y + 0 + 0.5f) * psy);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 2), 5, (x + w - 0.5f) * psx);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 2), 6, (y + h - 0.5f) * psy);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 3), 5, (x + 0 + 0.5f) * psx);
                    mesh.setVertexComponent(mesh.getFaceVertex(i, 3), 6, (y + h - 0.5f) * psy);

                    tiles.add(new Object[] { mesh, x, y, w, h, p1, p2, p3, p4, n });

                    x += w;
                }
            }
            mesh.compileMesh();
        }

        if(rebuild) {
            Log.put(1, "Rebuilding light map ...");

            int[] pixels = new int[scene.getLightMapWidth() * scene.getLightMapHeight()];

            for(int i = 0; i != pixels.length; i++) {
                pixels[i] = 0xFFFF00FF;
            }

            for(Object[] tile : tiles) {
                Node mesh = (Node)tile[0];
                int tx = (Integer)tile[1];
                int ty = (Integer)tile[2];
                int tw = (Integer)tile[3];
                int th = (Integer)tile[4];
                Vector3f p1 = (Vector3f)tile[5];
                Vector3f p2 = (Vector3f)tile[6];
                Vector3f p3 = (Vector3f)tile[7];
                Vector3f p4 = (Vector3f)tile[8];
                Vector3f n = (Vector3f)tile[9];

                Log.put(2, "Rendering tile " + tx + ", " + ty + ", " + tw + ", " + th + " ... ");

                for(int ix = tx; ix < tx + tw; ix++) {
                    for(int iy = ty; iy < ty + th; iy++) {
                        Vector3f a = p1.lerp(p2, (ix + 0.5f - tx) / (float)tw, new Vector3f());
                        Vector3f b = p4.lerp(p3, (ix + 0.5f - tx) / (float)tw, new Vector3f());
                        Vector3f p = a.lerp(b, (iy + 0.5f - ty) / (float)th, new Vector3f());
                        Vector4f color = new Vector4f();
                        Random random;
                        float sv;

                        for(Node light : lights) {
                            Vector3f lpos = light.getAbsolutePosition();
                            Vector3f lX = lpos.sub(p, new Vector3f());
                            Vector3f lN = lX.normalize(new Vector3f());
                            float lDotN = lN.dot(n);
                            float atten = 1 - Math.min(lX.length() / light.getLightRadius(), 1);

                            if(lDotN > 0 && atten > 0) {
                                sv = 1;

                                if(mesh.getReceivesShadow()) {
                                    random = new Random(1000);

                                    for(int i = 0; i != scene.getSampleCount(); i++) {
                                        origin = p.add(lN, new Vector3f());

                                        Vector3f sample = new Vector3f(
                                            random.nextFloat() * 2 - 1,
                                            random.nextFloat() * 2 - 1,
                                            random.nextFloat() * 2 - 1
                                        );

                                        if(sample.length() < 0.0000001) {
                                            sample.set(0, 1, 0);
                                        }
                                        sample.normalize(scene.getSampleRadius());

                                        direction = lpos.add(sample, sample).sub(origin, new Vector3f());

                                        BoundingBox bounds = new BoundingBox();

                                        hit = false;
                                        time = new float[]{ direction.length() };
                                        direction.normalize();
                                        bounds.add(origin);
                                        bounds.add(new Vector3f(direction).mul(time[0]).add(origin));
                                        bounds.buffer(1, 1, 1);
                                        octTree.traverse(bounds, (tri) -> {
                                            if(tri.intersects(origin, direction, 0, time)) {
                                                hit = true;
                                            }
                                            return !hit;
                                        });
                                        if(hit) {
                                            sv -= 1.0f / scene.getSampleCount();
                                        }
                                    }
                                }

                                Vector4f color2 = new Vector4f(mesh.getDiffuseColor()).mul(light.getLightColor());

                                color2.mul(sv * atten);
                                if(scene.isLightMapLambert()) {
                                    color2.mul(Math.min(1, lDotN));
                                }
                                color.add(color2);
                            }
                        }

                        if(mesh.getReceivesShadow()) {
                            int  count = 0;

                            random = new Random(1000);
                            sv = 1;

                            for(int i = 0; i != scene.getSampleCount(); i++) {
                                Vector3f direction = new Vector3f(
                                    random.nextFloat() * 2 - 1,
                                    random.nextFloat() * 2 - 1,
                                    random.nextFloat() * 2 - 1
                                );

                                if(direction.length() < 0.0000001) {
                                    direction.set(0, 1, 0);
                                }
                                direction.normalize();

                                if(direction.dot(n) > 0.2f) {
                                    count++;
                                }
                            }

                            random = new Random(1000);

                            for(int i = 0; i != scene.getSampleCount(); i++) {
                                origin = p.add(n, new Vector3f());

                                Vector3f direction = new Vector3f(
                                    random.nextFloat() * 2 - 1,
                                    random.nextFloat() * 2 - 1,
                                    random.nextFloat() * 2 - 1
                                );

                                if(direction.length() < 0.0000001) {
                                    direction.set(0, 1, 0);
                                }
                                direction.normalize();

                                if(direction.dot(n) > 0.2f) {
                                    BoundingBox bounds = new BoundingBox();

                                    hit = false;
                                    time = new float[]{ scene.getAOLength() };
                                    bounds.add(origin);
                                    bounds.add(new Vector3f(direction).mul(time[0]).add(origin));
                                    bounds.buffer(1, 1, 1);
                                    octTree.traverse(bounds, (tri) -> {
                                        if(tri.intersects(origin, direction, 0, time)) {
                                            hit = true;
                                        }
                                        return !hit;
                                    });
                                    if(hit) {
                                        sv = Math.max(0, sv - scene.getAOStrength() / count);
                                    }
                                }
                            }
                            color.mul(sv);
                        }
                        color.add(mesh.getAmbientColor());

                        float max = Math.max(color.x, Math.max(color.y, color.z));

                        if(max > 1) {
                            color.div(max);
                        }

                        int rc = (int)(color.x * 255) & 0xFF;
                        int gc = (int)(color.y * 255) & 0xFF;
                        int bc = (int)(color.z * 255) & 0xFF;

                        pixels[iy * scene.getLightMapWidth() + ix] =  0xFF000000 | ((rc << 16) & 0xFF0000) | ((gc << 8) & 0xFF00) | bc;
                    }
                }
            }

            BufferedImage image = new BufferedImage(scene.getLightMapWidth(), scene.getLightMapHeight(), BufferedImage.TYPE_INT_ARGB);

            image.setRGB(0, 0, scene.getLightMapWidth(), scene.getLightMapHeight(), pixels, 0, scene.getLightMapWidth());

            ImageIO.write(image, "png", file);
        }

        Game.getInstance().getAssets().unLoad(file);

        scene.getRoot().traverse((n) -> {
            if(n.isLightMapEnabled()) {
                Texture texture = Game.getInstance().getAssets().load(file);

                texture.toLinear(true);
                n.setTexture2(texture);
            }
            return true;
        });
    }
}
