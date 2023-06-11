package org.jgl3.demo;

import java.io.File;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;
import org.joml.Vector3f;

public class Tree implements  MeshBuilder.Generator {

    @Override
    public Mesh generate(File file, String[] tokens) throws Exception {
        MeshBuilder builder = new MeshBuilder();

        builder.addBox(0, 24, 0, 48, 48, 48, 1, 1, 1, false);
        builder.calcNormals(true);
        for(int i = 0; i != builder.getFaceCount(); i++) {
            Vector3f n = builder.getFaceNormal(i);

            if(n.y > 0.5f) {
                builder.setQuadTextureCoordinates(i, 4, 4, 1, 1, 512, 512);
            } else if(n.y < -0.5f) {
                builder.setQuadTextureCoordinates(i, 4, 508, 1, 1, 512, 512);
            } else {
                builder.setQuadTextureCoordinates(i, 0.5f, 0.5f, 512 - 1, 512 - 1, 512, 512);
                if(n.x < -0.5f) {
                    builder.rotateTextureCoordinates(i, 2);
                } else if(n.x > 0.5f) {
                    builder.rotateTextureCoordinates(i, 3);
                } else if(n.z > 0.5f) {
                    builder.rotateTextureCoordinates(i, 2);
                } else {
                    builder.rotateTextureCoordinates(i, 3);
                }
            }
        }
        for(int i = 0; i != builder.getVertexCount(); i++) {
            Vector3f p = builder.getVertexPosition(i);

            if(p.y > 24) {
                p.mul(0.6f, 1, 0.6f);
            }
        }
        builder.subdivide(2);
        builder.smooth(1);
        builder.subdivide(1);
        builder.smooth(3);
        builder.calcNormals(true);

        return builder.build(file);
    }
    
}
