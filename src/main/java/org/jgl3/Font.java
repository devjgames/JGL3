package org.jgl3;

import java.io.File;

public final class Font extends Resource {
    
    private final File file;
    private final Texture texture;
    private final int charW;
    private final int charH;
    private final int columns;
    private final int whiteX;
    private final int whiteY;
    private final int scale;

    public Font(File file, int charW, int charH, int columns, int whiteX, int whiteY, int scale) throws Exception {
        this.file = file;
        texture = (Texture)new Texture.Loader().load(file);
        this.charW = charW;
        this.charH = charH;
        this.columns = columns;
        this.whiteX = whiteX;
        this.whiteY = whiteY;
        this.scale = scale;
    }

    public File getFile() {
        return file;
    }

    public Texture getTexture() {
        return texture;
    }

    public int getCharWidth() {
        return charW;
    }

    public int getCharHeight() {
        return charH;
    }

    public int getColumns() {
        return columns;
    }

    public int getWhiteX() {
        return whiteX;
    }

    public int getWhiteY() {
        return whiteY;
    }

    public int getScale() {
        return scale;
    }

    @Override
    public void destroy() throws Exception {
        texture.destroy();
        super.destroy();
    }
}
