package org.jgl3.demo;

import java.io.File;

import org.jgl3.scene.Mesh;
import org.jgl3.scene.MeshBuilder;

public class SmoothCube implements MeshBuilder.Generator {

    @Override
    public Mesh generate(File file) throws Exception {
        MeshBuilder builder = new MeshBuilder();

        builder.addBox(0, 0, 0, 64, 64, 64, 1, 1, 1, false);
        builder.subdivide(4);
        builder.smooth(8);
        builder.calcNormals(true);

        return builder.build(file);
    }
    
}
