package org.jgl3.demo;

import java.io.File;
import java.util.Hashtable;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;
import org.joml.GeometryUtils;
import org.joml.Vector3f;

public class Ledge implements MeshBuilder.Generator  {

    @Override
    public Mesh generate(File file, String[] tokens) throws Exception {
        MeshBuilder builder = new MeshBuilder();
        Hashtable<String, Integer> vertices = new Hashtable<>();
        int rows = tokens.length;

        for(int r = 1; r != rows; r++) {
            String row = tokens[r];
            int cols = row.length();

            for(int c = 0; c != cols; c++) {
                char ch = row.charAt(c);
                int i = (int)ch - (int)'0';

                if(i >= 0 && i <= 5) {
                    int x = -64 * cols / 2 + c * 64;
                    int y = -64;
                    int z = -64 * rows / 2 + (r - 1) * 64;

                    if(i < 3) {
                        addVertices(x, y, z, 0, builder, vertices);
                    } else {
                        addVertices(x, y, z, 1, builder, vertices);
                        y += 64;
                        addVertices(x, y, z, 1, builder, vertices);
                    }
                }
            }
        }

        for(int r = 1; r != rows; r++) {
            String row = tokens[r];
            int cols = row.length();

            for(int c = 0; c != cols; c++) {
                char ch = row.charAt(c);
                int i = (int)ch - (int)'0';

                if(i >= 0 && i <= 5) {
                    int x = -64 * cols / 2 + c * 64;
                    int y = -64;
                    int z = -64 * rows / 2 + (r - 1) * 64;

                    if(i < 3) {
                        addFaces(x, y, z, 0, tokens, r, c, rows, cols, builder, vertices);
                    } else {
                        addFaces(x, y, z, 1, tokens, r, c, rows, cols, builder, vertices);
                        y += 64;
                        addFaces(x, y, z, 1, tokens, r, c, rows, cols, builder, vertices);
                    }
                }
            }
        }

        builder.subdivide(3);
        builder.smooth(8);
        builder.calcNormals(true);

        return builder.build(file);
    }

    private String keyFor(int x, int y, int z, int h) {
        return x + ":" +  y + ":" + z + ":" + h;
    }

    private void addVertex(int x, int y, int z, int h, MeshBuilder builder, Hashtable<String, Integer> vertices) {
        String key = keyFor(x, y, z, h);

        if(!vertices.containsKey(key)) {
            vertices.put(key, builder.addVertex(x, y, z));
        }
    }

    private boolean isEdge(int row, int col, String[] tokens, char ch, int rows, int cols) {
        if(row < 1 || row >= rows || col < 0 || col >= cols) {
            return true;
        }
        int i = (int)tokens[row].charAt(col) - (int)'0';
        int j = (int)ch - (int)'0';

        if(j < 3) {
            return !(i >= 0 && i < 3);
        } else {
            return !(i >= 3 && i <= 5);
        }
    }

    private void addVertices(int x, int y, int z, int h, MeshBuilder builder, Hashtable<String, Integer> vertices) {
        addVertex(x + 00, y + 00, z + 00, h, builder, vertices);
        addVertex(x + 00, y + 64, z + 00, h, builder, vertices);
        addVertex(x + 64, y + 64, z + 00, h, builder, vertices);
        addVertex(x + 64, y + 00, z + 00, h, builder, vertices);
        addVertex(x + 00, y + 00, z + 64, h, builder, vertices);
        addVertex(x + 00, y + 64, z + 64, h, builder, vertices);
        addVertex(x + 64, y + 64, z + 64, h, builder, vertices);
        addVertex(x + 64, y + 00, z + 64, h, builder, vertices);
    }

