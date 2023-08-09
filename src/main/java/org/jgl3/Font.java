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
        this(file, Game.getInstance().getAssets().load(file), charW, charH, columns, whiteX, whiteY, scale);
    }

    public Font(Class<?> cls, String name, int charW, int charH, int columns, int whiteX, int whiteY, int scale) throws Exception {
        this(null, Texture.load(cls, name), charW, charH, columns, whiteX, whiteY, scale);
    }

    private Font(File file, Texture texture, int charW, int charH, int columns, int whiteX, int whiteY, int scale) throws Exception {
        this.file = file;
        this.texture = texture;
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

    public void measure(String s, int lineSpacing, int[] size) {
        int w = 0;
        int p = 0;
        int l = 0;

        size[0] = 0;
        size[1] = 0;

        for(int i = 0; i != s.length(); i++) {
            char c = s.charAt(i);

            if(c == '\n') {
                l += charH * scale;
                size[1] = l;
                size[0] = Math.max(size[0], w);
                w = 0;
                l += lineSpacing * scale;
                p++;
            } else {
                int j = (int)c - (int)' ';

                if(j >= 0 && j < 100) {
                    w += charW * scale;
                    p++;
                }
            }
        }
        if(p != 0) {
            size[0] = Math.max(size[0], w);
            size[1] = size[1] + charH * scale + ((l != 0) ? lineSpacing * scale : 0);
        }
    }

    @Override
    public void destroy() throws Exception {
        texture.destroy();
        super.destroy();
    }
}
