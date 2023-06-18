package org.jgl3;

import java.util.Vector;

public final class OctTree {

    public static interface Visit {
        boolean visit(Triangle triangle);
    }

    public static OctTree create(Vector<Triangle> triangles, int minTrisPerTree) {
        Triangle[] tris = triangles.toArray(new Triangle[triangles.size()]);
        BoundingBox bounds = new BoundingBox();

        for (Triangle tri : tris) {
            bounds.add(tri.getP1());
            bounds.add(tri.getP2());
            bounds.add(tri.getP3());
        }
        bounds.buffer(1, 1, 1);

        return new OctTree(tris, bounds, minTrisPerTree);
    }

    private final Vector<OctTree> children = new Vector<>();
    private final Triangle[] triangles;
    private final BoundingBox bounds;

    public OctTree(Triangle[] triangles, BoundingBox bounds, int minTrisPerTree) {
        this.bounds = bounds;
        if (triangles.length > minTrisPerTree) {
            float lx = bounds.getMin().x;
            float ly = bounds.getMin().y;
            float lz = bounds.getMin().z;
            float hx = bounds.getMax().x;
            float hy = bounds.getMax().y;
            float hz = bounds.getMax().z;
            float cx = (lx + hx) / 2;
            float cy = (ly + hy) / 2;
            float cz = (lz + hz) / 2;
            BoundingBox[] bList = new BoundingBox[] { new BoundingBox(lx, ly, lz, cx, cy, cz),
                    new BoundingBox(cx, ly, lz, hx, cy, cz), new BoundingBox(lx, cy, lz, cx, hy, cz),
                    new BoundingBox(lx, ly, cz, cx, cy, hz), new BoundingBox(cx, cy, cz, hx, hy, hz),
                    new BoundingBox(cx, cy, lz, hx, hy, cz), new BoundingBox(cx, ly, cz, hx, cy, hz),
                    new BoundingBox(lx, cy, cz, cx, hy, hz) };
            Vector<Vector<Triangle>> triLists = new Vector<>();
            Vector<Triangle> keep = new Vector<>();
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            triLists.add(new Vector<>());
            for (Triangle triangle : triangles) {
                boolean added = false;

                for (int i = 0; i != bList.length; i++) {
                    BoundingBox b = bList[i];

                    if (b.contains(triangle.getP1()) && b.contains(triangle.getP2()) && b.contains(triangle.getP3())) {
                        added = true;
                        triLists.get(i).add(triangle);
                        break;
                    }
                }
                if (!added) {
                    keep.add(triangle);
                }
            }
            for (int i = 0; i != triLists.size(); i++) {
                if (triLists.get(i).size() != 0) {
                    children.add(new OctTree(triLists.get(i).toArray(new Triangle[triLists.get(i).size()]), bList[i],
                            minTrisPerTree));
                }
            }
            this.triangles = keep.toArray(new Triangle[keep.size()]);
        } else {
            this.triangles = triangles;
        }
    }

    public int getChildCount() {
        return children.size();
    }

    public OctTree getChild(int i) {
        return children.get(i);
    }

    public int getTriangleCount() {
        return triangles.length;
    }

    public Triangle getTriangle(int i, Triangle triangle) {
        return triangle.set(triangles[i]);
    }

    public BoundingBox getBounds() {
        return bounds;
    }

    public void traverse(BoundingBox bounds, Visit v) {
        if(this.bounds.touches(bounds)) {
            for(Triangle triangle : triangles) {
                if(!v.visit(triangle)) {
                    return;
                }
            }
            for(OctTree tree : children) {
                tree.traverse(bounds, v);
            }
        }
    }
}