    private void addSide(String v1, String v2, String v3, String v4, boolean noEdge, MeshBuilder builder, Hashtable<String, Integer> vertices) throws Exception {
        int f = builder.addFace(
            false,
            vertices.get(v1), 
            vertices.get(v2),
            vertices.get(v3),
            vertices.get(v4)
            );
        int e = builder.getFaceEdge(f);
        Vector3f n = new Vector3f();

        GeometryUtils.normal(
            builder.getVertexPosition(builder.getEdgeVertex(e + 0)),
            builder.getVertexPosition(builder.getEdgeVertex(e + 1)),
            builder.getVertexPosition(builder.getEdgeVertex(e + 2)),
            n
        );

        if(noEdge) {
            builder.setQuadTextureCoordinates(f, 0.5f, 0.5f + 512, 512 - 1, 512 - 1, 1024, 1024);
        } else {
            builder.setQuadTextureCoordinates(f, 0.5f, 0.5f, 512 - 1, 512 - 1, 1024, 1024);
            if(n.z > 0.5f) {
                builder.rotateTextureCoordinates(f, 2);
            } else if(n.x < -0.5f) {
                builder.rotateTextureCoordinates(f, 2);
            } else {
                builder.rotateTextureCoordinates(f, 3);
            }
        }
    }

    private void addFaces(int x, int y, int z, int h, String[] tokens, int row, int col, int rows, int cols, MeshBuilder builder, Hashtable<String, Integer> vertices) throws Exception {
        char ch = tokens[row].charAt(col);
        int f;

        if(h == 0 || y == 0) {
            f = builder.addFace(
                false, 
                vertices.get(keyFor(x + 00, y + 64, z + 00, h)),
                vertices.get(keyFor(x + 00, y + 64, z + 64, h)),
                vertices.get(keyFor(x + 64, y + 64, z + 64, h)),
                vertices.get(keyFor(x + 64, y + 64, z + 00, h))
            );
            if(ch == '0' || ch == '3') {
                builder.setQuadTextureCoordinates(f, 4, 4, 1, 1, 1024, 1024);
            } else if(ch == '1' || ch == '4') {
                builder.setQuadTextureCoordinates(f, 0.5f + 512, 0.5f, 512 - 1, 512 - 1, 1024, 1024);
            } else {
                builder.setQuadTextureCoordinates(f, 0.5f + 512, 0.5f + 512, 512 - 1, 512 - 1, 1024, 1024);
            }
        }
        if(y == -64) {
            f = builder.addFace(
                false,
                vertices.get(keyFor(x + 00, y + 00, z + 00, h)),
                vertices.get(keyFor(x + 64, y + 00, z + 00, h)),
                vertices.get(keyFor(x + 64, y + 00, z + 64, h)),
                vertices.get(keyFor(x + 00, y + 00, z + 64, h))
            );
            builder.setQuadTextureCoordinates(f, 0.5f, 0.5f + 512, 512 - 1, 512 - 1, 1024, 1024);
        }

        if(isEdge(row - 1, col, tokens, ch, rows, cols)) {
            addSide(
                keyFor(x + 00, y + 00, z + 00, h),
                keyFor(x + 00, y + 64, z + 00, h),
                keyFor(x + 64, y + 64, z + 00, h),
                keyFor(x + 64, y + 00, z + 00, h),
                y == -64 && h == 1,
                builder, 
                vertices
            );
        }
        if(isEdge(row + 1, col, tokens, ch, rows, cols)) {
            addSide(
                keyFor(x + 00, y + 00, z + 64, h),
                keyFor(x + 64, y + 00, z + 64, h),
                keyFor(x + 64, y + 64, z + 64, h),
                keyFor(x + 00, y + 64, z + 64, h),
                y == -64 && h == 1,
                builder, 
                vertices
            );
        }
        if(isEdge(row, col - 1, tokens, ch, rows, cols)) {
            addSide(
                keyFor(x + 00, y + 00, z + 00, h),
                keyFor(x + 00, y + 00, z + 64, h),
                keyFor(x + 00, y + 64, z + 64, h),
                keyFor(x + 00, y + 64, z + 00, h),
                y == -64 && h == 1,
                builder, 
                vertices
            );
        }
        if(isEdge(row, col + 1, tokens, ch, rows, cols)) {
            addSide(
                keyFor(x + 64, y + 00, z + 00, h),
                keyFor(x + 64, y + 64, z + 00, h),
                keyFor(x + 64, y + 64, z + 64, h),
                keyFor(x + 64, y + 00, z + 64, h),
                y == -64 && h == 1,
                builder, 
                vertices
            );
        }
    }
}
