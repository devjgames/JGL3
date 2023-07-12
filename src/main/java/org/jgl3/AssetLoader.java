package org.jgl3;

import java.io.File;

public interface AssetLoader {
    Object load(File file) throws Exception;
}
