package org.jgl3.demo;

import java.io.File;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;
import org.jgl3.scene.MeshBuilder.Generator;

public class Map implements Generator {

    private static final int S = 64;

    @Override
    public Mesh generate(File file, String[] tokens) throws Exception {
        MeshBuilder builder = new MeshBuilder();
        int rows = tokens.length - 1;
        int cols = tokens[1].length();

        for(int r = 1; r != tokens.length; r++) {
            for(int c = 0; c != tokens[r].length(); c++) {
                char ch = tokens[r].charAt(c);

                if(ch == '1' || ch == '0' || ch == '-' || ch == 'd') {
                    int x = -S * cols / 2 + c * S;
                    int z = -S * rows / 2 + (r - 1) * S;
                    int b = builder.getVertexCount();
                    int f;

                    if(ch == '-' || ch == 'd') {
                        builder.addVertex(x, -S, z);
                        builder.addVertex(x, -S, z + S);
                        builder.addVertex(x + S, -S, z + S);
                        builder.addVertex(x + S, -S, z);
                    } else {
                        builder.addVertex(x, 0, z);
                        builder.addVertex(x, 0, z + S);
                        builder.addVertex(x + S, 0, z + S);
                        builder.addVertex(x + S, 0, z);
                    }
                    f = builder.addFace(false, b, b + 1, b + 2, b + 3);
                    builder.setQuadTextureCoordinates(f, 0, 128 + ((ch == '0' || ch == 'd') ? 64 : 0), 64, 64, 64, 256);
                    b += 4;

                    builder.addVertex(x, S * 2, z);
                    builder.addVertex(x, S * 2, z + S);
                    builder.addVertex(x + S, S * 2, z + S);
                    builder.addVertex(x + S, S * 2, z);
                    f = builder.addFace(true, b, b + 1, b + 2, b + 3);
                    builder.setQuadTextureCoordinates(f, 0, 128, 64, 64, 64, 256);
                    b += 4;

                    if(tokens[r - 1].charAt(c) == '*') {
                        builder.addVertex(x, 0, z);
                        builder.addVertex(x + S, 0, z);
                        builder.addVertex(x + S, S * 2, z);
                        builder.addVertex(x, S * 2, z);
                        f = builder.addFace(false, b, b + 1, b + 2, b + 3);
                        builder.setQuadTextureCoordinates(f, 0, 0, 64, 128, 64, 256);
                        builder.rotateTextureCoordinates(f, 2);
                        b += 4;
                    }
                    if(tokens[r + 1].charAt(c) == '*') {
                        builder.addVertex(x, 0, z + S);
                        builder.addVertex(x + S, 0, z + S);
                        builder.addVertex(x + S, S * 2, z + S);
                        builder.addVertex(x, S * 2, z + S);
                        f = builder.addFace(true, b, b + 1, b + 2, b + 3);
                        builder.setQuadTextureCoordinates(f, 0, 0, 64, 128, 64, 256);
                        b += 4;
                    }
                    if(tokens[r].charAt(c - 1) == '*') {
                        builder.addVertex(x, 0, z);
                        builder.addVertex(x, S * 2, z);
                        builder.addVertex(x, S * 2, z + S);
                        builder.addVertex(x, 0, z + S);
                        f = builder.addFace(false, b, b + 1, b + 2, b + 3);
                        builder.setQuadTextureCoordinates(f, 0, 0, 64, 128, 64, 256);
                        builder.rotateTextureCoordinates(f, 3);
                        b += 4;
                    }
                    if(tokens[r].charAt(c + 1) == '*') {
                        builder.addVertex(x + S, 0, z);
                        builder.addVertex(x + S, S * 2, z);
                        builder.addVertex(x + S, S * 2, z + S);
                        builder.addVertex(x + S, 0, z + S);
                        f = builder.addFace(true, b, b + 1, b + 2, b + 3);
                        builder.setQuadTextureCoordinates(f, 0, 0, 64, 128, 64, 256);
                        builder.rotateTextureCoordinates(f, 3);
                        b += 4;
                    }
                    if(ch == '-' || ch == 'd') {
                        if(tokens[r - 1].charAt(c) == '*' || tokens[r - 1].charAt(c) == '1' || tokens[r - 1].charAt(c) == '0') {
                            builder.addVertex(x, -S, z);
                            builder.addVertex(x + S, -S, z);
                            builder.addVertex(x + S, 0, z);
                            builder.addVertex(x, 0, z);
                            f = builder.addFace(false, b, b + 1, b + 2, b + 3);
                            builder.setQuadTextureCoordinates(f, 0, 128, 64, 64, 64, 256);
                            b += 4;
                        }
                        if(tokens[r + 1].charAt(c) == '*' || tokens[r + 1].charAt(c) == '1' || tokens[r + 1].charAt(c) == '0') {
                            builder.addVertex(x, -S, z + S);
                            builder.addVertex(x + S, -S, z + S);
                            builder.addVertex(x + S, 0, z + S);
                            builder.addVertex(x, 0, z + S);
                            f = builder.addFace(true, b, b + 1, b + 2, b + 3);
                            builder.setQuadTextureCoordinates(f, 0, 128, 64, 64, 64, 256);
                            b += 4;
                        }
                        if(tokens[r].charAt(c - 1) == '*' || tokens[r].charAt(c - 1) == '1' || tokens[r].charAt(c - 1) == '0') {
                            builder.addVertex(x, -S, z);
                            builder.addVertex(x, 0, z);
                            builder.addVertex(x, 0, z + S);
                            builder.addVertex(x, -S, z + S);
                            f = builder.addFace(false, b, b + 1, b + 2, b + 3);
                            builder.setQuadTextureCoordinates(f, 0, 128, 64, 64, 64, 256);
                            b += 4;
                        }
                        if(tokens[r].charAt(c + 1) == '*' || tokens[r].charAt(c + 1) == '1' || tokens[r].charAt(c + 1) == '0') {
                            builder.addVertex(x + S, -S, z);
                            builder.addVertex(x + S, 0, z);
                            builder.addVertex(x + S, 0, z + S);
                            builder.addVertex(x + S, -S, z + S);
                            f = builder.addFace(true, b, b + 1, b + 2, b + 3);
                            builder.setQuadTextureCoordinates(f, 0, 128, 64, 64, 64, 256);
                            b += 4;
                        }
                    }
                }
            }
        }
        builder.calcNormals(false);

        return builder.build(file);
    }
    
}
