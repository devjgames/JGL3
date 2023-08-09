package org.jgl3;

import java.io.File;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class LightMapper {

    private float[] time = null;
    private Vector3f origin = null;
    private Vector3f direction = null;
    private boolean hit = false;
    private int lightMapWidth = 128;
    private int lightMapHeight = 128;
    private int sampleCount = 64;
    private float sampleRadius = 32;
    private float aoStrength = 1;
    private float aoLength = 32;
    private final Vector4f ambient = new Vector4f();

    public int getLightMapWidth() {
        return lightMapWidth;
    }

    public void setLightMapWidth(int width) {
        lightMapWidth = width;
    }

    public int getLightMapHeight() {
        return lightMapHeight;
    }

    public void setLightMapHeight(int height) {
        lightMapHeight = height;
    }

    public int getSampleCount() {
        return sampleCount;
    }

    public void setSampleCount(int count) {
        sampleCount = count;
    }

    public float getSampleRadius() {
        return sampleRadius;
    }

    public void setSampleRadius(float radius) {
        sampleRadius = radius;
    }

    public float getAOStrength() {
        return aoStrength;
    }

    public void setAOStrength(float strength) {
        aoStrength = strength;
    }

    public float getAOLength() {
        return aoLength;
    }

    public void setAOLength(float length) {
        aoLength = length;
    }
    
    public void map(Scene scene, File file, boolean rebuild) throws Exception {
        OctTree octTree = null;
        Vector<Object[]> tiles = new Vector<>();
        Vector<Mesh> meshes = new Vector<>();
        Vector<PointLight> lights = new Vector<>();
        Vector<Triangle> triangles = new Vector<>();
        int x = 0;
        int y = 0;
        int maxH = 0;
        float psx = 1.0f / lightMapWidth;
        float psy = 1.0f / lightMapHeight;

        scene.getRoot().calcModel();
        
        if(!rebuild) {
            rebuild = !file.exists();
        }
        if(rebuild) {
            scene.getRoot().traverse(scene, (s, n) -> {
                if(n instanceof MeshPT2) {
                    Mesh mesh = (Mesh)n;
                    MeshPipeline p = mesh.getMeshPipeline();

                    for(int i = 0; i != p.getIndexCount(); ) {
                        int i1 = p.getIndex(i++);
                        int i2 = p.getIndex(i++);
                        int i3 = p.getIndex(i++);
                        Triangle triangle = new Triangle();

                        triangle.getP1().set(
                            p.getVertexComponent(i1, 0),
                            p.getVertexComponent(i1, 1),
                            p.getVertexComponent(i1, 2)
                        );
                        triangle.getP2().set(
                            p.getVertexComponent(i2, 0),
                            p.getVertexComponent(i2, 1),
                            p.getVertexComponent(i2, 2)
                        );
                        triangle.getP3().set(
                            p.getVertexComponent(i3, 0),
                            p.getVertexComponent(i3, 1),
                            p.getVertexComponent(i3, 2)
                        );
                        triangle.calcPlane();
                        triangle.transform(n.getModel());

                        triangles.add(triangle);
                    }
                }
                return true;
            });
            octTree = OctTree.create(triangles, 16);
        }

        ambient.set(0, 0, 0, 1);

        scene.getRoot().traverse(scene, (s, n) -> {
            if(n instanceof PointLight) {
                lights.add((PointLight)n);
            }
            if(n instanceof AmbientLight) {
                ambient.add(((Light)n).getColor());
            }
            if(n instanceof MeshPT2) {
                meshes.add((Mesh)n);
            }
            return true;
        });

        for(Mesh mesh : meshes) {
            MeshPipeline p = mesh.getMeshPipeline();

            for(int i = 0; i != p.getFaceCount(); i++) {
                int vc = p.getFaceVertexCount(i);

                if(vc == 4) {
                    Vector3f p1 = new Vector3f(
                        p.getVertexComponent(p.getFaceVertex(i, 0), 0),
                        p.getVertexComponent(p.getFaceVertex(i, 0), 1),
                        p.getVertexComponent(p.getFaceVertex(i, 0), 2)
                    );
                    Vector3f p2 = new Vector3f(
                        p.getVertexComponent(p.getFaceVertex(i, 1), 0),
                        p.getVertexComponent(p.getFaceVertex(i, 1), 1),
                        p.getVertexComponent(p.getFaceVertex(i, 1), 2)
                    );
                    Vector3f p3 = new Vector3f(
                        p.getVertexComponent(p.getFaceVertex(i, 2), 0),
                        p.getVertexComponent(p.getFaceVertex(i, 2), 1),
                        p.getVertexComponent(p.getFaceVertex(i, 2), 2)
                    );
                    Vector3f p4 = new Vector3f(
                        p.getVertexComponent(p.getFaceVertex(i, 3), 0),
                        p.getVertexComponent(p.getFaceVertex(i, 3), 1),
                        p.getVertexComponent(p.getFaceVertex(i, 3), 2)
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

                    if(x + w >= lightMapWidth) {
                        x = 0;
                        y += maxH;
                        maxH = 0;
                    }
                    maxH = Math.max(maxH, h);
                    if(y + maxH >= lightMapHeight) {
                        Log.put(0, "Failed to allocate light map tile");
                        return;
                    }

                    p.setVertexComponent(p.getFaceVertex(i, 0), 5, (x + 0 + 0.5f) * psx);
                    p.setVertexComponent(p.getFaceVertex(i, 0), 6, (y + 0 + 0.5f) * psy);
                    p.setVertexComponent(p.getFaceVertex(i, 1), 5, (x + w - 0.5f) * psx);
                    p.setVertexComponent(p.getFaceVertex(i, 1), 6, (y + 0 + 0.5f) * psy);
                    p.setVertexComponent(p.getFaceVertex(i, 2), 5, (x + w - 0.5f) * psx);
                    p.setVertexComponent(p.getFaceVertex(i, 2), 6, (y + h - 0.5f) * psy);
                    p.setVertexComponent(p.getFaceVertex(i, 3), 5, (x + 0 + 0.5f) * psx);
                    p.setVertexComponent(p.getFaceVertex(i, 3), 6, (y + h - 0.5f) * psy);

                    tiles.add(new Object[] { mesh, x, y, w, h, p1, p2, p3, p4, n });

                    x += w;
                } else {
                    Log.put(0, "Could not map polygon, polygon is not a quad ...");
                    for(int j = 0; j != vc; j++) {
                        p.setVertexComponent(p.getFaceVertex(i, j), 5, 0.5f * psx);
                        p.setVertexComponent(p.getFaceVertex(i, j), 6, 0.5f * psy);
                    }
                }
            }
            p.bufferVertices(VertexUsage.STATIC);
        }

        if(rebuild) {
            Log.put(1, "Rebuilding light map ...");

            int[] pixels = new int[lightMapWidth * lightMapHeight];

            for(int i = 0; i != pixels.length; i++) {
                pixels[i] = 0xFFFF00FF;
            }

            for(Object[] tile : tiles) {
                MeshPT2 mesh = (MeshPT2)tile[0];
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

                        for(PointLight light : lights) {
                            Vector3f lpos = light.getAbsolutePosition();
                            Vector3f lX = lpos.sub(p, new Vector3f());
                            Vector3f lN = lX.normalize(new Vector3f());
                            float lDotN = lN.dot(n);
                            float atten = 1 - Math.min(lX.length() / light.getRange(), 1);

                            if(lDotN > 0 && atten > 0) {
                                sv = 1;

                                random = new Random(1000);

                                for(int i = 0; i !=  sampleCount; i++) {
                                    origin = p.add(lN, new Vector3f());

                                    Vector3f sample = new Vector3f(
                                        random.nextFloat() * 2 - 1,
                                        random.nextFloat() * 2 - 1,
                                        random.nextFloat() * 2 - 1
                                    );

                                    if(sample.length() < 0.0000001) {
                                        sample.set(0, 1, 0);
                                    }
                                    sample.normalize(sampleRadius);

                                    direction = lpos.add(sample, sample).sub(origin, new Vector3f());

                                    BoundingBox bounds = new BoundingBox();

                                    hit = false;
                                    time = new float[]{ direction.length() };
                                    direction.normalize();
                                    bounds.clear();
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
                                        sv -= 1.0f / sampleCount;
                                    }
                                }

                                Vector4f color2 = new Vector4f(mesh.getColor()).mul(light.getColor());

                                color2.mul(sv * atten).mul(Math.min(1, lDotN));
                                color.add(color2);
                            }
                        }

                        int  count = 0;

                        random = new Random(1000);
                        sv = 1;

                        for(int i = 0; i != sampleCount; i++) {
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

                        for(int i = 0; i != sampleCount; i++) {
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
                                time = new float[]{ aoLength };
                                bounds.clear();
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
                                    sv = Math.max(0, sv - aoStrength / count);
                                }
                            }
                        }
                        color.mul(sv);
                    
                        color.add(ambient);

                        float max = Math.max(color.x, Math.max(color.y, color.z));

                        if(max > 1) {
                            color.div(max);
                        }

                        int rc = (int)(color.x * 255) & 0xFF;
                        int gc = (int)(color.y * 255) & 0xFF;
                        int bc = (int)(color.z * 255) & 0xFF;

                        pixels[iy * lightMapWidth + ix] =  0xFF000000 | ((rc << 16) & 0xFF0000) | ((gc << 8) & 0xFF00) | bc;
                    }
                }
            }

            BufferedImage image = new BufferedImage(lightMapWidth, lightMapHeight, BufferedImage.TYPE_INT_ARGB);

            image.setRGB(0, 0, lightMapWidth, lightMapHeight, pixels, 0, lightMapWidth);

            ImageIO.write(image, "png", file);
        }

        Game.getInstance().getAssets().unLoad(file);

        scene.getRoot().traverse(scene, (s, n) -> {
            if(n instanceof MeshPT2) {
                MeshPT2 mesh = (MeshPT2)n;
                Texture texture = Game.getInstance().getAssets().load(file);

                texture.toLinear(true);
                mesh.setTexture2(texture);
            }
            return true;
        });
    }
}
