package org.jgl3;

import java.io.File;

public interface AssetLoader {
    Object load(File file, AssetManager assets) throws Exception;
}
