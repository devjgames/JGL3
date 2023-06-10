package org.jgl3.demo;

import java.io.File;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;
import org.joml.Vector3f;

public class Ledge2 implements MeshBuilder.Generator  {

    @Override
    public Mesh generate(File file) throws Exception {
        MeshBuilder builder = new MeshBuilder();

        builder.addBox(0, -32, 0, 128, 64, 128, 2, 1, 2, false);
        builder.calcNormals(true);
        for(int i = 0; i != builder.getFaceCount(); i++) {
            Vector3f n = builder.getFaceNormal(i);

            if(Math.abs(n.y) < 0.5f) {
                builder.setQuadTextureCoordinates(i, 0.5f, 0.5f, 512 - 1, 512 - 1, 1024, 1024);
                if(n.z > 0.5f || n.x < -0.5f) {
                    builder.rotateTextureCoordinates(i, 2);
                } else {
                    builder.rotateTextureCoordinates(i, 3);
                }
            } else if(n.y > 0.5f) {
                builder.setQuadTextureCoordinates(i, 512 + 0.5f, 0.5f, 512 - 1, 512 - 1, 1024, 1024);
            } else {
                builder.setQuadTextureCoordinates(i, 0.5f, 512 + 0.5f, 512 - 1, 512 - 1, 1024, 1024);
            }
        }
        builder.subdivide(3);
        builder.smooth(8);
        builder.calcNormals(true);

        return builder.build(file);
    }
    
}
