package org.jgl3.demo;

import java.io.File;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;

public class SmoothBox implements MeshBuilder.Generator {

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

        builder.addBox(0, 0, 0, sx, sy, sz, dx, dy, dz, false);
        builder.subdivide(d);
        builder.smooth(s);
        builder.calcNormals(true);

        return builder.build(file);
    }
    
}
