package org.jgl3.scene;

import java.io.File;

import org.jgl3.BoundingBox;
import org.jgl3.Game;
import org.jgl3.Triangle;

public interface Renderable {
    
    File getFile();

    BoundingBox getBounds();

    int getTriangleCount();

    Triangle getTriangle(int i, Triangle triangle);

    void update(Game game, Scene scene, Node node) throws Exception;

    void render(Game game, Scene scene, Node node) throws Exception;

    Renderable newInstance() throws Exception;
}
