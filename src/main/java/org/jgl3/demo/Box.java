package org.jgl3.demo;

import java.io.File;
import java.util.Random;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;
import org.joml.Vector3f;

public class Box implements MeshBuilder.Generator {

    @Override
    public Mesh generate(File file, String[] tokens) throws Exception {
        MeshBuilder builder = new MeshBuilder();
        float sx = Float.parseFloat(tokens[1]);
        float sy = Float.parseFloat(tokens[2]);
        float sz = Float.parseFloat(tokens[3]);
        int dx = Integer.parseInt(tokens[4]);
        int dy = Integer.parseInt(tokens[5]);
        int dz = Integer.parseInt(tokens[6]);
        int d = Integer.parseInt(tokens[7]);
        int s = Integer.parseInt(tokens[8]);
        float units = Float.parseFloat(tokens[9]);
        int noise = Integer.parseInt(tokens[10]);


        builder.addBox(0, 0, 0, sx, sy, sz, dx, dy, dz, false);
        if(noise != 0) {
            Random random = new Random(1000);

            builder.calcNormals(false);
            for(int i = 0; i != builder.getVertexCount(); i++) {
                Vector3f p = builder.getVertexPosition(i);
                Vector3f n = builder.getVertexNormal(i);
                float amount = -noise + random.nextFloat() * noise * 2;

                p.add(n.x * amount, n.y * amount, n.z * amount);
            }
        }
        builder.subdivide(d);
        builder.smooth(s);
        builder.calcNormals(true);
        builder.calcTextureCoordinates(0, 0, 0, units, 0);

        return builder.build(file);
    }
    
}
